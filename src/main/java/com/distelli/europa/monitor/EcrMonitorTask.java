/*
  $Id: $
  @file EcrMonitorTask.java
  @brief Contains the EcrMonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;

import com.distelli.europa.clients.*;
import com.distelli.europa.models.*;
import com.distelli.persistence.PageIterator;
import com.google.inject.assistedinject.Assisted;
import lombok.extern.log4j.Log4j;

@Log4j
public class EcrMonitorTask extends RepoMonitorTask
{
    public interface Factory {
        public EcrMonitorTask create(ContainerRepo repo,
                                     CountDownLatch latch);
    }

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
            log.debug("Monitoring ECR Repo: "+_repo);
        if(_ecrClient == null)
            initEcrClient();

        
        Map<String, DockerImageId> imageTags = listImageTags();

        // Filter out the changes:
        for ( String removed : findChanges(imageTags, DockerImageId::getSha) ) {
            imageTags.put(removed, DockerImageId.builder()
                          .tag(removed)
                          .build());
        }

        // Transform into DockerImage objects, and save them:
        saveNewEvents(
            saveManifestChanges(
                toDockerImages(imageTags.values())));
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

    private Map<String, DockerImageId> listImageTags()
    {
        Map<String, DockerImageId> images = new HashMap<>();
        if(_ecrClient == null)
            return images;

        PageIterator iter = new PageIterator().pageSize(100);
        if(log.isDebugEnabled())
            log.debug("Listing images from ECR repo: "+_repo);
        do {
            for ( DockerImageId imageId : _ecrClient.listImages(_repo, iter) ) {
                images.put(imageId.getTag(), imageId);
            }
        } while(iter.getMarker() != null);


        if(log.isDebugEnabled())
            log.debug("Found "+images.size()+" images in ECR repo: "+_repo);
        return images;
    }

    private List<DockerImage> toDockerImages(Collection<DockerImageId> imageIds) {
        List<DockerImage> images = new ArrayList<>();
        if ( imageIds.size() < 1 ) return images;
        PageIterator pageIterator = new PageIterator().pageSize(100);
        do {
            // TODO: Do I need to deal with "deleted" tags specially?
            images.addAll(_ecrClient.describeImages(_repo, imageIds, pageIterator));
        } while(pageIterator.getMarker() != null);
        return images;
    }

    protected List<DockerImage> describeImages(List<DockerImageId> images, PageIterator pageIterator)
    {
        return _ecrClient.describeImages(_repo, images, pageIterator);
    }
}
