/*
  $Id: $
  @file EcrMonitorTask.java
  @brief Contains the EcrMonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.persistence.PageIterator;
import com.google.inject.assistedinject.Assisted;

public class EcrMonitorTask extends MonitorTask
{
    private static final Logger log = Logger.getLogger(EcrMonitorTask.class);

    public interface Factory {
        public EcrMonitorTask create(ContainerRepo repo,
                                     CountDownLatch latch);
    }

    @Inject
    private RegistryCredsDb _registryCredsDb = null;
    @Inject
    private RepoEventsDb _repoEventsDb = null;

    private ECRClient _ecrClient;

    @Inject
    public EcrMonitorTask(@Assisted ContainerRepo repo,
                          @Assisted CountDownLatch latch)
    {
        super(repo, latch);
    }

    public void monitor()
    {
        /**
           1. List all images from the repo - List<DockerImageId>
           2. List all the image events already stored
           3. Order the images not stored by time
           4. Store the new images
        */
        if(log.isDebugEnabled())
            log.debug("Monitoring Repo: "+_repo);
        if(_ecrClient == null)
            initEcrClient();
        List<DockerImageId> images = listImages();
        List<RepoEvent> events = listEvents();
        List<DockerImageId> imagesToSave = getNewImages(images, events);
        saveNewEvents(imagesToSave);
    }

    private void initEcrClient()
    {
        RegistryCred registryCred = _registryCredsDb.getCred(_repo.getDomain(),
                                                             _repo.getCredId());
        if(registryCred == null) {
            log.error("Failed to find RegistryCred for Repo: "+_repo);
            return;
        }

        _ecrClient = new ECRClient(registryCred);
    }

    private void saveNewEvents(List<DockerImageId> images)
    {
        if(images == null || images.size() == 0)
            return;
        PageIterator pageIterator = new PageIterator().pageSize(100);
        List<DockerImage> imagesToSave = new ArrayList<DockerImage>();
        do {
            List<DockerImage> imageList = _ecrClient.describeImages(_repo, images, pageIterator);
            imagesToSave.addAll(imageList);
        } while(pageIterator.getMarker() != null);

        Collections.sort(imagesToSave, new Comparator<DockerImage>() {
                public int compare(DockerImage i1, DockerImage i2) {
                    long i1PushTime = getPushTime(i1);
                    long i2PushTime = getPushTime(i2);
                    if(i1PushTime > i2PushTime)
                        return 1;
                    if(i1PushTime < i2PushTime)
                        return -1;
                    return 0;
                }

                private long getPushTime(DockerImage dockerImage)
                {
                    Long pushTime = dockerImage.getPushTime();
                    if(pushTime == null)
                        return 0;
                    return pushTime.longValue();
                }
            });
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

    private List<DockerImageId> getNewImages(List<DockerImageId> images, List<RepoEvent> events)
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

    private List<RepoEvent> listEvents()
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

    private List<DockerImageId> listImages()
    {
        List<DockerImageId> imageIdList = new ArrayList<DockerImageId>();
        if(_ecrClient == null)
            return imageIdList;

        PageIterator iter = new PageIterator().pageSize(100);
        do {
            List<DockerImageId> images = _ecrClient.listImages(_repo, iter);
            imageIdList.addAll(images);
        } while(iter.getMarker() != null);

        return imageIdList;
    }
}
