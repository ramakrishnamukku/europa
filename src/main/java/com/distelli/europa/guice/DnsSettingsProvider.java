/*
  $Id: $
  @file DnsSettingsProvider.java
  @brief Contains the DnsSettingsProvider.java class

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
import com.distelli.europa.models.DnsSettings;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class DnsSettingsProvider implements Provider<DnsSettings>
{
    @Inject
    private SettingsDb _settingsDb;

    private DnsSettings _dnsSettings;

    public DnsSettingsProvider()
    {

    }

    public synchronized void refresh()
    {
        _dnsSettings = null;
    }

    public synchronized DnsSettings get()
    {
        if(_dnsSettings != null)
            return _dnsSettings;
        List<EuropaSetting> settings = _settingsDb.listRootSettingsByType(EuropaSettingType.DNS);
        _dnsSettings = DnsSettings.fromEuropaSettings(settings);
        return _dnsSettings;
    }
}
