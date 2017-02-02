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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.distelli.persistence.*;
import com.distelli.webserver.*;
import com.google.inject.Singleton;
import org.eclipse.jetty.http.HttpMethod;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;
import javax.inject.Provider;
import com.distelli.europa.EuropaRequestContext;
import java.util.stream.Collectors;

@Log4j
@Singleton
public class ListReposInRegistry extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private RegistryCredsDb _credsDb;

    @Inject
    private Provider<GcrClient.Builder> _gcrClientBuilderProvider;

    @Inject
    private Provider<DockerHubClient.Builder> _dhClientBuilderProvider;

    public ListReposInRegistry()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String credId = ajaxRequest.getParam("credId");
        String credDomain = requestContext.getOwnerDomain();
        RegistryCred cred = null;
        if(credId != null)
        {
            cred = _credsDb.getCred(credDomain, credId);
            if(cred == null)
                throw(new AjaxClientException("Invalid CredId: "+credId, JsonError.Codes.BadParam, 400));
        }
        else
        {
            RegistryProvider provider = ajaxRequest.getParamAsEnum("provider", RegistryProvider.class, true);
            cred = RegistryCred.builder()
                .domain(credDomain)
                .provider(provider)
                .username(getParam(ajaxRequest, provider, "username"))
                .password(getParam(ajaxRequest, provider, "password"))
                .secret(getParam(ajaxRequest, provider, "secret"))
                .key(getParam(ajaxRequest, provider, "key"))
                .region(getParam(ajaxRequest, provider, "region"))
                .endpoint(getParam(ajaxRequest, provider, "endpoint"))
                .build();
        }

        switch(cred.getProvider())
        {
        case ECR:
            return listEcrRepos(cred);
        case GCR:
            return listGcrRepos(cred);
        case DOCKERHUB:
            return listDockerHubRepos(cred);
        }
        return null;
    }

    private static Set<String> asSet(String... strs) {
        return new HashSet<>(Arrays.asList(strs));
    }

    private static String getParam(AjaxRequest ajaxRequest, RegistryProvider provider, String paramName) {
        boolean isRequired = false;
        Set<String> required = null;
        switch ( provider ) {
        case DOCKERHUB:
            required = asSet("username", "password", "endpoint");
            break;
        case PRIVATE:
            required = asSet("username", "password");
            break;
        case ECR:
            required = asSet("key", "secret", "region");
            break;
        case GCR:
            required = asSet("secret", "region");
            break;
        }
        if ( null == required ) return null;
        if ( required.contains(paramName) ) {
            return ajaxRequest.getParam(paramName, true);
        }
        return null;
    }

    private List<String> listEcrRepos(RegistryCred registryCred)
    {
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

    private List<String> listGcrRepos(RegistryCred registryCred)
    {
        try {
            GcrClient gcrClient = _gcrClientBuilderProvider.get()
                .gcrCredentials(new GcrServiceAccountCredentials(registryCred.getSecret()))
                .gcrRegion(GcrRegion.getRegion(registryCred.getRegion()))
                .build();
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

    private List<String> listDockerHubRepos(RegistryCred registryCred) {
        DockerHubClient client = _dhClientBuilderProvider.get()
            .credentials(registryCred.getUsername(), registryCred.getPassword())
            .build();
        List<String> repoNames = new ArrayList<>();
        try {
            for ( PageIterator iter : new PageIterator().pageSize(100) ) {
                repoNames.addAll(client.listRepositories(registryCred.getUsername(), iter).stream()
                                 .map((repo) -> repo.getNamespace() + "/" + repo.getName())
                                 .collect(Collectors.toList()));
            }
        } catch ( IOException ex ) {
            throw new RuntimeException(ex);
        }
        return repoNames;
    }
}
