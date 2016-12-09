/*
  $Id: $
  @file SaveRegistryCreds.java
  @brief Contains the SaveRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.UUID;
import org.apache.log4j.Logger;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.webserver.*;
import com.distelli.europa.util.*;

import java.util.regex.*;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SaveRegistryCreds implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(SaveRegistryCreds.class);

    @Inject
    private RegistryCredsDb _db;

    private final Pattern registryCredNamePattern = Pattern.compile("[a-zA-Z0-9_-]+");

    public SaveRegistryCreds()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        RegistryCred cred = ajaxRequest.convertContent(RegistryCred.class,
                                                       true); //throw if null
        //Validate that the fields we want are non-null
        FieldValidator.validateNonNull(cred, "provider", "region", "key", "secret");
        FieldValidator.validateMatch(cred, "name", registryCredNamePattern);
        cred.setCreated(System.currentTimeMillis());
        String id = UUID.randomUUID().toString();
        cred.setId(id);
        //save in the db
        _db.save(cred);
        return id;
    }
}
