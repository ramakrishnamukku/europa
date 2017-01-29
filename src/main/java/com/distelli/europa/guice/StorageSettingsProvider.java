/*
  $Id: $
  @file StorageSettingsProvider.java
  @brief Contains the StorageSettingsProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.models.EuropaSetting;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.europa.models.StorageSettings;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class StorageSettingsProvider implements Provider<StorageSettings>
{
    @Inject
    private SettingsDb _settingsDb;

    private StorageSettings _storageSettings;

    public StorageSettingsProvider()
    {

    }

    public synchronized StorageSettings get()
    {
        if(_storageSettings != null)
            return _storageSettings;
        List<EuropaSetting> settings = _settingsDb.listRootSettingsByType(EuropaSettingType.STORAGE);
        if(settings == null || settings.size() == 0)
            return null;
        _storageSettings = StorageSettings.fromEuropaSettings(settings);
        return _storageSettings;
    }
}
