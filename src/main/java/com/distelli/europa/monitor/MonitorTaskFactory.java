/*
  $Id: $
  @file MonitorTaskFactory.java
  @brief Contains the MonitorTaskFactory.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import com.distelli.europa.models.*;
import javax.inject.Inject;

public class MonitorTaskFactory
{
    @Inject
    private EcrMonitorTask.Factory _ecrMonitorTaskFactory;
    @Inject
    private GcrMonitorTask.Factory _gcrMonitorTaskFactory;

    public MonitorTask createMonitorTask(ContainerRepo repo)
    {
        RegistryProvider provider = repo.getProvider();
        if(provider == RegistryProvider.ECR)
            return _ecrMonitorTaskFactory.create(repo);
        else if(provider == RegistryProvider.GCR)
            return _gcrMonitorTaskFactory.create(repo);
        else
            return null;
    }
}
