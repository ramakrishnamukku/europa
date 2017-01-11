package com.distelli.europa.db;

import com.distelli.europa.models.*;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.distelli.utils.CompactUUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import lombok.extern.log4j.Log4j;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import java.util.Set;
import java.util.HashSet;

@Log4j
@Singleton
public class RegistryManifestDb extends BaseDb {
    private static final String TABLE_NAME = "rmanifest";
    private static final String ATTR_REPOSITORY = "repo";
    private static final String ATTR_TAG = "tag";
    private static final String ATTR_MANIFEST_ID = "id";
    private static final String ATTR_DIGESTS = "mds";
    private static final String ATTR_UPLOADED_BY = "by";

    private Index<RegistryManifest> _main;

    private final ObjectMapper _om = new ObjectMapper();

    @Inject
    private UserDb _userDb;

    @Inject
    private RegistryBlobDb _blobDb;

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName(TABLE_NAME)
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr(ATTR_REPOSITORY, AttrType.STR))
                    .rangeKey(attr(ATTR_TAG, AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(RegistryManifest.class)
            .put(ATTR_REPOSITORY, String.class, "repository")
            .put(ATTR_TAG, String.class, "tag")
            .put(ATTR_MANIFEST_ID, String.class, "manifestId")
            .put(ATTR_DIGESTS, new TypeReference<Set<String>>(){}, "digests")
            .put(ATTR_UPLOADED_BY, String.class, "uploadedBy");
        return module;
    }

    @Inject
    protected RegistryManifestDb(Index.Factory indexFactory,
                                 ConvertMarker.Factory convertMarkerFactory) {
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(RegistryManifest.class)
            .withTableName(TABLE_NAME)
            .withHashKeyName(ATTR_REPOSITORY)
            .withRangeKeyName(ATTR_TAG)
            .withConvertValue(_om::convertValue)
            .withConvertMarker(convertMarkerFactory.create(ATTR_REPOSITORY, ATTR_TAG))
            .build();
    }

    /**
     * Overwrites with a new registry manifest, potentially
     */
    public void put(RegistryManifest manifest) throws UnknownDigests {
        // Validate uploadedBy:
        if ( null == manifest.getUploadedBy() || manifest.getUploadedBy().isEmpty() ) {
            throw new IllegalArgumentException("uploadedBy is required parameter");
        }
        if ( null == _userDb.getUserByDomain(manifest.getUploadedBy()) ) {
            throw new IllegalArgumentException("Unknown uploadedBy="+manifest.getUploadedBy());
        }

        String manifestId = manifest.getManifestId();
        if ( null == manifestId || ! manifestId.matches("^sha256:[0-9a-f]{64}$") ) {
            throw new IllegalArgumentException(
                "Illegal manifestId="+manifestId+" expected to match sha256:[0-9a-f]{64}");
        }

        // Validate digests (and add references):
        Set<String> digests = manifest.getDigests();
        if ( null == digests ) digests = Collections.emptySet();
        Set<String> unknownDigests = new HashSet<>();
        for ( String digest : digests ) {
            if ( ! _blobDb.addReference(digest, manifestId) ) {
                unknownDigests.add(digest);
            }
        }
        if ( ! unknownDigests.isEmpty() ) {
            for ( String digest : digests ) {
                if ( ! unknownDigests.contains(digest) ) {
                    _blobDb.removeReference(digest, manifestId);
                }
            }
            throw new UnknownDigests(
                "DigestsUnknown "+unknownDigests+" referenced by "+manifest,
                unknownDigests);
        }

        boolean success = false;
        try {
            RegistryManifest old = _main.putItem(manifest);
            if ( null != old && null != old.getDigests() && null != old.getManifestId() ) {
                // clean-up references:
                for ( String digest : old.getDigests() ) {
                    _blobDb.removeReference(digest, old.getManifestId());
                }
            }
            success = true;
        } finally {
            if ( ! success ) {
                for ( String digest : digests ) {
                    _blobDb.removeReference(digest, manifestId);
                }
            }
        }
    }
}
