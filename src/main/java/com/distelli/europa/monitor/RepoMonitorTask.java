/*
  $Id: $
  @file RepoMonitorTask.java
  @brief Contains the RepoMonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;

import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.notifiers.*;
import com.distelli.persistence.PageIterator;
import lombok.extern.log4j.Log4j;

@Log4j
public abstract class RepoMonitorTask extends MonitorTask
{
    @Inject
    protected RegistryCredsDb _registryCredsDb = null;
    @Inject
    protected RepoEventsDb _repoEventsDb = null;
    @Inject
    protected NotificationsDb _notificationDb = null;
    @Inject
    protected Notifier _notifier = null;
    @Inject
    protected ContainerRepoDb _containerRepoDb;

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
        if(log.isDebugEnabled())
            log.debug("Found "+newImages.size()+" new images in repo: "+_repo);
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
        if(log.isDebugEnabled())
            log.debug("Found "+eventList.size()+" events in repo: "+_repo);
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
        RepoEvent lastEventSaved = null;
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
                if(log.isDebugEnabled())
                    log.debug("Saving RepoEvent: "+repoEvent+" for Repo: "+_repo);
                _repoEventsDb.save(repoEvent);
                lastEventSaved = repoEvent;
                notify(image, repoEvent);
            } catch(Throwable t) {
                log.error("Failed to save RepoEvent: "+repoEvent+": "+t.getMessage(), t);
            }
        }

        if(lastEventSaved != null)
            _containerRepoDb.setLastEvent(_repo.getDomain(), _repo.getId(), lastEventSaved);
    }

    protected void notify(DockerImage image, RepoEvent event)
    {
        //first get the list of notifications.
        //for each notification call the notifier
        List<Notification> notifications = _notificationDb.listNotifications(_repo.getDomain(),
                                                                             _repo.getId(),
                                                                             new PageIterator().pageSize(100));
        List<String> nfIdList = new ArrayList<String>();
        for(Notification notification : notifications)
        {
            if(log.isDebugEnabled())
                log.debug("Triggering Notification: "+notification+" for Image: "+image+" and Event: "+event);
            NotificationId nfId = _notifier.notify(notification, image, _repo);
            if(nfId != null)
                nfIdList.add(nfId.toCanonicalId());
        }
        _repoEventsDb.setNotifications(event.getDomain(), event.getRepoId(), event.getId(), nfIdList);
        event.setNotifications(nfIdList);
    }

    protected abstract List<DockerImage> describeImages(List<DockerImageId> images, PageIterator pageIterator);
}
