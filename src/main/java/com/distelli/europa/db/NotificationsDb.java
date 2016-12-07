/*
  $Id: $
  @file NotificationsDb.java
  @brief Contains the NotificationsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.List;
import java.util.UUID;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.Index;
import com.distelli.persistence.PageIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;

import com.distelli.europa.models.*;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NotificationsDb
{
    private static final Logger log = Logger.getLogger(NotificationsDb.class);

    private Index<Notification> _main;
    private static final ObjectMapper om = new ObjectMapper();

    private static TransformModule createTransforms(TransformModule module) {
        module.createTransform(Notification.class)
        .put("hk", String.class,
             (item) -> getHashKey(item.getRepoProvider(), item.getRegion(), item.getRepoName()))
        .put("rk", String.class,
             (item) -> getRangeKey(item.getType(), item.getId()))

        .put("id", String.class, "id")
        .put("type", NotificationType.class, "type")
        .put("region", String.class, "region")
        .put("rp", RegistryProvider.class, "repoProvider")
        .put("rnam", String.class, "repoName")
        .put("sec", String.class, "secret")
        .put("tgt", String.class, "target");
        return module;
    }


    private static final String getHashKey(RegistryProvider repoProvider, String region, String repoName)
    {
        return String.format("%s:%s:%s",
                             repoProvider.toString().toLowerCase(),
                             region.toLowerCase(),
                             repoName.toLowerCase());
    }

    private static final String getRangeKey(NotificationType type, String id)
    {
        return String.format("%s:%s",
                             type.toString().toLowerCase(),
                             id.toLowerCase());
    }

    @Inject
    public NotificationsDb(Index.Factory indexFactory, ConvertMarker.Factory convertMarkerFactory) {
        om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(Notification.class)
        .withTableName("notifications")
        .withNoEncrypt("hk", "rk")
        .withHashKeyName("hk")
        .withRangeKeyName("rk")
        .withConvertValue(om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "rk"))
        .build();
    }

    public void save(Notification notification)
    {
        String id = notification.getId();
        if(id == null)
            notification.setId(UUID.randomUUID().toString());
        _main.putItem(notification);
    }

    public void deleteNotification(RegistryProvider repoProvider, String region, String repoName, NotificationType type, String id)
    {
        _main.deleteItem(getHashKey(repoProvider, region, repoName),
                         getRangeKey(type, id));
    }

    public List<Notification> listNotifications(RegistryProvider repoProvider, String region, String repoName, PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(repoProvider, region, repoName), pageIterator).list();
    }
}
