package com.distelli.europa.models;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.Singular;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
    protected List<String> notifications = null;
    @Singular
    protected List<String> imageTags = null;
    protected String imageSha = null;
    protected String imageManifestId = null;
}
