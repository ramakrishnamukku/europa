package com.distelli.europa.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.models.ContainerRepo;
import com.distelli.europa.models.RegistryCatalogList;
import com.distelli.europa.models.RegistryProvider;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.RequestContext;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryCatalog extends RegistryBase {
    @Inject
    private ContainerRepoDb _reposDb;
    @Inject
    private EuropaConfiguration _europaConfiguration;

    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        //If we're running in multi-tenant mode then catalog API is
        //not supported
        if(_europaConfiguration.isMultiTenant())
            throw new RegistryError("/v2/_catalog is unsupported in multi-tenant mode.",
                                   RegistryErrorCode.UNSUPPORTED);

        String pageSizeStr = requestContext.getParameter("n");
        String marker = requestContext.getParameter("last");
        //TODO: Figure out the domain when listing repos
        String domain = null;
        int pageSize = 10;
        if(pageSizeStr != null)
        {
            try {
                pageSize = Integer.parseInt(pageSizeStr);
            } catch(NumberFormatException nfe) {
                //If the page size parameter is invalid just ignore it
                //and default to 10.
                pageSize = 10;
            }
        }
        PageIterator pageIterator = new PageIterator().pageSize(pageSize).marker(marker);
        List<ContainerRepo> repos = _reposDb.listRepos(domain,
                                                       RegistryProvider.EUROPA,
                                                       pageIterator);
        List<String> repoList = new ArrayList<String>();
        String lastRepo = null;
        if(repos != null && repos.size() > 0)
        {
            for(ContainerRepo repo : repos) {
                repoList.add(repo.getName());
                lastRepo = repo.getName();
            }
        }
        RegistryCatalogList catalogList = RegistryCatalogList
        .builder()
        .repositories(repoList)
        .build();

        //Link: <<url>?n=<n from the request>&last=<last repository in response>>; rel="next"
        String hostPort = requestContext.getHostPort(null);
        String proto = requestContext.getProto();
        WebResponse response = WebResponse.toJson(catalogList, 200);
        if(lastRepo != null)
            response.setResponseHeader("Link", proto+"://"+hostPort+"?n="+pageSize+"&last="+lastRepo+">; rel=next");
        return response;
    }
}
