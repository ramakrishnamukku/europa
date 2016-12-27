/*
  $Id: $
  @file RepoMonitorTask.java
  @brief Contains the RepoMonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.persistence.PageIterator;

public abstract class RepoMonitorTask extends MonitorTask
{
    private static final Logger log = Logger.getLogger(RepoMonitorTask.class);

    @Inject
    protected RegistryCredsDb _registryCredsDb = null;
    @Inject
    protected RepoEventsDb _repoEventsDb = null;

    protected ContainerRepo _repo;
    public RepoMonitorTask(ContainerRepo repo, CountDownLatch latch)
    {
        super(latch);
        _repo = repo;
    }

    protected List<DockerImageId> getNewImages(List<DockerImageId> images, List<RepoEvent> events)
    {
        Map<String, RepoEvent> eventsBySha = new HashMap<String, RepoEvent>();
        for(RepoEvent event : events)
            eventsBySha.put(event.getImageSha(), event);
        List<DockerImageId> newImages = new ArrayList<DockerImageId>();
        for(DockerImageId imageId : images)
        {
            RepoEvent event = eventsBySha.get(imageId.getSha());
            if(event == null)
                newImages.add(imageId);
        }
        return newImages;
    }


    protected List<RepoEvent> listEvents()
    {
        PageIterator iter = new PageIterator().pageSize(1000);
        List<RepoEvent> eventList = new ArrayList<RepoEvent>();
        do {
            List<RepoEvent> events = _repoEventsDb.listEvents(_repo.getDomain(),
                                                              _repo.getId(),
                                                              iter);
            eventList.addAll(events);
        } while(iter.getMarker() != null);

        return eventList;
    }

    protected void saveNewEvents(List<DockerImageId> images)
    {
        if(images == null || images.size() == 0)
            return;
        PageIterator pageIterator = new PageIterator().pageSize(100);
        List<DockerImage> imagesToSave = new ArrayList<DockerImage>();
        do {
            List<DockerImage> imageList = describeImages(images, pageIterator);
            imagesToSave.addAll(imageList);
        } while(pageIterator.getMarker() != null);

        Collections.sort(imagesToSave, new DockerImageComparator());
        for(DockerImage image: imagesToSave)
        {
            RepoEvent repoEvent = RepoEvent
            .builder()
            .domain(_repo.getDomain())
            .repoId(_repo.getId())
            .eventType(RepoEventType.PUSH)
            .eventTime(image.getPushTime())
            .imageSize(image.getImageSize())
            .imageTags(image.getImageTags())
            .imageSha(image.getImageSha())
            .build();

            try {
                _repoEventsDb.save(repoEvent);
            } catch(Throwable t) {
                log.error("Failed to save RepoEvent: "+repoEvent+": "+t.getMessage(), t);
            }
        }
    }

    protected abstract List<DockerImage> describeImages(List<DockerImageId> images, PageIterator pageIterator);
}
