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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Provider;

import com.distelli.europa.models.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.google.inject.assistedinject.Assisted;
import com.distelli.persistence.PageIterator;
import lombok.extern.log4j.Log4j;

@Log4j
public class GcrMonitorTask extends RepoMonitorTask
{
    private GcrClient _gcrClient = null;

    @Inject
    private Provider<GcrClient.Builder> _gcrClientBuilderProvider;

    public interface Factory {
        public GcrMonitorTask create(ContainerRepo repo);
    }

    @Inject
    public GcrMonitorTask(@Assisted ContainerRepo repo)
    {
        super(repo);
    }

    public void monitor()
    {
        if(log.isDebugEnabled())
            log.debug("Monitoring GCR repo: "+_repo);

        if(_gcrClient == null)
            initGcrClient();

        // Get all the image tags:
        Map<String, GcrImageTag> imageTags = listImageTags();

        // Filter out the changes:
        for ( String removed : findChanges(imageTags, GcrImageTag::getSha) ) {
            imageTags.put(removed, GcrImageTag.builder()
                          .tag(removed)
                          .created(System.currentTimeMillis())
                          .build());
        }

        List<DockerImage> dockerImages = toDockerImages(imageTags.values());

        saveNewEvents(
            saveManifestChanges(dockerImages));
    }

    private List<DockerImage> toDockerImages(Collection<GcrImageTag> imageTags) {
        Map<String, DockerImage> imagesBySha = new LinkedHashMap<String, DockerImage>();
        for ( GcrImageTag imageTag : imageTags ) {
            DockerImage image = imagesBySha.get(imageTag.getSha());
            if ( null == image ) {
                image = DockerImage.builder()
                    .imageSha(imageTag.getSha())
                    .pushTime(imageTag.getCreated())
                    .imageSize(null) // GCR doesn't have this :(.
                    .imageTag(imageTag.getTag())
                    .build();
                imagesBySha.put(imageTag.getSha(), image);
            } else {
                if ( image.getPushTime() > imageTag.getCreated() ) {
                    // Use the oldest push time:
                    image.setPushTime(imageTag.getCreated());
                }
                image.addImageTag(imageTag.getTag());
            }
        }
        return new ArrayList<>(imagesBySha.values());
    }

    private void initGcrClient()
    {
        RegistryCred registryCred = _registryCredsDb.getCred(_repo.getDomain(),
                                                             _repo.getCredId());
        if(registryCred == null) {
            log.error("Failed to find RegistryCred for Repo: "+_repo);
            return;
        }

        _gcrClient = _gcrClientBuilderProvider.get()
            .gcrCredentials(new GcrServiceAccountCredentials(registryCred.getSecret()))
            .gcrRegion(GcrRegion.getRegion(registryCred.getRegion()))
            .build();
    }

    private Map<String, GcrImageTag> listImageTags()
    {
        Map<String, GcrImageTag> images = new LinkedHashMap<>();
        if(_gcrClient == null)
            return images;

        GcrIterator iter = GcrIterator.builder().pageSize(100).build();
        if(log.isDebugEnabled())
            log.debug("Listing images from GCR repo: "+_repo);
        do {
            try {
                List<GcrImageTag> imageTags = _gcrClient.listImageTags(_repo.getName(), iter);
                for(GcrImageTag imgTag : imageTags)
                {
                    images.put(imgTag.getTag(), imgTag);
                }
            } catch(Throwable t) {
                throw(new RuntimeException(t));
            }
        } while(iter.getMarker() != null);

        if(log.isDebugEnabled())
            log.debug("Found "+images.size()+" images in GCR repo: "+_repo);
        return images;
    }
}
