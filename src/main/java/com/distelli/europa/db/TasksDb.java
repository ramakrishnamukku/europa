package com.distelli.europa.db;

import com.distelli.europa.models.Monitor;
import com.distelli.europa.models.RawTaskEntry;
import com.distelli.europa.tasks.Task;
import com.distelli.europa.tasks.TaskFactory;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.distelli.utils.CompactUUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.lang.management.ManagementFactory;
import javax.persistence.RollbackException;
import java.lang.management.ManagementFactory;
import static com.distelli.utils.LongSortKey.*;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

@Log4j
@Singleton
public class TasksDb extends BaseDb
{
    private static final int POLL_INTERVAL_MS = 10000;
    private static final int MAX_TASKS_IN_INTERVAL = 100;
    @Inject
    private MonitorDb _monitorDb;
    @Inject
    private Monitor _monitor;
    @Inject
    private ScheduledExecutorService _executor;
    @Inject
    private SequenceDb _seqDb;
    @Inject
    private Map<String, TaskFactory> _taskFactories;
    private Index<Lock> _locks;
    private Index<Lock> _locksForMonitor;
    private Index<RawTaskEntry> _tasks;
    private Index<RawTaskEntry> _tasksForMonitor;
    private Index<RawTaskEntry> _tasksForEntity;
    private final ObjectMapper _om = new ObjectMapper();

    private static class Lock {
        // Primary key:
        public String lid; // unique identifier for lock.

        public String tid; // LongSortKey(taskId) or "#" to indicate this is the actual lock.

        // Optimistic lock fields:
        public String mid; // monitor id working on task.
        public Long agn; // Incremented to indicate the task needs to run again.
    }

    public static TableDescription getLocksTableDescription() {
        return TableDescription.builder()
            .tableName("locks")
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("lid", AttrType.STR))
                    .rangeKey(attr("tid", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    // Used to find orphaned locks (monitor stopped heartbeating):
                    IndexDescription.builder()
                    .hashKey(attr("mid", AttrType.STR))
                    .indexName("mid-index")
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    public static TableDescription getTasksTableDescription() {
        return TableDescription.builder()
            .tableName("tasks")
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("id", AttrType.NUM))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    // Used to find runnable tasks (mid='#') or
                    // find orphaned tasks (monitor stopped heartbeating):
                    IndexDescription.builder()
                    .hashKey(attr("mid", AttrType.STR))
                    .indexName("mid-index")
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    // Used to find history of tasks ran for entity:
                    IndexDescription.builder()
                    .hashKey(attr("ety", AttrType.STR))
                    .rangeKey(attr("eid", AttrType.STR))
                    .indexName("ety-eid-index")
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(RawTaskEntry.class)
            // PK
            .put("id", Long.class, "taskId")
            // AK
            .put("ety", String.class, "entityType")
            .put("eid", String.class, TasksDb::toEid, TasksDb::fromEid)
            // lock info:
            .put("lids", new TypeReference<Set<String>>(){}, "lockIds")
            .put("mid", String.class, "monitorId")
            // task state:
            .put("st8", byte[].class, "privateTaskState")
            .put("err", String.class, "errorMessage")
            .put("ts", Long.class, "startTime")
            .put("tf", Long.class, "endTime");
        return module;
    }

    private static String toEid(RawTaskEntry task) {
        return validEntityId(task.getEntityId()) + "@" + longToSortKey(task.getTaskId());
    }

    private static void fromEid(RawTaskEntry task, String eid) {
        if ( null == eid ) return;
        String[] fields = eid.split("@", 2);
        if ( fields.length != 2 ) return;
        task.setEntityId(fields[0]);
    }

    private static String validEntityId(String entityId) {
        if ( null == entityId ) {
            throw new IllegalArgumentException("entityId must not be null");
        }
        if ( entityId.contains("@") ) {
            throw new IllegalArgumentException("entityId must not contain '@', got="+entityId);
        }
        return entityId;
    }

    @Inject
    protected TasksDb(Index.Factory indexFactory) {
        _om.registerModule(createTransforms(new TransformModule()));

        _tasks = indexFactory.create(RawTaskEntry.class)
            .withTableDescription(getTasksTableDescription())
            .withConvertValue(_om::convertValue)
            .build();

        _tasksForMonitor = indexFactory.create(RawTaskEntry.class)
            .withTableDescription(getTasksTableDescription(), "mid-index")
            .withConvertValue(_om::convertValue)
            .build();

        _tasksForEntity = indexFactory.create(RawTaskEntry.class)
            .withTableDescription(getTasksTableDescription(), "ety-eid-index")
            .withConvertValue(_om::convertValue)
            .build();

        _locks = indexFactory.create(Lock.class)
            .withNoEncrypt("mid", "agn")
            .withTableDescription(getLocksTableDescription())
            .withConvertValue(_om::convertValue)
            .build();

        _locksForMonitor = indexFactory.create(Lock.class)
            .withNoEncrypt("mid", "agn")
            .withTableDescription(getLocksTableDescription(), "mid-index")
            .withConvertValue(_om::convertValue)
            .build();
    }

    @Inject
    protected void init() {
        _executor.scheduleAtFixedRate(
            this::startRunnableTasks,
            ThreadLocalRandom.current().nextLong(POLL_INTERVAL_MS),
            POLL_INTERVAL_MS,
            TimeUnit.MILLISECONDS);
    }

    private void startRunnableTasks() {
        List<RawTaskEntry> tasks = _tasksForMonitor
            .queryItems("#", new PageIterator().pageSize(MAX_TASKS_IN_INTERVAL))
            .list();
        // Randomly distribute the tasks over poll interval:
        for ( RawTaskEntry task : tasks ) {
            _executor.schedule(
                () -> runTask(_monitor, task.getTaskId()),
                ThreadLocalRandom.current().nextLong(POLL_INTERVAL_MS),
                TimeUnit.MILLISECONDS);
        }
    }

    //////////////////////////////////////////////////////////////////////
    // Call this to add a task. You can wait on the future returned.
    //////////////////////////////////////////////////////////////////////
    public Future<?> addTask(Monitor monitor, Task task) {
        // [1] Setup and validation:
        if ( null == monitor ) {
            throw new IllegalArgumentException("monitor may not be null");
        }

        RawTaskEntry rawTask = task.toRawTaskEntry();
        if ( null == rawTask.getEntityType() ) {
            throw new IllegalArgumentException("missing task.rawTaskEntry.entityType");
        }
        if ( null == rawTask.getEntityId() ) {
            throw new IllegalArgumentException("missing task.rawTaskEntry.entityId");
        }
        TaskFactory taskFactory =
            _taskFactories.get(rawTask.getEntityType());
        if ( null == taskFactory ) {
            throw new IllegalArgumentException(
                "missing dependency injector for task.rawTaskEntry.entityType="+
                rawTask.getEntityType());
        }
        // Make sure the raw task only has these fields set:
        Set<String> lockIds = rawTask.getLockIds();
        rawTask = RawTaskEntry.builder()
            .taskId(_seqDb.nextTaskId())
            .entityType(rawTask.getEntityType())
            .entityId(rawTask.getEntityId())
            .lockIds((null == lockIds)?Collections.emptySet():lockIds)
            .monitorId("#") // Make sure this is marked as "runnable".
            .privateTaskState(rawTask.getPrivateTaskState())
            // These fields should be null:
            // .errorMessage(null)
            // .startTime(null)
            // .endTime(null)
            .build();

        // Verify that we can transform this into a Runnable:
        taskFactory.toRunnable(rawTask);

        // [2] Create entry in tasks table:
        _tasks.putItemOrThrow(rawTask);

        // [3] Dispatch thread which will attempt to lock and run task.
        long taskId = rawTask.getTaskId();
        return _executor.submit(
            () -> runTask(monitor, taskId));
    }

    //////////////////////////////////////////////////////////////////////
    // Call this in your task implementation to update the task state:
    //////////////////////////////////////////////////////////////////////
    public void updateTaskState(String taskId, byte[] taskState) {
        _tasks.updateItem(taskId, null)
            .set("st8", taskState)
            .always();
    }

    public void releaseLocksForMonitorId(String monitorId) {
        // Release locks on the "locks" table:
        for ( PageIterator iter : new PageIterator() ) {
            for ( Lock lock : _locksForMonitor.queryItems(monitorId, iter).list() ) {
                try {
                    _locks.updateItem(lock.lid, lock.tid)
                        .remove("mid")
                        .when((expr) -> expr.eq("mid", monitorId));
                } catch ( RollbackException ex ) {}
            }
        }
        // Release locks on individual "tasks":
        for ( PageIterator iter : new PageIterator() ) {
            for ( RawTaskEntry task : _tasksForMonitor.queryItems(monitorId, iter).list() ) {
                try {
                    _tasks.updateItem(task.getTaskId(), null)
                        .set("mid", "#")
                        .when((expr) -> expr.eq("mid", monitorId));
                } catch ( RollbackException ex ) {}
            }
        }
    }

    private void cleanupLocksForTaskId(Collection<String> lockIds, long taskId) {
        if ( null == lockIds ) return;
        for ( String lockId : lockIds ) {
            _locks.deleteItem(lockId, longToSortKey(taskId));
        }
    }

    private void runTask(Monitor monitor, long taskId) {
        // [1] wrap with monitor so thread is forced to stop if heartbeat fails:
        List<String> locksAcquired = new ArrayList<>();
        List<Long> lockAgns = new ArrayList<>();
        String monitorId = null;
        boolean wasThreadInterrupted = false;
        try ( Monitor.Lock monitorLock = monitor.monitor() ) {
            monitorId = monitorLock.getId();
            // [2] Lock this task
            RawTaskEntry task;
            try {
                task = _tasks.updateItem(taskId, null)
                    .set("mid", monitorId)
                    .returnAllNew()
                    // mid == # indicates a runnable task:
                    .when((expr) -> expr.eq("mid", "#"));
            } catch ( RollbackException ex ) {
                // Someone else locked the task:
                log.debug("Missed lock on taskId="+taskId);
                // Check if this task was triggered from a stale entry
                // in the locks table:
                task = _tasks.getItem(taskId);
                if ( null != task && null != task.getEndTime() ) {
                    cleanupLocksForTaskId(task.getLockIds(), taskId);
                }
                return;
            }

            // [3] Enqueue task for locks (might overwrite, that is fine):
            Set<String> lockIds = task.getLockIds();
            if ( null == lockIds ) lockIds = Collections.emptySet();
            for ( String lockId : lockIds ) {
                _locks.updateItem(lockId, longToSortKey(taskId))
                    .always();
                // Indicate that task is enqueued for lock:
                _locks.updateItem(lockId, "#")
                    .increment("agn", 1)
                    .always();
            }

            // [4] Attempt to acquire locks in sorted order (to avoid deadlock):
            List<String> sortedLockIds = new ArrayList<>(lockIds);
            Collections.sort(sortedLockIds);
            for ( String lockId : sortedLockIds ) {
                try {
                    // Add to locksAcquired before successful lock
                    // so we can attempt to release the lock:
                    locksAcquired.add(lockId);
                    Lock lock = _locks.updateItem(lockId, "#")
                        .set("mid", monitorId)
                        .returnAllNew()
                        .when((expr) -> expr.not(expr.exists("mid")));
                    lockAgns.add(lock.agn);
                } catch ( RollbackException ex ) {
                    // Someone else locked the task:
                    log.debug("Missed lock on lockId="+lockId+" when trying to run taskId="+taskId);
                    return;
                }
            }

            // [3] Run the task:
            TaskFactory taskFactory = _taskFactories.get(task.getEntityType());
            if ( null == taskFactory ) {
                // Probably means the fleet is not fully updated with support
                // for the new task type:
                log.debug("Unsupported taskType="+task.getEntityType()+" when trying to run taskId="+taskId);
                return;
            }
            String err = null;
            _tasks.updateItem(taskId, null)
                .set("ts", System.currentTimeMillis())
                .always();
            String threadName = null;
            try {
                threadName = Thread.currentThread().getName();
                Thread.currentThread().setName("TASK:"+taskId);
                taskFactory.toRunnable(task).run();
                wasThreadInterrupted = Thread.interrupted();
            } catch ( Throwable ex ) {
                wasThreadInterrupted = Thread.interrupted();
                if ( wasThreadInterrupted ) {
                    log.debug("Interrrupted: "+ex.getMessage(), ex);
                } else {
                    // Log a message with the full stack trace:
                    String errorId = CompactUUID.randomUUID().toString();
                    log.error("TASK FAILED: taskId="+taskId+
                              " errorId="+errorId+" "+ex.getMessage(), ex);
                    // Store in the DB the same errorId:
                    err = "errorId="+errorId+" nodeName="+ManagementFactory.getRuntimeMXBean().getName()+
                        " "+ex.getMessage();
                }
            } finally {
                if ( null != threadName ) {
                    Thread.currentThread().setName(threadName);
                }
            }
            if ( ! wasThreadInterrupted ) {
                _tasks.updateItem(taskId, null)
                    .set("err", err)
                    .set("tf", System.currentTimeMillis())
                    .remove("mid")
                    .always();
                // Remove entries from the locks table:
                cleanupLocksForTaskId(locksAcquired, taskId);
            }
        } catch ( Throwable ex ) {
            if ( null == monitorId ) {
                log.error(ex.getMessage(), ex);
            } else {
                // Must fail the monitor so this task is retried:
                monitor.setFailHeartbeat(monitorId);
                log.error("Failed to release task lock, forcing heartbeat failure. Error="+ex.getMessage(),
                          ex);
            }
        } finally {
            // Remove locks:
            if ( null == monitorId ) {
                // Reset the interrupt status:
                if ( wasThreadInterrupted ) Thread.currentThread().interrupt();
                return;
            }
            wasThreadInterrupted = Thread.interrupted() || wasThreadInterrupted;
            try {
                // Try to remove the task lock (fails if task completed)
                String finalMonitorId = monitorId;
                try {
                    _tasks.updateItem(taskId, null)
                        .set("mid", "#")
                        .when((expr) -> expr.eq("mid", finalMonitorId));
                } catch ( RollbackException ex ) {}

                // Release all locks:
                for ( String lockId : locksAcquired ) {
                    try {
                        _locks.updateItem(lockId, "#")
                            .remove("mid")
                            .when((expr) -> expr.eq("mid", finalMonitorId));
                    } catch ( RollbackException ex ) {}
                }

                if ( ! wasThreadInterrupted ) {
                    _executor.submit(() -> spawnTasksFor(monitor, locksAcquired, lockAgns));
                }
            } catch ( Throwable ex ) {
                monitor.setFailHeartbeat(monitorId);
                log.error("Failed to release task lock, forcing heartbeat failure. Error="+ex.getMessage(),
                          ex);
            } finally {
                // Reset the interrupt status:
                if ( wasThreadInterrupted ) Thread.currentThread().interrupt();
            }
        }
    }

    private void spawnTasksFor(Monitor monitor, List<String> lockIds, List<Long> lockAgns) {
        for ( int i=0; i < lockAgns.size(); i++ ) {
            String lockId = lockIds.get(i);
            Long agn = lockAgns.get(i);
            boolean spawnedTasks = false;
            for ( Lock lock : _locks.queryItems(
                      lockId, new PageIterator().pageSize(2)).list() )
            {
                if ( "#".equals(lock.tid) ) continue;
                Long taskId = sortKeyToLong(lock.tid);
                _executor.submit(() -> runTask(monitor, taskId));
                spawnedTasks = true;
            }
            if ( ! spawnedTasks ) {
                _locks.deleteItem(lockId, "#", (expr) -> expr.eq("agn", agn));
            }
        }
    }
}
