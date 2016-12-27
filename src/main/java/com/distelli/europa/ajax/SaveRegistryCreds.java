/*
  $Id: $
  @file SaveRegistryCreds.java
  @brief Contains the SaveRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.*;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.util.*;
import com.distelli.ventura.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.distelli.persistence.PageIterator;
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
        validateRegistryCreds(cred);
        cred.setCreated(System.currentTimeMillis());
        String id = UUID.randomUUID().toString();
        cred.setId(id);
        //save in the db
        _db.save(cred);
        return new HashMap<String, String>() {{
            put("id", id);
        }};
    }

    private void validateRegistryCreds(RegistryCred cred) {
        RegistryProvider provider = cred.getProvider();
        switch(provider)
        {
        case ECR:
            validateEcrCreds(cred);
            break;
        case GCR:
            validateGcrCreds(cred);
            break;
        default:
            throw(new AjaxClientException("Unsupported Container Registry: "+provider, JsonError.Codes.BadContent, 400));
        }
    }

    private void validateEcrCreds(RegistryCred cred) {
        ECRClient ecrClient = new ECRClient(cred);
        PageIterator pageIterator = new PageIterator().pageSize(1);
        try {
            List<ContainerRepo> repos = ecrClient.listRepositories(pageIterator);
        } catch(Throwable t) {
            throw(new AjaxClientException("Invalid Credentials: "+t.getMessage(), JsonError.Codes.BadContent, 400));
        }
    }

    private void validateGcrCreds(RegistryCred cred) {
        GcrCredentials gcrCreds = new GcrServiceAccountCredentials(cred.getSecret());
        GcrRegion gcrRegion = GcrRegion.getRegion(cred.getRegion());
        GcrClient gcrClient = new GcrClient(gcrCreds, gcrRegion);

        GcrIterator iter = GcrIterator.builder().pageSize(1).build();
        try {
            List<GcrRepository> repos = gcrClient.listRepositories(iter);
        } catch(Throwable t) {
            throw(new AjaxClientException("Invalid Credentials: "+t.getMessage(), JsonError.Codes.BadContent, 400));
        }
    }
}
