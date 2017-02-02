/*
  $Id: $
  @file MonitorTaskFactory.java
  @brief Contains the MonitorTaskFactory.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import com.distelli.europa.models.ContainerRepo;
import javax.inject.Inject;

public class MonitorTaskFactory
{
    @Inject
    private EcrMonitorTask.Factory _ecrMonitorTaskFactory;
    @Inject
    private GcrMonitorTask.Factory _gcrMonitorTaskFactory;
    @Inject
    private DockerHubMonitorTask.Factory _dockerHubMonitorTaskFactory;

    public MonitorTask createMonitorTask(ContainerRepo repo)
    {
        switch ( repo.getProvider() ) {
        case ECR: return _ecrMonitorTaskFactory.create(repo);
        case GCR: return _gcrMonitorTaskFactory.create(repo);
        case DOCKERHUB: return _dockerHubMonitorTaskFactory.create(repo);
        }
        return null;
    }
}
