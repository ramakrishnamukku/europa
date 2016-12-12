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
import org.apache.log4j.Logger;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ListRegistryCreds implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(ListRegistryCreds.class);

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
