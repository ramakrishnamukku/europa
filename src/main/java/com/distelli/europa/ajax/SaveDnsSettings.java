/*
  $Id: $
  @file SaveDnsSettings.java
  @brief Contains the SaveDnsSettings.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import com.distelli.europa.models.EuropaSetting;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.europa.models.DnsSettings;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.guice.DnsSettingsProvider;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonError;
import com.distelli.webserver.JsonSuccess;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class SaveDnsSettings extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private SettingsDb _settingsDb;
    @Inject
    private DnsSettingsProvider _dnsSettingsProvider;

    public SaveDnsSettings()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        DnsSettings dnsSettings = ajaxRequest.convertContent(DnsSettings.class,
                                                             true); //throw if null
        List<EuropaSetting> europaSettings = dnsSettings.toEuropaSettings();
        for(EuropaSetting setting : europaSettings)
            _settingsDb.save(setting);

        _dnsSettingsProvider.refresh();
        return JsonSuccess.Success;
    }
}
