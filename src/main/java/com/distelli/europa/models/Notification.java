package com.distelli.europa.models;

import lombok.*;

@Data
public class Notification
{
    protected String id = null;
    protected String region = null;
    protected String repoName = null;
    protected RegistryProvider repoProvider = null;
    protected String secret = null;
    protected String target = null;
    protected NotificationType type = null;

    @Builder
    public Notification()
    {

    }
}
