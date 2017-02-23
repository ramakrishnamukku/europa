package com.distelli.europa.monitor;

import com.distelli.europa.clients.DockerHubClient;
import com.distelli.europa.models.DockerHubRepoTag;
import com.distelli.europa.models.DockerImage;
import com.distelli.europa.models.RegistryCred;
import com.distelli.europa.models.ContainerRepo;
import com.distelli.persistence.PageIterator;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.extern.log4j.Log4j;

@Log4j
public class DockerHubMonitorTask extends RepoMonitorTask
{
    public interface Factory {
        public DockerHubMonitorTask create(ContainerRepo repo);
    }

    @Inject
    private Provider<DockerHubClient.Builder> _dhClientBuilderProvider;

    @Inject
    public DockerHubMonitorTask(@Assisted ContainerRepo repo)
    {
        super(repo);
    }

    private DockerHubClient getClient() {
        RegistryCred cred = _registryCredsDb.getCred(_repo.getDomain(),
                                                             _repo.getCredId());
        if(cred == null) {
            log.error("Failed to find RegistryCred for Repo: "+_repo);
            return null;
        }
        return _dhClientBuilderProvider.get()
            .credentials(cred.getUsername(), cred.getPassword())
            .build();
    }

    public void monitor() throws Exception
    {
        DockerHubClient client = getClient();
        if ( null == client ) return;

        /**
           1. List all images from the repo - List<DockerImageId>
           2. List all the image events already stored
           3. Order the images not stored by time
           4. Store the new images
        */
        if(log.isDebugEnabled())
            log.debug("Monitoring DOCKERHUB Repo: "+_repo);

        Map<String, DockerHubRepoTag> imageTags = listImageTags(client);

        // Filter out the changes:
        for ( String removed : findChanges(imageTags, DockerHubRepoTag::getDigest) ) {
            imageTags.put(removed, DockerHubRepoTag.builder()
                          .tag(removed)
                          .build());
        }

        // Transform into DockerImage objects, and save them:
        saveChanges(toDockerImages(imageTags.values()));
    }

    private Map<String, DockerHubRepoTag> listImageTags(DockerHubClient client) throws Exception
    {
        Map<String, DockerHubRepoTag> images = new LinkedHashMap<>();

        for ( PageIterator iter : new PageIterator().pageSize(100) ) {
            for ( DockerHubRepoTag repoTag : client.listRepoTags(_repo.getName(), iter) ) {
                images.put(repoTag.getTag(), repoTag);
            }
        }

        if(log.isDebugEnabled())
            log.debug("Found "+images.size()+" images in ECR repo: "+_repo);
        return images;
    }

    private List<DockerImage> toDockerImages(Collection<DockerHubRepoTag> repoTags) {
        Map<String, DockerImage> imagesBySha = new LinkedHashMap<String, DockerImage>();
        for ( DockerHubRepoTag repoTag : repoTags ) {
            DockerImage image = imagesBySha.get(repoTag.getDigest());
            if ( null == image ) {
                image = DockerImage.builder()
                    .imageSha(repoTag.getDigest())
                    .pushTime(repoTag.getPushTime())
                    .imageSize(repoTag.getSize())
                    .imageTag(repoTag.getTag())
                    .build();
                imagesBySha.put(repoTag.getDigest(), image);
            } else {
                if(image.getPushTime() == null || (repoTag.getPushTime() != null && image.getPushTime() > repoTag.getPushTime()))
                {
                    // Use the oldest push time:
                    image.setPushTime(repoTag.getPushTime());
                }
                image.addImageTag(repoTag.getTag());
            }
        }
        return new ArrayList<>(imagesBySha.values());
    }
}
