package com.distelli.europa.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification
{
    protected String domain = null;
    protected String id = null;
    protected String repoId = null;
    protected String region = null;
    protected String repoName = null;
    protected RegistryProvider repoProvider = null;
    protected String secret = null;
    protected String target = null;
    protected NotificationType type = null;
}
