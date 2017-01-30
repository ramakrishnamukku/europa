/*
  $Id: $
  @file UpdateStorageCreds.java
  @brief Contains the UpdateStorageCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.models.EuropaSetting;
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
public class UpdateStorageCreds extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private SettingsDb _settingsDb;

    @Inject
    private Provider<StorageSettings> _storageSettingsProvider;

    private static final JsonError StorageNotInitialized = new JsonError("Storage is not Initialized", "StorageNotInitialized", 400);

    public UpdateStorageCreds()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        StorageSettings storageSettings = _storageSettingsProvider.get();
        if(storageSettings == null)
            throw(new AjaxClientException(StorageNotInitialized));

        if(storageSettings.getOsType() != ObjectStoreType.S3)
            throw(new AjaxClientException("Creds cannot be set for Storage Type: "+storageSettings.getOsType(),
                                          "InvalidStorageType",
                                          400));

        storageSettings = ajaxRequest.convertContent(StorageSettings.class,
                                                     true); //throw if null
        FieldValidator.validateNonNull(storageSettings,
                                       StorageSettings.SETTING_OS_CRED_KEY,
                                       StorageSettings.SETTING_OS_CRED_SECRET);

        List<EuropaSetting> europaSettings = storageSettings.toEuropaSettings();
        europaSettings.add(StorageSettings.toSetting(StorageSettings.SETTING_OS_CRED_KEY,
                                                     storageSettings.getOsCredKey()));
        europaSettings.add(StorageSettings.toSetting(StorageSettings.SETTING_OS_CRED_SECRET,
                                                     storageSettings.getOsCredSecret()));
        for(EuropaSetting setting : europaSettings)
            _settingsDb.save(setting);
        return JsonSuccess.Success;
    }
}
