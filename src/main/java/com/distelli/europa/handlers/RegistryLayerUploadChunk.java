package com.distelli.europa.handlers;

import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.distelli.europa.util.ObjectKeyFactory;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.models.RegistryBlob;
import com.distelli.europa.models.RegistryBlobPart;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.distelli.webserver.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import com.distelli.objectStore.ObjectStore;
import com.distelli.objectStore.ObjectPartKey;
import com.distelli.objectStore.ObjectKey;
import com.distelli.objectStore.ObjectPartId;
import javax.persistence.EntityNotFoundException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import java.io.InputStream;
import java.security.DigestInputStream;
import com.distelli.europa.security.JDKMessageDigest;
import java.io.FilterInputStream;
import java.io.IOException;
import com.distelli.utils.ResettableInputStream;
import com.distelli.utils.CountingInputStream;

@Log4j
@Singleton
public class RegistryLayerUploadChunk extends RegistryBase {
    private static long MIN_SIZE = 5*1048576;

    @Inject
    private ObjectStore _objectStore;
    @Inject
    private ObjectKeyFactory _objectKeyFactory;
    @Inject
    private RegistryBlobDb _blobDb;

    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        try {
            return handleRegistryRequest(requestContext, false);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }

    public WebResponse handleRegistryRequest(RequestContext requestContext, boolean isLastChunk) throws Exception {
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
        ObjectKey objKey = _objectKeyFactory.forRegistryBlobId(blobId);
        ObjectPartKey partKey = ObjectPartKey.builder()
            .bucket(objKey.getBucket())
            .key(objKey.getKey())
            .uploadId(blob.getUploadId())
            .build();
        long totalSize = getTotalSize(blob.getPartIds());
        int partNum = getLength(blob.getPartIds()) + 1;
        ObjectPartId partId = null;
        SHA256Digest digest =  ( null == blob.getMdEncodedState() )
            ? new SHA256Digest()
            : new SHA256Digest(blob.getMdEncodedState());

        InputStream is = requestContext.getRequestStream();
        long contentLength = requestContext.getContentLength();
        if ( contentLength <= 0 ) {
            // we must count first:
            is = new ResettableInputStream(is);
            CountingInputStream counter = new CountingInputStream(is);
            byte[] chunk = new byte[1024*1024];
            while ( counter.read(chunk) > 0 );
            contentLength = counter.getCount();
            is.reset();
        }

        is = new DigestInputStream(is, new JDKMessageDigest(digest));
        try {
            partId = _objectStore.multipartPut(partKey, partNum, contentLength, is);
        } catch ( EntityNotFoundException ex ) {
            // Forget about this upload in our DB then...
            _blobDb.forgetBlob(blobId);
            throw new RegistryError("Invalid :uuid parameter", RegistryErrorCode.BLOB_UPLOAD_UNKNOWN);
        }
        RegistryBlobPart blobPart = RegistryBlobPart.builder()
            .chunkSize(contentLength)
            .partNum(partId.getPartNum())
            .partId(partId.getPartId())
            .build();
        _blobDb.addPart(blobId, partNum, blobPart, blob.getMdEncodedState(), digest.getEncodedState());
        WebResponse response = new WebResponse(201);
        response.setContentType("text/plain");
        response.setResponseHeader("Range", "0-"+(totalSize+contentLength));
        response.setResponseHeader("Docker-Upload-UUID", blobId);
        response.setResponseHeader("Location", "/v2/"+name+"/blobs/uploads/"+blobId);
        return response;
    }

/*
        Long[] range = parseRange(requestContext.getHeaderValue("Range"));
        if ( null == range && ! isLastChunk) {
            System.err.println("headers="+requestContext.getHeaders());
            throw new RegistryError(
                "Expected range to match regex ^(?:0|[1-9][0-9]*)-(?:0|[1-9][0-9]*)$ got="+
                requestContext.getHeaderValue("Range"),
                RegistryErrorCode.RANGE_INVALID);
        }

        long contentLength = requestContext.getContentLength();
        if ( ! isLastChunk ) {
            if ( contentLength < MIN_SIZE ) {
                throw rangeNotSatisfiable("Content-Length must be > "+MIN_SIZE, requestContext, totalSize);
            }
        }
        if ( null != range && range[0] != totalSize ) {
            throw rangeNotSatisfiable("Expected Range to begin with "+totalSize+", but got "+range[0], requestContext, totalSize);
        }
        if ( null != range && range[1]-range[0] != contentLength ) {
            throw new RegistryError(
                "Expected Range to end with "+(totalSize+contentLength)+", but got "+range[1],
                RegistryErrorCode.RANGE_INVALID);
        }
*/

    private Long[] parseRange(String rangeStr) {
        Long[] range = new Long[2];
        if ( null == rangeStr || ! rangeStr.matches("^(?:0|[1-9][0-9]*)-(?:0|[1-9][0-9]*)$") ) {
            return null;
        }
        int dash = rangeStr.indexOf('-');
        range[0] = Long.parseLong(rangeStr.substring(0, dash));
        range[1] = Long.parseLong(rangeStr.substring(dash+1));
        return range;
    }

    private RegistryError rangeNotSatisfiable(String reason, RequestContext requestContext, long totalSize) {
        String name = requestContext.getMatchedRoute().getParam("name");
        String blobId = requestContext.getMatchedRoute().getParam("uuid");
        return new RegistryError(reason, RegistryErrorCode.RANGE_NOT_SATISFIABLE) {
            public Map<String, String> getResponseHeaders() {
                return new HashMap<String, String>() {{
                    put("Location", "/v2/"+name+"/blobs/uploads/"+blobId);
                    put("Range", "0-"+totalSize);
                    put("Docker-Upload-UUID", blobId);
                }};
            }
        };
    }

    private int getLength(List<RegistryBlobPart> partIds) {
        if ( null == partIds ) return 0;
        return partIds.size();
    }

    private long getTotalSize(List<RegistryBlobPart> partIds) {
        if ( null == partIds ) return 0;
        return partIds.stream().mapToLong((part) -> part.getChunkSize()).sum();
    }
}
