/*
  $Id: $
  @file MonitorTask.java
  @brief Contains the MonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.util.concurrent.CountDownLatch;
import lombok.extern.log4j.Log4j;

@Log4j
public abstract class MonitorTask implements Runnable
{
    protected CountDownLatch _latch;

    public MonitorTask(CountDownLatch latch)
    {
        _latch = latch;
    }

    public void run()
    {
        try {
            if(log.isDebugEnabled())
                log.debug("Starting MonitorTask: "+this.getClass().getName());
            monitor();
        } catch(Throwable t) {
            log.error(t.getMessage(), t);
        }
        if(_latch != null)
            _latch.countDown();

        if(log.isDebugEnabled())
            log.debug("Finished MonitorTask: "+this.getClass().getName());
    }

    public abstract void monitor();
}
