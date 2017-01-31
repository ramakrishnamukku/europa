/*
  $Id: $
  @file RepoMonitorTask.java
  @brief Contains the RepoMonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.util.function.Function;
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
    protected RegistryManifestDb _manifestDb = null;
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

    // Returns a list of tags that were removed:
    protected <T> List<String> findChanges(Map<String, T> tagToSha, Function<T, String> getSha) {
        List<String> removed = new ArrayList<>();
        for ( PageIterator iter : new PageIterator() ) {
            for ( RegistryManifest manifest :
                      _manifestDb.listManifestsByRepoId(_repo.getDomain(), _repo.getId(), iter) )
            {
                String tag = manifest.getTag();
                T elm = tagToSha.get(tag);
                if ( null == elm ) {
                    removed.add(tag);
                } else {
                    String sha = getSha.apply(elm);
                    if ( manifest.getManifestId().equals(sha) ) {
                        tagToSha.remove(tag);
                    }
                }
            }
        }
        return removed;
    }

    protected List<DockerImage> saveManifestChanges(List<DockerImage> images) {
        Collections.sort(images, new DockerImageComparator());
        for ( DockerImage image : images ) {
            for ( String tag : image.getImageTags() ) {
                if ( null == image.getImageSha() ) {
                    _manifestDb.remove(_repo.getDomain(), _repo.getId(), tag);
                } else {
                    _manifestDb.put(
                        RegistryManifest.builder()
                        .domain(_repo.getDomain())
                        .uploadedBy(_repo.getDomain())
                        .contentType("application/octet-stream")
                        .containerRepoId(_repo.getId())
                        .tag(tag)
                        .manifestId(image.getImageSha())
                        .virtualSize(image.getImageSize())
                        .pushTime(image.getPushTime())
                        .build());
                }
            }
        }
        return images;
    }

    protected List<DockerImage> saveNewEvents(List<DockerImage> images) {
        RepoEvent lastEventSaved = null;
        for ( DockerImage image: images ) {
            RepoEvent repoEvent = RepoEvent.builder()
                .domain(_repo.getDomain())
                .repoId(_repo.getId())
                .eventType((null == image.getImageSha()) ? RepoEventType.DELETE : RepoEventType.PUSH)
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
        return images;
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
}
