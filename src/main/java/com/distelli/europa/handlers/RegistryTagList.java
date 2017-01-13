package com.distelli.europa.handlers;

import org.eclipse.jetty.http.HttpMethod;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.distelli.webserver.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Singleton;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import com.distelli.europa.db.RegistryManifestDb;
import com.distelli.europa.models.RegistryManifest;
import java.util.List;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import java.util.stream.Collectors;
import com.distelli.persistence.PageIterator;

@Log4j
@Singleton
public class RegistryTagList extends RegistryBase {
    @Inject
    private RegistryManifestDb _manifestDb;
    private static class Response {
        public String name;
        public List<String> tags;
    }
    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        String owner = requestContext.getMatchedRoute().getParam("owner");
        String ownerDomain = getDomainForOwner(owner);
        if ( null != owner && null == ownerDomain ) {
            throw new RegistryError("Unknown username="+owner,
                                    RegistryErrorCode.NAME_UNKNOWN);
        }
        String name = requestContext.getMatchedRoute().getParam("name");
        PageIterator it = new PageIterator()
            .pageSize(getPageSize(requestContext))
            .marker(requestContext.getParameter("last"));
        List<RegistryManifest> manifests = _manifestDb.listManifestsByRepo(ownerDomain, name, it);

        Response response = new Response();
        response.name = joinWithSlash(owner, name);
        response.tags = manifests.stream()
            .map((manifest) -> manifest.getTag())
            .collect(Collectors.toList());

        String location = null;
        if ( null != it.getMarker() ) {
            location = joinWithSlash("/v2", owner, name, "tags/list") + "?last=" + it.getMarker();
            if ( DEFAULT_PAGE_SIZE != it.getPageSize() ) {
                location = location + "&n="+it.getPageSize();
            }
        }
        WebResponse webResponse = toJson(response);
        if ( null != location ) {
            webResponse.setResponseHeader("Link", location + "; rel=\"next\"");
        }
        return webResponse;
    }
}
