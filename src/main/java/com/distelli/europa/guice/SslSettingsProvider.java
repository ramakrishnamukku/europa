/*
  $Id: $
  @file SslSettingsProvider.java
  @brief Contains the SslSettingsProvider.java class

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
import com.distelli.europa.models.SslSettings;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class SslSettingsProvider implements Provider<SslSettings>
{
    @Inject
    private SettingsDb _settingsDb;

    private SslSettings _sslSettings;

    public SslSettingsProvider()
    {

    }

    public synchronized SslSettings get()
    {
        if(_sslSettings != null)
            return _sslSettings;
        List<EuropaSetting> settings = _settingsDb.listRootSettingsByType(EuropaSettingType.SSL);
        _sslSettings = SslSettings.fromEuropaSettings(settings);
        return _sslSettings;
    }
}
