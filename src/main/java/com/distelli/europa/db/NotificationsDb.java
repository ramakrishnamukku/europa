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
import com.distelli.europa.EuropaConfiguration;

import com.distelli.europa.Constants;
import com.distelli.europa.models.*;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NotificationsDb
{
    private static final Logger log = Logger.getLogger(NotificationsDb.class);

    private Index<Notification> _main;

    private final ObjectMapper _om = new ObjectMapper();
    private EuropaConfiguration _europaConfiguration;

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(Notification.class)
        .put("hk", String.class,
             (item) -> getHashKey(item.getDomain(), item.getRepoId()))
        .put("rk", String.class,
             (item) -> getRangeKey(item.getType(), item.getId()))
        .put("id", String.class, "id")
        .put("repoId", String.class, "repoId")
        .put("domain", String.class, "domain")
        .put("type", NotificationType.class, "type")
        .put("region", String.class, "region")
        .put("rp", RegistryProvider.class, "repoProvider")
        .put("rnam", String.class, "repoName")
        .put("sec", String.class, "secret")
        .put("tgt", String.class, "target");
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

    private final String getRangeKey(NotificationType type, String id)
    {
        return String.format("%s:%s",
                             type.toString().toLowerCase(),
                             id.toLowerCase());
    }

    @Inject
    public NotificationsDb(Index.Factory indexFactory,
                           ConvertMarker.Factory convertMarkerFactory,
                           EuropaConfiguration europaConfiguration) {
        _europaConfiguration = europaConfiguration;
        _om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(Notification.class)
        .withTableName("notifications")
        .withNoEncrypt("hk", "rk")
        .withHashKeyName("hk")
        .withRangeKeyName("rk")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "rk"))
        .build();
    }

    public void save(Notification notification)
    {
        String id = notification.getId();
        if(id == null)
            notification.setId(UUID.randomUUID().toString());
        if(notification.getRepoId() == null)
            throw(new IllegalArgumentException("Invalid null repo Id in notification: "+notification));
        if(_europaConfiguration.isMultiTenant() && notification.getDomain() == null)
            throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for Notification: "+
                                               notification));
        _main.putItem(notification);
    }

    public void deleteNotification(String domain, String repoId, NotificationType type, String id)
    {
        _main.deleteItem(getHashKey(domain, repoId),
                         getRangeKey(type, id));
    }

    public List<Notification> listNotifications(String domain, String repoId, PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(domain, repoId), pageIterator).list();
    }
}
