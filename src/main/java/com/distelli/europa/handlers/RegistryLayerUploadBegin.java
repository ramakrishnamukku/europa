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
import com.distelli.objectStore.ObjectStore;

@Log4j
@Singleton
public class RegistryLayerUploadBegin extends RegistryBase {
    @Inject
    private ObjectStore _objectStore;

    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        String digest = requestContext.getParameter("digest");
        if ( null == digest ) return handleMultipartInit(requestContext);
        return handleMonolithicUpload(requestContext);
    }

    private WebResponse handleMonolithicUpload(RequestContext requestContext) {
        throw new UnsupportedOperationException();
    }

    private WebResponse handleMultipartInit(RequestContext requestContext) {
        String uuid = "TODO";
        String location = "/v2/"+requestContext.getParameter("name")+"/blobs/uploads/"+uuid;
        WebResponse response = new WebResponse(202);
        response.setResponseHeader("Location", location);
        response.setResponseHeader("Docker-Upload-UUID", uuid);
        return response;
    }
}
