/*
  $Id: $
  @file ImagePushWebhookContent.java
  @brief Contains the ImagePushWebhookContent.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.distelli.europa.models.*;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagePushWebhookContent
{
    public static final String EVENT_NAME = "DockerImagePush";

    protected String event = EVENT_NAME;
    protected Image image = null;
    protected Repository repository = null;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        protected List<String> tags;
        protected String sha;
        protected Long size;
        protected String pushed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Repository {
        protected String name;
        protected String region;
        protected String provider;
    }

    public void setImage(DockerImage dockerImage)
    {
        String pushTime = "";
        Long pushTimeLong = dockerImage.getPushTime();
        if(pushTimeLong != null) {
            pushTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withZone(ZoneId.of("UTC"))
            .format(Instant.ofEpochMilli(pushTimeLong.longValue()));
        }

        this.image = Image
        .builder()
        .tags(dockerImage.getImageTags())
        .sha(dockerImage.getImageSha())
        .size(dockerImage.getImageSize())
        .pushed(pushTime)
        .build();
    }

    public void setRepository(ContainerRepo repository)
    {
        String repoProvider = null;
        RegistryProvider provider = repository.getProvider();
        if(provider != null)
            repoProvider = provider.toString();
        this.repository = Repository
        .builder()
        .name(repository.getName())
        .region(repository.getRegion())
        .provider(repoProvider)
        .build();
    }
}
