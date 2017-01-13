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
import com.distelli.europa.models.RegistryProvider;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.RequestContext;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryCatalog extends RegistryBase {
    private static int DEFAULT_PAGE_SIZE = 100;
    @Inject
    private ContainerRepoDb _reposDb;

    private static class Response {
        public List<String> repositories;
    }

    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        String owner = requestContext.getMatchedRoute().getParam("owner");
        String ownerDomain = getDomainForOwner(owner);
        if ( null != owner && null == ownerDomain ) {
            throw new RegistryError("Unknown username="+owner,
                                    RegistryErrorCode.NAME_UNKNOWN);
        }

        PageIterator pageIterator = new PageIterator()
            .pageSize(getPageSize(requestContext))
            .marker(requestContext.getParameter("last"));

        List<ContainerRepo> repos = _reposDb.listRepos(ownerDomain,
                                                       RegistryProvider.EUROPA,
                                                       pageIterator);

        Response response = new Response();
        response.repositories = repos.stream()
            .map((repo) -> joinWithSlash(owner, repo.getName()))
            .collect(Collectors.toList());

        String location = null;
        if ( null != pageIterator.getMarker() ) {
            location = joinWithSlash("/v2", owner, "_catalog") + "?last=" + pageIterator.getMarker();
            if ( DEFAULT_PAGE_SIZE != pageIterator.getPageSize() ) {
                location = location + "&n="+pageIterator.getPageSize();
            }
        }

        WebResponse webResponse = toJson(response);
        if ( null != location ) {
            webResponse.setResponseHeader("Link", location + "; rel=\"next\"");
        }
        return webResponse;
    }
}
