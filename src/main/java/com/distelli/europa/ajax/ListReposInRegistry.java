/*
  $Id: $
  @file ListReposInRegistry.java
  @brief Contains the ListReposInRegistry.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.distelli.persistence.*;
import com.distelli.ventura.*;
import com.google.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ListReposInRegistry implements AjaxHelper
{
    public ListReposInRegistry()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        RegistryProvider provider = ajaxRequest.getParamAsEnum("provider", RegistryProvider.class, true);
        String secret = ajaxRequest.getParam("secret", true);
        String region = ajaxRequest.getParam("region", true);

        switch(provider)
        {
        case ECR:
            String key = ajaxRequest.getParam("key", true);
            return listEcrRepos(key, secret, region);
        case GCR:
            return listGcrRepos(secret, region);
        }
        return null;
    }

    private List<String> listEcrRepos(String key, String secret, String region)
    {
        RegistryCred registryCred = RegistryCred
        .builder()
        .key(key)
        .secret(secret)
        .region(region)
        .build();

        ECRClient ecrClient = new ECRClient(registryCred);
        PageIterator pageIterator = new PageIterator().pageSize(100);
        List<String> repoNames = new ArrayList<String>();
        do {
            List<ContainerRepo> repos = ecrClient.listRepositories(pageIterator);
            for(ContainerRepo repo : repos)
                repoNames.add(repo.getName());
        } while(pageIterator.getMarker() != null);
        return repoNames;
    }

    private List<String> listGcrRepos(String secret, String region)
    {
        try {
            GcrCredentials gcrCreds = new GcrServiceAccountCredentials(secret);
            GcrRegion gcrRegion = GcrRegion.getRegion(region);
            GcrClient gcrClient = new GcrClient(gcrCreds, gcrRegion);
            GcrIterator iter = GcrIterator
            .builder()
            .pageSize(100)
            .build();
            List<String> repoNames = new ArrayList<String>();
            do {
                List<GcrRepository> repos = gcrClient.listRepositories(iter);
                for(GcrRepository repo : repos)
                    repoNames.add(String.format("%s/%s",
                                                repo.getProjectName(),
                                                repo.getRepositoryName()));
            } while(iter.getMarker() != null);
            return repoNames;
        } catch(Throwable t) {
            log.error(t.getMessage(), t);
            return null;
        }
    }
}
