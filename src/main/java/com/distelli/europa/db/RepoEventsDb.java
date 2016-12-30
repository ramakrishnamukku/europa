/*
  $Id: $
  @file RepoEventsDb.java
  @brief Contains the RepoEventsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

import com.distelli.europa.Constants;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.models.*;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.Index;
import com.distelli.persistence.PageIterator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RepoEventsDb
{
    private Index<RepoEvent> _main;
    private Index<RepoEvent> _byTime;

    private EuropaConfiguration _europaConfiguration;
    private final ObjectMapper _om = new ObjectMapper();

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(RepoEvent.class)
        .put("hk", String.class,
             (item) -> getHashKey(item.getDomain(), item.getRepoId()))
        .put("id", String.class,
             (item) -> item.getId(),
             (item, id) -> item.setId(id))
        .put("domain", String.class, "domain")
        .put("repoId", String.class, "repoId")
        .put("type", RepoEventType.class, "eventType")
        .put("size", Long.class, "imageSize")
        .put("sha", String.class, "imageSha")
        .put("tags", new TypeReference<List<String>>(){}, "imageTags")
        .put("ntfs", new TypeReference<List<String>>(){}, "notifications")
        .put("mid", String.class, "imageManifestId")
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

    @Inject
    public RepoEventsDb(Index.Factory indexFactory,
                        ConvertMarker.Factory convertMarkerFactory,
                        EuropaConfiguration europaConfiguration) {
        _europaConfiguration = europaConfiguration;
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(RepoEvent.class)
        .withTableName("events")
        .withNoEncrypt("hk", "id", "etime")
        .withHashKeyName("hk")
        .withRangeKeyName("id")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "id"))
        .build();

        _byTime = indexFactory.create(RepoEvent.class)
        .withIndexName("events", "etime-index")
        .withNoEncrypt("hk", "id", "etime")
        .withHashKeyName("hk")
        .withRangeKeyName("etime")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "etime"))
        .build();
    }

    public void save(RepoEvent repoEvent)
    {
        String id = repoEvent.getId();
        if(id == null)
            id = UUID.randomUUID().toString().toLowerCase();
        else
            id = id.toLowerCase();
        repoEvent.setId(id);
        if(repoEvent.getRepoId() == null)
            throw(new IllegalArgumentException("Invalid null repo Id in repoEvent: "+repoEvent));
        if(_europaConfiguration.isMultiTenant() && repoEvent.getDomain() == null)
            throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for RepoEvent: "+
                                               repoEvent));
        _main.putItem(repoEvent);
    }

    public List<RepoEvent> listEvents(String domain, String repoId, PageIterator pageIterator)
    {
        return _byTime.queryItems(getHashKey(domain, repoId), pageIterator).list();
    }

    public RepoEvent getEventById(String domain, String repoId, String eventId) {
        return _main.getItem(getHashKey(domain, repoId),
                             eventId.toLowerCase());
    }

    public void setNotifications(String domain, String repoId, String eventId, List<String> notifications) {
        _main.updateItem(getHashKey(domain, repoId),
                         eventId.toLowerCase())
        .set("ntfs", AttrType.LIST, notifications)
        .when((expr) -> expr.eq("id", eventId.toLowerCase()));
    }
}
