/*
  $Id: $
  @file GcrMonitorTask.java
  @brief Contains the GcrMonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import org.apache.log4j.Logger;
import java.util.concurrent.CountDownLatch;
import com.google.inject.assistedinject.Assisted;

import org.apache.log4j.Logger;
import com.distelli.europa.models.*;
import javax.inject.Inject;

public class GcrMonitorTask extends MonitorTask
{
    private static final Logger log = Logger.getLogger(GcrMonitorTask.class);

    public interface Factory {
        public GcrMonitorTask create(ContainerRepo repo,
                                     CountDownLatch latch);
    }

    @Inject
    public GcrMonitorTask(@Assisted ContainerRepo repo,
                          @Assisted CountDownLatch latch)
    {
        super(repo, latch);
    }

    public void monitor()
    {
        System.out.println("Monitoring GCR repo: "+_repo);
    }
}
