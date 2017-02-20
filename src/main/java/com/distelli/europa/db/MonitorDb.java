package com.distelli.europa.db;

import java.util.HashMap;
import com.distelli.europa.models.Monitor;
import com.distelli.europa.tasks.ReapMonitorTask;
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
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import java.util.HashSet;
import java.lang.management.ManagementFactory;
import javax.persistence.RollbackException;
import com.distelli.europa.EuropaVersion;

@Log4j
@Singleton
public class MonitorDb extends BaseDb
{
    @Inject
    private ScheduledExecutorService _executor;
    @Inject
    private TasksDb _tasksDb;
    private Index<Monitor> _main;
    private final ObjectMapper _om = new ObjectMapper();
    private Map<String, Long> _heartbeats = new HashMap<>();

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName("monitors")
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("id", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(Monitor.class)
            .put("id", String.class, "id")
            .put("nam", String.class, "nodeName")
            .put("ver", String.class, "version")
            .put("hb", Long.class, "heartbeat");
        return module;
    }

    @Inject
    public MonitorDb(Index.Factory indexFactory) {
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(Monitor.class)
            .withNoEncrypt("hb")
            .withTableDescription(getTableDescription())
            .withConvertValue(_om::convertValue)
            .build();
    }

    // Should only be ran once in the VM:
    public Monitor startMonitor() {
        Monitor monitor = createMonitor();
        ScheduledFuture<?> heartbeat = _executor.scheduleAtFixedRate(
            () -> heartbeat(monitor),
            ThreadLocalRandom.current().nextLong(Monitor.HEARTBEAT_INTERVAL_MS),
            Monitor.HEARTBEAT_INTERVAL_MS,
            TimeUnit.MILLISECONDS);
        long reap_interval_ms = Monitor.HEARTBEAT_INTERVAL_MS * Monitor.REAP_INTERVAL;
        ScheduledFuture<?> reaper = _executor.scheduleAtFixedRate(
            () -> reaper(monitor),
            ThreadLocalRandom.current().nextLong(reap_interval_ms),
            reap_interval_ms,
            TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        reaper.cancel(false);
                        heartbeat.cancel(false);
                        String monitorId = monitor.getId();
                        monitor.forceAllMonitorsToHalt(null);
                        if ( ! monitor.isFailHeartbeat() ) {
                            _main.deleteItem(monitorId, null);
                        }
                    } catch ( Throwable ex ) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            });
        return monitor;
    }

    public List<Monitor> listMonitors(PageIterator iter) {
        return _main.scanItems(iter);
    }

    private Monitor createMonitor() {
        Monitor monitor = Monitor.builder()
            .id(CompactUUID.randomUUID().toString())
            .nodeName(ManagementFactory.getRuntimeMXBean().getName())
            .version(EuropaVersion.VERSION)
            .heartbeat(1)
            .build();
        _main.putItem(monitor);
        return monitor;
    }

    private void reaper(Monitor liveMonitor) {
        synchronized ( _heartbeats ) {
            Set<String> monitorIds = new HashSet<>(_heartbeats.keySet());
            for ( PageIterator iter : new PageIterator().pageSize(100) ) {
                for ( Monitor monitor : listMonitors(iter) ) {
                    String monitorId = monitor.getId();
                    monitorIds.remove(monitorId);
                    Long lastHB = _heartbeats.get(monitorId);
                    _heartbeats.put(monitorId, monitor.getHeartbeat());
                    // New monitor observed:
                    if ( null == lastHB ) {
                        continue;
                    }
                    // Heartbeats observed:
                    if ( monitor.getHeartbeat() != lastHB ) {
                        continue;
                    }
                    // Dead monitor observed:
                    reap(liveMonitor, monitor);
                }
            }
            // Keep the _heartbeats map tidy:
            for ( String monitorId : monitorIds ) {
                _heartbeats.remove(monitorId);
            }
        }
    }

    private void reap(Monitor liveMonitor, Monitor monitor) {
        // Add task to delete references:
        _tasksDb.addTask(liveMonitor,
                         ReapMonitorTask.builder()
                         .monitorId(monitor.getId())
                         .build());
        // Delete the monitor:
        _main.deleteItem(monitor.getId(), null);
    }

    private void heartbeat(Monitor monitor) {
        // We are already shutting down:
        String monitorId = monitor.getId();
        if ( null == monitorId ) return;
        if ( ! monitor.isFailHeartbeat() ) {
            try {
                monitor.incrementHeartbeat();
                _main.updateItem(monitor.getId(), null)
                    .increment("hb", 1)
                    .when((expr) -> expr.exists("id"));
                return;
            } catch ( Throwable ex ) {
                // Monitor was shutdown during DB update:
                if ( null == monitor.getId() ) return;

                if ( ex instanceof RollbackException ) {
                    // This could happen if the computer is put to sleep:
                    log.warn("Detected monitor deletion, forcing all tasks to stop (perhaps computer sleeped).");
                } else {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
        try {
            monitor.forceAllMonitorsToHalt(createMonitor());
            _main.deleteItem(monitorId, null);
        } catch ( Throwable ex ) {
            log.error(ex.getMessage(), ex);
        }
    }
}
