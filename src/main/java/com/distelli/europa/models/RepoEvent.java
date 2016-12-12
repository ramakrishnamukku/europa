package com.distelli.europa.models;

import org.apache.log4j.Logger;

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
}
