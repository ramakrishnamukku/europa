package com.distelli.europa.models;

import org.apache.log4j.Logger;

import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoEvent
{
    protected String domain = null;
    protected String repoId = null;
    protected String id = null;
    protected RepoEventType eventType;
    protected Long eventTime = null;
    protected Long imageSize = null;
    @Singular
    protected List<String> imageTags = null;
    protected String imageSha = null;
    protected String imageManifestId = null;
}
