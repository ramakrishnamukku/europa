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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.distelli.utils.ResettableInputStream;
import java.io.InputStream;
import com.distelli.utils.CountingInputStream;
import com.distelli.utils.ResettableInputStream;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import com.distelli.europa.util.ObjectKeyFactory;
import com.distelli.objectStore.ObjectKey;
import com.distelli.objectStore.ObjectStore;
import com.distelli.europa.models.RegistryManifest;
import com.distelli.europa.models.UnknownDigests;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.db.RegistryManifestDb;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

@Log4j
@Singleton
public class RegistryManifestPush extends RegistryBase {
    private static ObjectMapper OM = new ObjectMapper();
    
    static {
        // Support deserializing interfaces:
        OM.registerModule(new MrBeanModule());
        OM.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OM.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        OM.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    }

    @Inject
    private ObjectKeyFactory _objectKeyFactory;
    @Inject
    private ObjectStore _objectStore;
    @Inject
    private RegistryManifestDb _manifestDb;
    @Inject
    private RegistryBlobDb _blobDb;

    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        try {
            return handleRegistryRequestThrows(requestContext);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }
    public WebResponse handleRegistryRequestThrows(RequestContext requestContext) throws Exception {
        String name = requestContext.getMatchedRoute().getParam("name");
        String reference = requestContext.getMatchedRoute().getParam("reference");
        // TODO: Validate name and reference.

        InputStream is = new ResettableInputStream(requestContext.getRequestStream());
        CountingInputStream counter = new CountingInputStream(is);

        // Find the stream size and checksum, validate the layers exist:
        MessageDigest digestCalc = MessageDigest.getInstance("SHA-256");
        DigestInputStream digestStream = new DigestInputStream(counter, digestCalc);

        JsonNode manifest = OM.readTree(digestStream);
        Set<String> digests = getDigests(manifest);

        long contentLength = counter.getCount();
        is.reset();
        String finalDigest = "sha256:" + printHexBinary(digestCalc.digest()).toLowerCase();

        ObjectKey objKey = _objectKeyFactory.forRegistryManifest(finalDigest);
        if ( null == _objectStore.head(objKey) ) {
            _objectStore.put(objKey, contentLength, is);
        } else {
            objKey = null;
        }

        boolean success = false;
        try {
            _manifestDb.put(RegistryManifest.builder()
                            .uploadedBy(requestContext.getRemoteUser())
                            .manifestId(finalDigest)
                            .repository(name)
                            .tag(reference)
                            .digests(digests)
                            .build());
            success = true;
        } catch ( UnknownDigests ex ) {
            // TODO: make this be a list of digests...
            throw new RegistryError("Invalid digest(s), are unknown"+ex.getDigests(),
                                    RegistryErrorCode.BLOB_UNKNOWN,
                                    400);
        } finally {
            if ( ! success ) {
                if ( null != objKey ) _objectStore.delete(objKey);
            }
        }

        WebResponse response = new WebResponse();
        response.setHttpStatusCode(201);
        response.setResponseHeader("Location", "/v2/"+name+"/manifests/"+finalDigest);
        response.setResponseHeader("Docker-Content-Digest", finalDigest);
        return response;
    }

    private Set<String> getDigests(JsonNode manifest) {
        Set<String> digests = new TreeSet<>();
        if ( manifest.at("/layers").isArray() ) {
            for ( JsonNode layer : manifest.at("/layers") ) {
                digests.add(layer.at("/digest").asText());
            }
        } else if ( manifest.at("/fsLayers").isArray() ) {
            for ( JsonNode layer : manifest.at("/fsLayers") ) {
                digests.add(layer.at("/blobSum").asText());
            }
        }
        return digests;
    }
}
