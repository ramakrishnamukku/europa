package com.distelli.europa.handlers;

import com.distelli.europa.EuropaRequestContext;
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
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.models.RegistryBlob;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.distelli.objectStore.ObjectStore;
import com.distelli.objectStore.ObjectKey;
import com.distelli.objectStore.ObjectMetadata;
import com.distelli.europa.util.ObjectKeyFactory;
import javax.inject.Provider;

@Log4j
@Singleton
public class RegistryLayerPull extends RegistryBase {
    @Inject
    private RegistryBlobDb _blobDb;
    @Inject
    private Provider<ObjectStore> _objectStoreProvider;
    @Inject
    private Provider<ObjectKeyFactory> _objectKeyFactoryProvider;
    public WebResponse handleRegistryRequest(EuropaRequestContext requestContext) {
        String ownerUsername = requestContext.getOwnerUsername();
        String ownerDomain = requestContext.getOwnerDomain();
        String name = requestContext.getMatchedRoute().getParam("name");
        String digest = requestContext.getMatchedRoute().getParam("digest");

        if ( null == digest || digest.isEmpty() ) {
            throw new RegistryError("Invalid :digest parameter (must not be empty)",
                                    RegistryErrorCode.BLOB_UNKNOWN);
        }

        RegistryBlob blob = _blobDb.getRegistryBlobByDigest(digest.toLowerCase());
        if ( null == blob ) {
            throw new RegistryError("Invalid :digest parameter (digest is not known)",
                                    RegistryErrorCode.BLOB_UNKNOWN);
        }
        ObjectKeyFactory objectKeyFactory = _objectKeyFactoryProvider.get();
        ObjectKey objKey = objectKeyFactory.forRegistryBlobId(blob.getBlobId());
        ObjectStore objectStore = _objectStoreProvider.get();
        // Check that object store is consistent with DB:
        ObjectMetadata objMeta = objectStore.head(objKey);
        if ( null == objMeta ) {
            throw new RegistryError("Invalid :digest parameter (object key missing "+objKey+")",
                                    RegistryErrorCode.BLOB_UNKNOWN);
        }
        WebResponse response = new WebResponse(200);
        // NOTE: Response is gzip'ed, so we can't set the content-length!
        // response.setResponseHeader("Content-Length", ""+blob.getSize());
        String mediaType = blob.getMediaType();
        if ( null != mediaType ) {
            response.setContentType(mediaType);
        } else {
            // Assume a v1 manifest due to this bug:
            // https://github.com/docker/distribution/issues/2084
            response.setContentType("application/vnd.docker.container.image.v1+json");
        }
        response.setCharacterEncoding(null);
        response.setResponseHeader("Docker-Content-Digest", digest);
        response.setResponseWriter(
            (out) -> objectStore.get(objKey, (meta, in) -> {
                    pump(in, out);
                    return null;
                }));

        return response;
    }
}
