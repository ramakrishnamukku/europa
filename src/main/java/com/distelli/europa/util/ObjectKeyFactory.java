/*
  $Id: $
  @file ObjectKeyFactory.java
  @brief Contains the ObjectKeyFactory.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.util;

import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.EuropaConfiguration.ObjectStoreConfig;
import com.distelli.europa.notifiers.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j;

import com.distelli.objectStore.*;

@Log4j
@Singleton
public class ObjectKeyFactory
{
    private String _defaultBucket;
    private String _pathPrefix;

    public static final String WEBHOOKS_STORAGE_PREFIX = "webhooks";
    public static final String BLOBS_PREFIX = "blobs";

    @Inject
    public ObjectKeyFactory(EuropaConfiguration europaConfiguration)
    {
        ObjectStoreConfig config = europaConfiguration.getObjectStoreConfig();
        _defaultBucket = config.getBucket();
        _pathPrefix = config.getPathPrefix();
    }

    public String getDefaultBucket()
    {
        return _defaultBucket;
    }

    public String getPathPrefix()
    {
        return _pathPrefix;
    }

    public ObjectKey forWebhookRecord(NotificationId notificationId)
    {
        String key = null;
        if(_pathPrefix != null)
            key = String.format("%s/%s/%s",
                                _pathPrefix,
                                WEBHOOKS_STORAGE_PREFIX,
                                notificationId.getId());
        else
            key = String.format("%s/%s",
                                WEBHOOKS_STORAGE_PREFIX,
                                notificationId.getId());
        return ObjectKey.builder()
        .bucket(_defaultBucket)
        .key(key)
        .build();
    }

    public ObjectKey forRegistryBlobId(String blobId)
    {
        String key = null;
        if(_pathPrefix != null)
            key = String.format("%s/%s/%s",
                                _pathPrefix,
                                BLOBS_PREFIX,
                                blobId);
        else
            key = String.format("%s/%s",
                                BLOBS_PREFIX,
                                blobId);
        return ObjectKey.builder()
            .bucket(_defaultBucket)
            .key(key)
            .build();
    }
}
