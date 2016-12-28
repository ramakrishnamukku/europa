/*
  $Id: $
  @file ListRegistryCreds.java
  @brief Contains the ListRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.persistence.PageIterator;

import java.util.List;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.ventura.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ListRegistryCreds implements AjaxHelper
{
    @Inject
    private RegistryCredsDb _db;

    public ListRegistryCreds()
    {

    }

    /**
       Params:
       - Provider (optional)
    */
    public Object get(AjaxRequest ajaxRequest)
    {
        PageIterator pageIterator = new PageIterator().pageSize(1000).forward();
        RegistryProvider provider = ajaxRequest.getParamAsEnum("provider", RegistryProvider.class);
        String domain = ajaxRequest.getParam("domain");
        List<RegistryCred> creds;
        if(provider != null)
            creds = _db.listCredsForProvider(domain, provider, pageIterator);
        else
            creds = _db.listAllCreds(domain, pageIterator);
        for(RegistryCred cred : creds)
            cred.setSecret(null);
        return creds;
    }
}
