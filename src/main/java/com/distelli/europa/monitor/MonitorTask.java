/*
  $Id: $
  @file MonitorTask.java
  @brief Contains the MonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import lombok.extern.log4j.Log4j;

@Log4j
public abstract class MonitorTask implements Runnable
{

    public MonitorTask()
    {
    }

    public void run()
    {
        try {
            if(log.isDebugEnabled())
                log.debug("Starting MonitorTask: "+this);
            monitor();
        } catch(Throwable ex) {
            if ( ! (ex instanceof java.io.InterruptedIOException) ) {
                log.error(ex.getMessage(), ex);
            }
        }
        if(log.isDebugEnabled())
            log.debug("Finished MonitorTask: "+this);
    }

    public abstract void monitor() throws Exception;
}
