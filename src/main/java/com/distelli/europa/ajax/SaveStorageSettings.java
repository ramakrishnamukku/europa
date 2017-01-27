/*
  $Id: $
  @file SaveStorageSettings.java
  @brief Contains the SaveStorageSettings.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.models.EuropaSetting;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.europa.models.StorageSettings;
import com.distelli.europa.util.FieldValidator;
import com.distelli.objectStore.ObjectStoreType;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonError;
import com.distelli.webserver.JsonSuccess;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class SaveStorageSettings extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private SettingsDb _settingsDb;

    public SaveStorageSettings()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        StorageSettings storageSettings = ajaxRequest.convertContent(StorageSettings.class,
                                                                     true); //throw if null
        FieldValidator.validateNonNull(storageSettings,
                                       StorageSettings.SETTING_OS_TYPE);

        List<EuropaSetting> settingsList = new ArrayList<EuropaSetting>();
        String osType = storageSettings.getOsType();
        ObjectStoreType objectStoreType = null;
        try {
            objectStoreType = ObjectStoreType.valueOf(osType.toUpperCase());
        } catch(Throwable t) {
            throw(new AjaxClientException("Invalid value for objectStoreType", JsonError.Codes.BadContent, 400));
        }

        settingsList.add(toSetting(StorageSettings.SETTING_OS_TYPE, storageSettings.getOsType()));

        switch(objectStoreType)
        {
        case S3:
            FieldValidator.validateNonNull(storageSettings,
                                           StorageSettings.SETTING_OS_BUCKET,
                                           StorageSettings.SETTING_OS_ENDPOINT,
                                           StorageSettings.SETTING_OS_CRED_KEY,
                                           StorageSettings.SETTING_OS_CRED_SECRET);

            settingsList.add(toSetting(StorageSettings.SETTING_OS_BUCKET, storageSettings.getOsBucket()));
            settingsList.add(toSetting(StorageSettings.SETTING_OS_ENDPOINT, storageSettings.getOsEndpoint()));
            settingsList.add(toSetting(StorageSettings.SETTING_OS_CRED_KEY, storageSettings.getOsCredKey()));
            settingsList.add(toSetting(StorageSettings.SETTING_OS_CRED_SECRET, storageSettings.getOsCredSecret()));
            break;
        case DISK:
            FieldValidator.validateNonNull(storageSettings,
                                           StorageSettings.SETTING_OS_DISK_STORAGE_ROOT);
            settingsList.add(toSetting(StorageSettings.SETTING_OS_DISK_STORAGE_ROOT, storageSettings.getOsDiskRoot()));
            String bucket = storageSettings.getOsBucket();
            if(bucket == null)
                bucket = "default";
            settingsList.add(toSetting(StorageSettings.SETTING_OS_BUCKET, bucket));
            break;
        }

        String pathPrefix = storageSettings.getOsPathPrefix();
        if(pathPrefix != null)
            settingsList.add(toSetting(StorageSettings.SETTING_OS_PATH_PREFIX, pathPrefix));
        for(EuropaSetting setting : settingsList)
            _settingsDb.save(setting);
        return JsonSuccess.Success;
    }

    private EuropaSetting toSetting(String key, String value)
    {
        return EuropaSetting
        .builder()
        .key(key)
        .value(value)
        .type(EuropaSettingType.STORAGE)
        .build();
    }

}
