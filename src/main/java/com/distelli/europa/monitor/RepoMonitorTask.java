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
import javax.inject.Inject;

import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.notifiers.*;
import com.distelli.persistence.PageIterator;
import lombok.extern.log4j.Log4j;
import com.distelli.europa.pipeline.RunPipeline;

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
    @Inject
    private RunPipeline _runPipeline;
    @Inject
    private PipelineDb _pipelineDb;

    protected ContainerRepo _repo;
    public RepoMonitorTask(ContainerRepo repo)
    {
        _repo = repo;
    }

    /**
     * Do NOT run this task unless the syncCount matches. We do this so that tasks
     * can be evenly distributed over multiple servers.
     */
    @Override
    public void run()
    {
        try {
            if ( ! _containerRepoDb.incrementSyncCount(
                     _repo.getDomain(),
                     _repo.getId(),
                     _repo.getSyncCount()) )
            {
                log.debug("Failed to acquire lock for syncCount="+_repo.getSyncCount()+" repo="+_repo.getId());
                return;
            }
        } catch(Throwable t) {
            log.error(t.getMessage(), t);
            return;
        }
        super.run();
    }

    /**
     * Called by subclasses to find changes.
     *
     * PostCondition: tagToSha elements are removed if there was no changes, returns
     *   a list of tag names NOT in tagToSha that were apparently removed (since they
     *   are in the DB, but not in the tagToSha map).
     *
     * @param tagToSha is a map of tag names to objects that contain a "sha" (manifest digest)
     *
     * @param getSha is a function used get the "sha" from the object.
     *
     * @return a list of tag names that were removed.
     */
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

    /**
     * Called by subclasses to save information about DockerImage's that changed.
     */
    protected void saveChanges(List<DockerImage> images) {
        Collections.sort(images, new DockerImageComparator());
        saveManifests(images);
        saveEvents(images);
        executePipeline(images);
    }

    private void saveManifests(List<DockerImage> images) {
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
    }

    private void saveEvents(List<DockerImage> images) {
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
        //before we return lets set the last sync time on the container repo
        _repo.setLastSyncTime(System.currentTimeMillis());
        _containerRepoDb.setLastSyncTime(_repo.getDomain(), _repo.getId(), _repo.getLastSyncTime());
    }

    private void notify(DockerImage image, RepoEvent event)
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

    private void executePipeline(List<DockerImage> images) {
        for ( DockerImage image : images ) {
            for ( String tag : image.getImageTags() ) {
                executePipeline(tag, image.getImageSha());
            }
        }
    }

    private void executePipeline(String tag, String sha) {
        String domain = _repo.getDomain();
        String repoId = _repo.getId();
        for ( PageIterator it : new PageIterator() ) {
            for ( Pipeline pipeline : _pipelineDb.listByContainerRepoId(domain, repoId, it) ) {
                pipeline = _pipelineDb.getPipeline(pipeline.getId());
                _runPipeline.runPipeline(pipeline, _repo, tag, sha);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + ": "+_repo;
    }
}
