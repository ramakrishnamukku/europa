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
    protected RegistryManifestDb _manifestDb = null;
    @Inject
    protected ContainerRepoDb _containerRepoDb;

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
        _repo.setLastSyncTime(System.currentTimeMillis());
        _containerRepoDb.setLastSyncTime(_repo.getDomain(), _repo.getId(), _repo.getLastSyncTime());
    }

    @Override
    public String toString() {
        return getClass().getName() + ": "+_repo;
    }
}
