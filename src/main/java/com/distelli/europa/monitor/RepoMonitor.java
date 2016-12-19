/*
  $Id: $
  @file RepoMonitor.java
  @brief Contains the RepoMonitor.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import org.apache.log4j.Logger;

public class RepoMonitor implements Runnable
{
    private static final Logger log = Logger.getLogger(RepoMonitor.class);

    private MonitorQueue _monitorQueue;
    private boolean _running = true;

    private static final int CORE_POOL_SIZE = 50;
    private static final int MAX_POOL_SIZE = 50;

    private LinkedBlockingQueue<Runnable> _workQueue = null;
    private ThreadPoolExecutor _threadPool = null;

    public RepoMonitor(MonitorQueue monitorQueue)
    {
        _monitorQueue = monitorQueue;
        _workQueue = new LinkedBlockingQueue<Runnable>(1000);
        _threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                                             MAX_POOL_SIZE,
                                             60,
                                             TimeUnit.SECONDS,
                                             _workQueue,
                                             new ThreadPoolExecutor.CallerRunsPolicy());
        _threadPool.allowCoreThreadTimeOut(true);
    }

    public void run()
    {
        Thread.currentThread().setName("RepoMonitor");
        log.info("Starting RepoMonitor...");
        while(_running)
        {
            try {
                monitorUOW();
            } catch(Throwable t) {
                log.error(t.getMessage(),t);
            }

            try {
                if(!_running)
                    continue;
                Thread.sleep(60000);
            } catch(InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void monitorUOW()
        throws InterruptedException
    {
        MonitorTaskList taskList = _monitorQueue.getMonitorTasks();
        List<MonitorTask> tasks = taskList.getTasks();
        CountDownLatch latch = taskList.getCountDownLatch();
        for(MonitorTask task : tasks)
            _threadPool.submit(task);

        latch.await();
    }

    private synchronized void shutdown()
    {
        _running = false;
    }
}
