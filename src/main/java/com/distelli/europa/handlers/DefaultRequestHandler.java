/*
  $Id: $
  @file DefaultRequestHandler.java
  @brief Contains the DefaultRequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.models.DnsSettings;
import com.distelli.europa.react.JSXProperties;
import com.distelli.europa.react.PageTemplate;
import com.distelli.webserver.*;

import lombok.extern.log4j.Log4j;

@Log4j
public class DefaultRequestHandler extends RequestHandler<EuropaRequestContext>
{
    @Inject
    private PageTemplate _pageTemplate;
    @Inject
    private Provider<DnsSettings> _dnsSettingsProvider;

    public DefaultRequestHandler()
    {

    }

    public WebResponse handleRequest(EuropaRequestContext requestContext)
    {
        DnsSettings dnsSettings = _dnsSettingsProvider.get();
        if(dnsSettings == null)
            dnsSettings = DnsSettings.fromHostHeader(requestContext);
        JSXProperties props = new JSXProperties(requestContext, dnsSettings);
        return _pageTemplate.renderPage(requestContext, props);
    }
}
