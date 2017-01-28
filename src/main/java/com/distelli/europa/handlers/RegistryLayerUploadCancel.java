package com.distelli.europa.handlers;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.distelli.europa.util.ObjectKeyFactory;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.models.RegistryBlob;
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
import com.distelli.objectStore.ObjectPartKey;
import com.distelli.objectStore.ObjectKey;
import javax.persistence.EntityNotFoundException;
import javax.inject.Provider;

@Log4j
@Singleton
public class RegistryLayerUploadCancel extends RegistryBase {
    @Inject
    private Provider<ObjectStore> _objectStoreProvider;
    @Inject
    private Provider<ObjectKeyFactory> _objectKeyFactoryProvider;
    @Inject
    private RegistryBlobDb _blobDb;
    public WebResponse handleRegistryRequest(EuropaRequestContext requestContext) {
        String ownerUsername = requestContext.getOwnerUsername();
        String ownerDomain = requestContext.getOwnerDomain();
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
        ObjectPartKey partKey = ObjectPartKey.builder()
            .bucket(objKey.getBucket())
            .key(objKey.getKey())
            .uploadId(blob.getUploadId())
            .build();
        try {
            ObjectStore objectStore = _objectStoreProvider.get();
            objectStore.abortPut(partKey);
        } catch ( EntityNotFoundException ex ) {} // ignore
        _blobDb.forgetBlob(blobId);
        return new WebResponse(204);
    }
}
