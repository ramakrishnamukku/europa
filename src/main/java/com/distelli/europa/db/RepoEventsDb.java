/*
  $Id: $
  @file RepoEventsDb.java
  @brief Contains the RepoEventsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.UUID;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.Index;
import com.distelli.persistence.PageIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.europa.EuropaConfiguration;

import com.distelli.europa.Constants;
import com.distelli.europa.models.*;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RepoEventsDb
{
    private static final Logger log = Logger.getLogger(RepoEventsDb.class);

    private Index<RepoEvent> _main;

    private EuropaConfiguration _europaConfiguration;
    private final ObjectMapper _om = new ObjectMapper();

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(RepoEvent.class)
        .put("hk", String.class,
             (item) -> getHashKey(item.getDomain(), item.getRepoId()))
        .put("rk", String.class,
             (item) -> getRangeKey(item.getEventTime(), item.getId()))
        .put("domain", String.class, "domain")
        .put("repoId", String.class, "repoId")
        .put("id", String.class, "id")
        .put("type", RepoEventType.class, "eventType")
        .put("etime", Long.class, "eventTime");
        return module;
    }

    private final String getHashKey(String domain, String repoId)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(domain == null)
                throw(new IllegalArgumentException("Invalid null domain for multi-tenant setup"));
        }
        else
            domain = Constants.DOMAIN_ZERO;
        return String.format("%s:%s",
                             domain.toLowerCase(),
                             repoId.toLowerCase());
    }

    private final String getRangeKey(Long eventTime, String eventId)
    {
        return String.format("%019d:%s",
                             eventTime,
                             eventId.toLowerCase());
    }

    @Inject
    public RepoEventsDb(Index.Factory indexFactory,
                        ConvertMarker.Factory convertMarkerFactory,
                        EuropaConfiguration europaConfiguration) {
        _europaConfiguration = europaConfiguration;
        _om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(RepoEvent.class)
        .withTableName("events")
        .withNoEncrypt("hk", "rk")
        .withHashKeyName("hk")
        .withRangeKeyName("rk")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "rk"))
        .build();
    }

    public void save(RepoEvent repoEvent)
    {
        String id = repoEvent.getId();
        if(id == null)
            repoEvent.setId(UUID.randomUUID().toString());
        if(repoEvent.getRepoId() == null)
            throw(new IllegalArgumentException("Invalid null repo Id in repoEvent: "+repoEvent));
        if(_europaConfiguration.isMultiTenant() && repoEvent.getDomain() == null)
            throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for RepoEvent: "+
                                               repoEvent));
        _main.putItem(repoEvent);
    }

    public List<RepoEvent> listEvents(String domain, String repoId, PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(domain, repoId), pageIterator).list();
    }
}
