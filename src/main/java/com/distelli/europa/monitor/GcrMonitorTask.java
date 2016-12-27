/*
  $Id: $
  @file GcrMonitorTask.java
  @brief Contains the GcrMonitorTask.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import com.distelli.europa.models.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.google.inject.assistedinject.Assisted;
import com.distelli.persistence.PageIterator;

public class GcrMonitorTask extends RepoMonitorTask
{
    private static final Logger log = Logger.getLogger(GcrMonitorTask.class);
    private GcrClient _gcrClient = null;

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
        if(log.isDebugEnabled())
            log.debug("Monitoring GCR repo: "+_repo);

        if(_gcrClient == null)
            initGcrClient();
        List<DockerImageId> images = listImages();
        List<RepoEvent> events = listEvents();
        List<DockerImageId> imagesToSave = getNewImages(images, events);
        saveNewEvents(imagesToSave);
    }

    private void initGcrClient()
    {
        RegistryCred registryCred = _registryCredsDb.getCred(_repo.getDomain(),
                                                             _repo.getCredId());
        if(registryCred == null) {
            log.error("Failed to find RegistryCred for Repo: "+_repo);
            return;
        }

        GcrCredentials gcrCreds = new GcrServiceAccountCredentials(registryCred.getSecret());
        GcrRegion gcrRegion = GcrRegion.getRegion(registryCred.getRegion());
        _gcrClient = new GcrClient(gcrCreds, gcrRegion);
    }

    private List<DockerImageId> listImages()
    {
        List<DockerImageId> imageIdList = new ArrayList<DockerImageId>();
        if(_gcrClient == null)
            return imageIdList;

        GcrIterator iter = GcrIterator.builder().pageSize(100).build();
        if(log.isDebugEnabled())
            log.debug("Listing images from GCR repo: "+_repo);
        do {
            try {
                List<GcrImageTag> imageTags = _gcrClient.listImageTags(_repo.getName(), iter);
                for(GcrImageTag imgTag : imageTags)
                {
                    DockerImageId imageId = DockerImageId
                    .builder()
                    .tag(imgTag.getTag())
                    .sha(imgTag.getSha())
                    .pushTime(imgTag.getCreated())
                    .repoUri(_repo.getRepoUri())
                    .build();
                    imageIdList.add(imageId);
                }
            } catch(Throwable t) {
                throw(new RuntimeException(t));
            }
        } while(iter.getMarker() != null);

        if(log.isDebugEnabled())
            log.debug("Found "+imageIdList.size()+" images in repo: "+_repo);
        return imageIdList;
    }

    protected List<DockerImage> describeImages(List<DockerImageId> images, PageIterator pageIterator)
    {
        Map<String, DockerImage> imagesBySha = new HashMap<String, DockerImage>();
        List<DockerImage> imageList = new ArrayList<DockerImage>();
        for(DockerImageId imageId : images)
        {
            String imageSha = imageId.getSha();
            if(imageSha != null && imageSha.startsWith("sha256:"))
                imageSha = imageSha.substring("sha256:".length());

            DockerImage image = null;
            image = imagesBySha.get(imageSha);
            if(image == null)
            {
                image = DockerImage
                .builder()
                .imageSha(imageSha)
                .pushTime(imageId.getPushTime())
                .imageSize(null) //GCR doesn't seem to give us the image size
                .imageTag(imageId.getTag())
                .build();
                imagesBySha.put(imageSha, image);
                imageList.add(image);
            } else {
                image.addImageTag(imageId.getTag());
            }
        }
        return imageList;
    }
}
