/*
  $Id: $
  @file StorageSettings.java
  @brief Contains the StorageSettings.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.util.ArrayList;
import java.util.List;

import com.distelli.europa.Constants;
import com.distelli.objectStore.ObjectStoreType;
import com.distelli.webserver.AjaxClientException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageSettings
{
    public static final String SETTING_OS_TYPE = "osType";
    public static final String SETTING_OS_ENDPOINT = "osEndpoint";
    public static final String SETTING_OS_BUCKET = "osBucket";
    public static final String SETTING_OS_CRED_KEY = "osCredKey";
    public static final String SETTING_OS_CRED_SECRET = "osCredSecret";
    public static final String SETTING_OS_DISK_STORAGE_ROOT = "osDiskRoot";
    public static final String SETTING_OS_PATH_PREFIX = "osPathPrefix";

    protected ObjectStoreType osType;
    protected String osEndpoint;
    protected String osCredKey;
    protected String osCredSecret;
    protected String osBucket;
    protected String osDiskRoot;
    protected String osPathPrefix;

    private static final String DEFAULT_DISK_BUCKET = "default";

    public static StorageSettings fromEuropaSettings(List<EuropaSetting> settings)
    {
        if(settings == null || settings.size() == 0)
            return null;
        StorageSettings storageSettings = new StorageSettings();
        for(EuropaSetting setting : settings)
        {
            String key = setting.getKey();
            if(key.equalsIgnoreCase(StorageSettings.SETTING_OS_TYPE))
            {
                String value = setting.getValue();
                if(value == null)
                    throw(new RuntimeException("Missing value for EuropaSetting key: "+key));
                ObjectStoreType objectStoreType = null;
                try {
                    storageSettings.setOsType(ObjectStoreType.valueOf(value.toUpperCase()));
                } catch(Throwable t) {
                    throw(new RuntimeException("Illegal value for config: objectStore.type. Expected S3 or DISK"));
                }
            }
            else if(key.equalsIgnoreCase(StorageSettings.SETTING_OS_ENDPOINT))
                storageSettings.setOsEndpoint(setting.getValue());
            else if(key.equalsIgnoreCase(StorageSettings.SETTING_OS_BUCKET))
                storageSettings.setOsBucket(setting.getValue());
            else if(key.equalsIgnoreCase(StorageSettings.SETTING_OS_CRED_KEY))
                storageSettings.setOsCredKey(setting.getValue());
            else if(key.equalsIgnoreCase(StorageSettings.SETTING_OS_CRED_SECRET))
                storageSettings.setOsCredSecret(setting.getValue());
            else if(key.equalsIgnoreCase(StorageSettings.SETTING_OS_DISK_STORAGE_ROOT))
                storageSettings.setOsDiskRoot(setting.getValue());
            else if(key.equalsIgnoreCase(StorageSettings.SETTING_OS_PATH_PREFIX))
                storageSettings.setOsPathPrefix(setting.getValue());
        }

        if(storageSettings.getOsType() == ObjectStoreType.DISK && storageSettings.getOsBucket() == null)
            storageSettings.setOsBucket(DEFAULT_DISK_BUCKET);

        return storageSettings;
    }

    public void validate()
    {
        if(this.osType == null)
            throw(new AjaxClientException("Invalid or Missing field: osType in content"));
        switch(this.osType)
        {
        case S3:
            if(this.osBucket == null)
                throw(new AjaxClientException("Invalid or Missing field: osBucket in content"));
            if(this.osEndpoint == null)
                throw(new AjaxClientException("Invalid or Missing field: osEndpoint in content"));
            if(this.osCredKey == null)
                throw(new AjaxClientException("Invalid or Missing field: osCredKey in content"));
            if(this.osCredSecret == null)
                throw(new AjaxClientException("Invalid or Missing field: osCredSecret in content"));
            break;
        case DISK:
            if(this.osDiskRoot == null)
                throw(new AjaxClientException("Invalid or Missing field: osDiskRoot in content"));
            if(this.osBucket == null)
                this.osBucket = DEFAULT_DISK_BUCKET;
            break;
        }
    }

    public List<EuropaSetting> toEuropaSettings()
    {
        validate();
        List<EuropaSetting> settingsList = new ArrayList<EuropaSetting>();
        settingsList.add(toSetting(StorageSettings.SETTING_OS_TYPE, this.osType));
        switch(this.osType)
        {
        case S3:
            settingsList.add(toSetting(SETTING_OS_BUCKET, this.osBucket));
            settingsList.add(toSetting(SETTING_OS_ENDPOINT, this.osEndpoint));
            settingsList.add(toSetting(SETTING_OS_CRED_KEY, this.osCredKey));
            settingsList.add(toSetting(SETTING_OS_CRED_SECRET, this.osCredSecret));
            break;
        case DISK:
            settingsList.add(toSetting(SETTING_OS_DISK_STORAGE_ROOT, this.osDiskRoot));
            if(this.osBucket == null)
                this.osBucket = DEFAULT_DISK_BUCKET;
            settingsList.add(toSetting(SETTING_OS_BUCKET, this.osBucket));
            break;
        }

        if(osPathPrefix != null)
            settingsList.add(toSetting(SETTING_OS_PATH_PREFIX, this.osPathPrefix));
        return settingsList;
    }

    public static EuropaSetting toSetting(String key, ObjectStoreType value)
    {
        return toSetting(key, value.toString());
    }

    public static EuropaSetting toSetting(String key, String value)
    {
        return EuropaSetting
        .builder()
        .domain(Constants.DOMAIN_ZERO)
        .key(key)
        .value(value)
        .type(EuropaSettingType.STORAGE)
        .build();
    }
}
