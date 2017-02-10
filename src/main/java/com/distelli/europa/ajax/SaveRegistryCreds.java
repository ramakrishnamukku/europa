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
import java.util.regex.*;
import javax.inject.Inject;
import javax.inject.Provider;

import com.distelli.utils.CompactUUID;
import com.distelli.europa.Constants;
import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.util.*;
import com.distelli.webserver.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.distelli.persistence.PageIterator;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import com.distelli.europa.EuropaRequestContext;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.util.PermissionCheck;

@Log4j
@Singleton
public class SaveRegistryCreds extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private RegistryCredsDb _db;
    @Inject
    private Provider<GcrClient.Builder> _gcrClientBuilderProvider;
    @Inject
    private Provider<DockerHubClient.Builder> _dhClientBuilderProvider;
    @Inject
    protected PermissionCheck _permissionCheck;

    public SaveRegistryCreds()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        RegistryCred cred = ajaxRequest.convertContent(RegistryCred.class,
                                                       true); //throw if null
        //Validate that the fields we want are non-null
        FieldValidator.validateNonNull(cred, "provider");
        FieldValidator.validateMatch(cred, "name", Constants.REGISTRY_CRED_NAME_PATTERN);
        validateRegistryCreds(cred);
        cred.setCreated(System.currentTimeMillis());
        //Here we set the region to an empty string if its null. Thats
        //because DockerHub and Private repos have no region
        if(cred.getRegion() == null)
            cred.setRegion("");
        String credDomain = requestContext.getOwnerDomain();
        cred.setDomain(credDomain);
        String id = cred.getId();
        if(id != null) {
            _permissionCheck.check(ajaxRequest.getOperation(), requestContext, Boolean.TRUE);
            //check that cred with that id exists
            RegistryCred existingCred = _db.getCred(credDomain, id.toLowerCase());
            if(existingCred == null)
                throw(new AjaxClientException("Invalid Registry Cred Id: "+id, JsonError.Codes.BadContent, 400));
        } else {
            _permissionCheck.check(ajaxRequest.getOperation(), requestContext, Boolean.FALSE);
            id = CompactUUID.randomUUID().toString();
            cred.setId(id);
        }
        //save in the db
        _db.save(cred);
        HashMap<String, String> retVal = new HashMap<String, String>();
        retVal.put("id", id);
        return retVal;
    }

    private void validateRegistryCreds(RegistryCred cred) {
        RegistryProvider provider = cred.getProvider();
        switch(provider)
        {
        case ECR:
            FieldValidator.validateNonNull(cred, "key", "secret", "region");
            validateEcrCreds(cred);
            break;
        case GCR:
            FieldValidator.validateNonNull(cred, "secret", "region");
            validateGcrCreds(cred);
            break;
        case DOCKERHUB:
            FieldValidator.validateNonNull(cred, "username", "password");
            validateDockerHubCreds(cred);
            break;
        default:
            throw(new AjaxClientException("Unsupported Container Registry: "+provider, JsonError.Codes.BadContent, 400));
        }
    }

    private void validateDockerHubCreds(RegistryCred cred) {
        DockerHubClient client = _dhClientBuilderProvider.get()
            .credentials(cred.getUsername(), cred.getPassword())
            .build();

        try {
            client.listRepositories(cred.getUsername(), new PageIterator().pageSize(1));
        } catch(Throwable t) {
            throw(new AjaxClientException("Invalid Credentials: "+t.getMessage(), JsonError.Codes.BadContent, 400));
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
        GcrClient gcrClient = _gcrClientBuilderProvider.get()
            .gcrCredentials(new GcrServiceAccountCredentials(cred.getSecret()))
            .gcrRegion(GcrRegion.getRegion(cred.getRegion()))
            .build();

        GcrIterator iter = GcrIterator.builder().pageSize(1).build();
        try {
            List<GcrRepository> repos = gcrClient.listRepositories(iter);
        } catch(Throwable t) {
            throw(new AjaxClientException("Invalid Credentials: "+t.getMessage(), JsonError.Codes.BadContent, 400));
        }
    }
}
