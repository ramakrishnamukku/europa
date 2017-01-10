package com.distelli.europa.handlers;

import java.util.List;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.models.RegistryBlob;
import com.distelli.europa.models.RegistryBlobPart;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
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

@Log4j
@Singleton
public class RegistryLayerUploadProgress extends RegistryBase {
    @Inject
    private RegistryBlobDb _blobDb;

    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        String name = requestContext.getMatchedRoute().getParam("name");
        String blobId = requestContext.getMatchedRoute().getParam("uuid");
        if ( null == blobId || blobId.isEmpty() ) {
            throw new RegistryError("Invalid :uuid parameter (must not be empty)",
                                    RegistryErrorCode.BLOB_UPLOAD_UNKNOWN);
        }
        RegistryBlob blob = _blobDb.getRegistryBlobById(blobId);
        if ( null == blob ) {
            throw new RegistryError("Invalid :uuid parameter", RegistryErrorCode.BLOB_UPLOAD_UNKNOWN);
        }
        if ( null == blob.getUploadId() ) {
            throw new RegistryError("The :uuid parameter specifies an upload that already succeeded.", RegistryErrorCode.BLOB_UPLOAD_INVALID);
        }
        // TODO: Validate the uploadId... just not sure how to do that with current
        // ObjectStore API (and this isn't that important)...
        long totalSize = getTotalSize(blob.getPartIds());

        WebResponse response = new WebResponse(204);
        response.setContentType("text/plain");
        response.setResponseHeader("Range", "0-"+totalSize);
        response.setResponseHeader("Docker-Upload-UUID", blobId);
        response.setResponseHeader("Location", "/v2/"+name+"/blobs/uploads/"+blobId);
        return response;
    }

    private long getTotalSize(List<RegistryBlobPart> partIds) {
        if ( null == partIds ) return 0;
        return partIds.stream().mapToLong((part) -> part.getChunkSize()).sum();
    }
}
