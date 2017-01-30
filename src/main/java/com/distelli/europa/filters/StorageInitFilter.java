/*
  $Id: $
  @file StorageInitFilter.java
  @brief Contains the StorageInitFilter.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.filters;

import javax.inject.Inject;
import javax.inject.Provider;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.guice.StorageSettingsProvider;
import com.distelli.europa.models.StorageSettings;
import com.distelli.europa.react.PageTemplate;
import com.distelli.europa.react.JSXProperties;
import com.distelli.webserver.RequestFilter;
import com.distelli.webserver.RequestFilterChain;
import com.distelli.webserver.WebResponse;

import lombok.extern.log4j.Log4j;

@Log4j
public class StorageInitFilter implements RequestFilter<EuropaRequestContext>
{
    @Inject
    protected Provider<StorageSettings> _storageSettingsProvider;
    @Inject
    protected PageTemplate _pageTemplate;

    public StorageInitFilter()
    {

    }

    public WebResponse filter(EuropaRequestContext requestContext, RequestFilterChain next)
    {
        String path = requestContext.getPath();
        if(path.endsWith("/ajax"))
            return next.filter(requestContext);
        StorageSettings storageSettings = _storageSettingsProvider.get();
        if(storageSettings != null)
            return next.filter(requestContext);

        JSXProperties jsxProps = new JSXProperties(requestContext) {
                public boolean getStorage() {
                    return false;
                }
            };
        return _pageTemplate.renderPage(requestContext, jsxProps);
    }
}
