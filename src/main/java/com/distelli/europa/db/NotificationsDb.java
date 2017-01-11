/*
  $Id: $
  @file NotificationsDb.java
  @brief Contains the NotificationsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

import com.distelli.europa.Constants;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.models.*;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class NotificationsDb extends BaseDb
{
    private Index<Notification> _main;
    private Index<Notification> _byRepo;

    private final ObjectMapper _om = new ObjectMapper();
    private EuropaConfiguration _europaConfiguration;

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName("notifications")
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("hk", AttrType.STR))
                    .rangeKey(attr("id", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName("hk-rid-index")
                    .hashKey(attr("hk", AttrType.STR))
                    .rangeKey(attr("rid", AttrType.STR))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(Notification.class)
        .put("hk", String.class,
             (item) -> getHashKey(item.getDomain()))
        .put("id", String.class,
             (item) -> item.getId().toLowerCase(),
             (item, id) -> item.setId(id.toLowerCase()))
        .put("rid", String.class, "repoId")
        .put("domain", String.class, "domain")
        .put("type", NotificationType.class, "type")
        .put("region", String.class, "region")
        .put("rp", RegistryProvider.class, "repoProvider")
        .put("rnam", String.class, "repoName")
        .put("sec", String.class, "secret")
        .put("tgt", String.class, "target");
        return module;
    }

    private final String getHashKey(String domain)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(domain == null)
                throw(new IllegalArgumentException("Invalid null domain for multi-tenant setup"));
        }
        else
            domain = Constants.DOMAIN_ZERO;
        return domain.toLowerCase();
    }

    @Inject
    public NotificationsDb(Index.Factory indexFactory,
                           ConvertMarker.Factory convertMarkerFactory,
                           EuropaConfiguration europaConfiguration) {
        _europaConfiguration = europaConfiguration;
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(Notification.class)
        .withTableName("notifications")
        .withNoEncrypt("hk", "id", "repoId")
        .withHashKeyName("hk")
        .withRangeKeyName("id")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "id"))
        .build();

        _byRepo = indexFactory.create(Notification.class)
        .withIndexName("notifications", "hk-rid-index")
        .withNoEncrypt("hk", "id", "repoId")
        .withHashKeyName("hk")
        .withRangeKeyName("rid")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "rid", "repoId"))
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

    public void deleteNotification(String domain, String id)
    {
        _main.deleteItem(getHashKey(domain),
                         id.toLowerCase());
    }

    public List<Notification> listNotifications(String domain, String repoId, PageIterator pageIterator)
    {
        return _byRepo.queryItems(getHashKey(domain), pageIterator)
        .eq(repoId.toLowerCase())
        .list();
    }
}
