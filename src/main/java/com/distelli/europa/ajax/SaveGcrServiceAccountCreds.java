/*
  $Id: $
  @file SaveGcrServiceAccountCreds.java
  @brief Contains the SaveGcrServiceAccountCreds.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.regex.*;
import javax.inject.Inject;

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
import org.eclipse.jetty.http.HttpMethod;

@Log4j
@Singleton
public class SaveGcrServiceAccountCreds extends AjaxHelper
{
    @Inject
    private RegistryCredsDb _db;

    public SaveGcrServiceAccountCreds()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, RequestContext requestContext)
    {
        RegistryCred cred = ajaxRequest.convertContent(RegistryCred.class,
                                                       true); //throw if null
        //Validate that the fields we want are non-null
        FieldValidator.validateNonNull(cred, "provider", "region", "secret");
        FieldValidator.validateMatch(cred, "name", Constants.REGISTRY_CRED_NAME_PATTERN);
        FieldValidator.validateEquals(cred, "provider", RegistryProvider.GCR);
        validateRegistryCreds(cred);
        cred.setCreated(System.currentTimeMillis());
        String id = cred.getId();
        if(id != null) {
            //check that cred with that id exists
            RegistryCred existingCred = _db.getCred(cred.getDomain(), id.toLowerCase());
            if(existingCred == null)
                throw(new AjaxClientException("Invalid Registry Cred Id: "+id, JsonError.Codes.BadContent, 400));
        } else {
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
