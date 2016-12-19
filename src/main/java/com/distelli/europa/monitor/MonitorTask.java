/*
  $Id: $
  @file MonitorTask.java
  @brief Contains the MonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import com.distelli.europa.models.*;

public abstract class MonitorTask implements Runnable
{
    private static final Logger log = Logger.getLogger(MonitorTask.class);

    protected ContainerRepo _repo;
    protected CountDownLatch _latch;

    public MonitorTask(ContainerRepo repo, CountDownLatch latch)
    {
        _repo = repo;
        _latch = latch;
    }

    public void run()
    {
        try {
            monitor();
        } catch(Throwable t) {
            log.error(t.getMessage(), t);
        }
        if(_latch != null)
            _latch.countDown();
    }

    public abstract void monitor();
}
