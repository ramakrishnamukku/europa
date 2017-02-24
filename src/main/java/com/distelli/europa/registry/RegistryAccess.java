/*
  $Id: $
  @file RegistryAccess.java
  @brief Contains the RegistryAccess.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.registry;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.models.ContainerRepo;
import lombok.extern.log4j.Log4j;

public interface RegistryAccess
{
    public void checkAccess(String operationName, EuropaRequestContext requestContext);

    @Log4j
    public static class Default implements RegistryAccess {
        @Inject
        protected ContainerRepoDb _repoDb;

        protected static final Set<String> READ_OPERATIONS = new HashSet<String>();

        static {
            READ_OPERATIONS.add("RegistryLayerPull");
            READ_OPERATIONS.add("RegistryCatalog");
            READ_OPERATIONS.add("RegistryTagList");
            READ_OPERATIONS.add("RegistryManifestPull");
            READ_OPERATIONS.add("RegistryLayerExists");
            READ_OPERATIONS.add("RegistryManfestExists");
        }

        public void checkAccess(String operationName, EuropaRequestContext requestContext)
        {
            if(operationName.equalsIgnoreCase("RegistryDefault") ||
               operationName.equalsIgnoreCase("RegistryTokenHandler") ||
               operationName.equalsIgnoreCase("PremiumRegistryTokenHandler"))
                return;

            //Its an authenticated request with a valid token so its
            //allowed.
            if(allowAuthenticatedRequest(operationName, requestContext))
                return;
            //Registry Version check should throw an Auth Error
            if(operationName.equalsIgnoreCase("RegistryVersionCheck"))
                RequireAuthError.throwRequireAuth("Missing Authorization header", requestContext);
            checkPublicRepo(operationName, requestContext);
        }

        protected boolean allowAuthenticatedRequest(String operationName, EuropaRequestContext requestContext)
        {
            String requesterDomain = requestContext.getRequesterDomain();
            if(requesterDomain != null)
                return true;
            return false;
        }

        protected boolean isReadOperation(String operationName)
        {
            return READ_OPERATIONS.contains(operationName);
        }

        protected void checkPublicRepo(String operationName, EuropaRequestContext requestContext)
        {
            //Don't allow access to non-read operations
            if(!isReadOperation(operationName))
                throw(new RegistryError("You do not have access to this operation",
                                        RegistryErrorCode.UNAUTHORIZED));
            String ownerDomain = requestContext.getOwnerDomain();
            String repoName = requestContext.getMatchedRoute().getParam("name");
            ContainerRepo repo = _repoDb.getLocalRepo(ownerDomain,
                                                      repoName);
            //if its a public repo then allow acess
            if(repo != null && repo.isPublicRepo())
                return;

            //We've arrived here so this means that we don't allow access (or the repo was not found).
            throw(new RegistryError("You do not have access to this operation",
                                    RegistryErrorCode.UNAUTHORIZED));
        }
    }
}
