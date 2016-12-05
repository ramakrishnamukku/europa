/*
  $Id: $
  @file ListRegistryCreds.java
  @brief Contains the ListRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.persistence.PageIterator;

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
        RegistryProvider provider = ajaxRequest.getAsEnum("provider", RegistryProvider.class);
        if(provider != null)
            return _db.listCredsForProvider(provider, pageIterator);
        return _db.listAllCreds(pageIterator);
    }
}
