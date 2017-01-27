/*
  $Id: $
  @file GetStorageSettings.java
  @brief Contains the GetStorageSettings.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.models.EuropaSetting;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.europa.models.StorageSettings;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonError;
import com.distelli.webserver.JsonSuccess;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class GetStorageSettings extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private SettingsDb _settingsDb;

    public GetStorageSettings()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        List<EuropaSetting> settings = _settingsDb.listRootSettingsByType(EuropaSettingType.STORAGE);
        if(settings == null || settings.size() == 0)
            return null;
        StorageSettings storageSettings = StorageSettings.fromEuropaSettings(settings);
        storageSettings.setOsCredSecret(null);
        return storageSettings;
    }
}
