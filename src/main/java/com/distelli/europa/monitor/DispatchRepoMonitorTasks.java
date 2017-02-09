package com.distelli.europa.monitor;

import com.distelli.europa.Constants;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.models.StorageSettings;
import com.distelli.persistence.PageIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import com.distelli.europa.models.ContainerRepo;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j;
import java.util.Map;
import java.util.HashMap;

@Singleton @Log4j
public class DispatchRepoMonitorTasks implements Runnable {
    public static long TIME_INTERVAL_MICROSECONDS = 60 * 1000 * 1000;
    @Inject
    private ContainerRepoDb _containerRepoDb;
    @Inject
    private Provider<StorageSettings> _storageSettingsProvider;
    @Inject
    protected ScheduledExecutorService _scheduledExecutorService;
    @Inject
    private MonitorTaskFactory _monitorTaskFactory;

    // A semaphore is more flexible, since it doesn't require knowing the
    // number of tasks up-front:
    private Semaphore _semaphore = new Semaphore(0);
    private int _taskCount = 0;
    private boolean _scheduled = false;
    private Map<String, Long> _syncCounts = new HashMap<>();

    public synchronized void schedule() {
        _scheduledExecutorService.scheduleAtFixedRate(this, 0, TIME_INTERVAL_MICROSECONDS, TimeUnit.MICROSECONDS);
        _scheduled = true;
    }

    @Override
    public void run() {
        try {
            runThrows();
        } catch ( Throwable ex ) {
            log.error(ex.getMessage(), ex);
        }
    }

    public synchronized void runThrows() throws Exception {
        StorageSettings storageSettings = _storageSettingsProvider.get();
        if(storageSettings == null) {
            if(log.isDebugEnabled())
                log.debug("Skipping getMonitorTasks. Storage Not Initialized");
            return;
        }

        // Wait for all previous tasks to finish:
        try {
            log.debug("Waiting for "+_taskCount+" tasks to complete");
            _semaphore.acquire(_taskCount);
            _taskCount = 0;
        } catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
            return;
        }
        log.debug("Finding all ContainerRepos");
        List<Runnable> tasks = new ArrayList<>();
        for ( PageIterator iter : new PageIterator().pageSize(100) ) {
            for ( ContainerRepo repo : _containerRepoDb.listRepos(Constants.DOMAIN_ZERO, iter) ) {
                Runnable task = _monitorTaskFactory.createMonitorTask(repo);
                if ( null == task ) continue;
                String repoPK = repo.getDomain() + ":" + repo.getId();
                Long syncCount = _syncCounts.get(repoPK);
                if ( null == syncCount ) {
                    syncCount = repo.getSyncCount();
                    _syncCounts.put(repoPK, syncCount);
                } else {
                    _syncCounts.put(repoPK, ++syncCount);
                }
                repo.setSyncCount(syncCount);
                tasks.add(task);
            }
        }
        Collections.shuffle(tasks);
        log.debug("Scheduling " + tasks.size()+" tasks");
        for ( Runnable task : tasks ) {
            _scheduledExecutorService.schedule(
                // Guarantee that the semaphore is released after the task completes:
                () -> {
                    try {
                        task.run();
                    } finally {
                        _semaphore.release();
                    }
                },
                ( _taskCount * TIME_INTERVAL_MICROSECONDS ) / tasks.size(),
                TimeUnit.MICROSECONDS);
            _taskCount++;
        }
    }
}
