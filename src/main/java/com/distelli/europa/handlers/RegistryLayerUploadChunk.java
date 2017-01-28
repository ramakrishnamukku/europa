package com.distelli.europa.handlers;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.models.RegistryBlob;
import com.distelli.europa.models.RegistryBlobPart;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.distelli.europa.security.JDKMessageDigest;
import com.distelli.europa.util.ObjectKeyFactory;
import com.distelli.objectStore.ObjectKey;
import com.distelli.objectStore.ObjectPartId;
import com.distelli.objectStore.ObjectPartKey;
import com.distelli.objectStore.ObjectStore;
import com.distelli.utils.CountingInputStream;
import com.distelli.utils.ResettableInputStream;
import com.distelli.webserver.RequestContext;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.eclipse.jetty.http.HttpMethod;
import javax.inject.Provider;

@Log4j
@Singleton
public class RegistryLayerUploadChunk extends RegistryBase {
    private static long MIN_SIZE = 5*1048576;

    @Inject
    private Provider<ObjectStore> _objectStoreProvider;
    @Inject
    private Provider<ObjectKeyFactory> _objectKeyFactoryProvider;
    @Inject
    private RegistryBlobDb _blobDb;

    public WebResponse handleRegistryRequest(EuropaRequestContext requestContext) {
        try {
            return handleRegistryRequest(requestContext, false);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }

    public WebResponse handleRegistryRequest(EuropaRequestContext requestContext, boolean isLastChunk) throws Exception {
        String ownerUsername = requestContext.getOwnerUsername();
        String ownerDomain = requestContext.getOwnerDomain();
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
        ObjectKeyFactory objectKeyFactory = _objectKeyFactoryProvider.get();
        ObjectKey objKey = objectKeyFactory.forRegistryBlobId(blobId);
        ObjectPartKey partKey = getObjectPartKey(blobId, blob.getUploadId());

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
        if ( contentLength > 0 ) {
            int size = blob.getPartIds().size();
            if ( size > 0 ) {
                long lastSize = blob.getPartIds().get(size-1).getChunkSize();
                if ( lastSize < MIN_SIZE ) {
                    // Last chunk needs to be uploaded too:
                    throw rangeNotSatisfiable("Minimum chunk size="+MIN_SIZE, requestContext, totalSize-lastSize);
                }
            }

            is = new DigestInputStream(is, new JDKMessageDigest(digest));
            try {
                ObjectStore objectStore = _objectStoreProvider.get();
                partId = objectStore.multipartPut(partKey, partNum, contentLength, is);
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
        }
        WebResponse response = new WebResponse(201);
        response.setContentType("text/plain");
        response.setResponseHeader("Range", "0-"+(totalSize+contentLength));
        response.setResponseHeader("Docker-Upload-UUID", blobId);
        response.setResponseHeader("Location", joinWithSlash("/v2", ownerUsername, name, "blobs/uploads", blobId));
        return response;
    }

    protected ObjectPartKey getObjectPartKey(String blobId, String uploadId) {
        ObjectKeyFactory objectKeyFactory = _objectKeyFactoryProvider.get();
        ObjectKey objKey = objectKeyFactory.forRegistryBlobId(blobId);
        return ObjectPartKey.builder()
            .bucket(objKey.getBucket())
            .key(objKey.getKey())
            .uploadId(uploadId)
            .build();
    }

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

    private RegistryError rangeNotSatisfiable(String reason, EuropaRequestContext requestContext, long totalSize) {
        String ownerUsername = requestContext.getOwnerUsername();
        String name = requestContext.getMatchedRoute().getParam("name");
        String blobId = requestContext.getMatchedRoute().getParam("uuid");
        return new RegistryError(reason, RegistryErrorCode.RANGE_NOT_SATISFIABLE) {
            public Map<String, String> getResponseHeaders() {
                return new HashMap<String, String>() {{
                    put("Location", joinWithSlash("/v2", ownerUsername, name, "blobs/uploads", blobId));
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
