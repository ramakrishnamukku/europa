/*
  $Id: $
  @file DnsSettings.java
  @brief Contains the DnsSettings.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.distelli.europa.Constants;
import com.distelli.europa.EuropaRequestContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Log4j
public class DnsSettings
{
    private static final String SETTING_DNS_NAME = "dnsName";

    protected String dnsName;

    public static DnsSettings fromEuropaSettings(List<EuropaSetting> settings)
    {
        if(settings == null || settings.size() == 0)
            return null;
        DnsSettings dnsSettings = new DnsSettings();
        for(EuropaSetting setting : settings)
        {
            String key = setting.getKey();
            if(key.equalsIgnoreCase(SETTING_DNS_NAME))
                dnsSettings.setDnsName(setting.getValue());
        }
        return dnsSettings;
    }

    public static DnsSettings fromHostHeader(EuropaRequestContext requestContext)
    {
        String hostHeader = requestContext.getHostPort(null);
        DnsSettings dnsSettings = new DnsSettings();
        dnsSettings.setDnsName(hostHeader);
        return dnsSettings;
    }

    public static DnsSettings fromLocalAddress()
    {
        DnsSettings dnsSettings = new DnsSettings();
        String dnsName = null;
        try {
            dnsName = InetAddress.getLocalHost().getHostAddress();
        } catch(Throwable t) {
            log.error(t.getMessage(), t);
            dnsName = null;
        }
        dnsSettings.setDnsName(dnsName);
        return dnsSettings;
    }

    public List<EuropaSetting> toEuropaSettings()
    {
        List<EuropaSetting> settings = new ArrayList<EuropaSetting>();
        settings.add(EuropaSetting
                     .builder()
                     .domain(Constants.DOMAIN_ZERO)
                     .key(SETTING_DNS_NAME)
                     .value(this.dnsName)
                     .type(EuropaSettingType.DNS)
                     .build());
        return settings;
    }
}
