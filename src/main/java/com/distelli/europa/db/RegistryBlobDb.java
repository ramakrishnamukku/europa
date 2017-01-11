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
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import lombok.extern.log4j.Log4j;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

@Log4j
@Singleton
public class RegistryBlobDb extends BaseDb {
    private static final String TABLE_NAME = "rblob";
    private static final String ATTR_BLOB_ID = "id";
    private static final String ATTR_DIGEST = "md";
    private static final String ATTR_PART_IDS = "parts";
    private static final String ATTR_UPLOADED_BY = "by";
    private static final String ATTR_UPLOAD_ID = "up";
    private static final String ATTR_MD_ENCODED_STATE = "mdx";
    private static final String ATTR_MANIFEST_IDS = "mids";

    private Index<RegistryBlob> _main;
    private Index<RegistryBlob> _byDigest;

    private final ObjectMapper _om = new ObjectMapper();

    @Inject
    private UserDb _userDb;

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName(TABLE_NAME)
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr(ATTR_BLOB_ID, AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName(ATTR_DIGEST+"-index")
                    .hashKey(attr(ATTR_DIGEST, AttrType.STR))
                    .rangeKey(attr(ATTR_BLOB_ID, AttrType.STR))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(RegistryBlob.class)
            .put(ATTR_BLOB_ID, String.class, "blobId")
            .put(ATTR_DIGEST, String.class, "digest")
            .put(ATTR_PART_IDS, new TypeReference<List<RegistryBlobPart>>(){}, "partIds")
            .put(ATTR_UPLOADED_BY, String.class, "uploadedBy")
            .put(ATTR_UPLOAD_ID, String.class, "uploadId")
            .put(ATTR_MD_ENCODED_STATE, byte[].class, "mdEncodedState");
        module.createTransform(RegistryBlobPart.class)
            .put("n", Integer.class, "partNum")
            .put("i", String.class, "partId")
            .put("s", Long.class, "chunkSize");
        return module;
    }

    @Inject
    protected RegistryBlobDb(Index.Factory indexFactory,
                             ConvertMarker.Factory convertMarkerFactory) {
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(RegistryBlob.class)
            .withTableName(TABLE_NAME)
            .withNoEncrypt(ATTR_BLOB_ID, ATTR_MD_ENCODED_STATE, ATTR_DIGEST, ATTR_MANIFEST_IDS)
            .withHashKeyName(ATTR_BLOB_ID)
            .withConvertValue(_om::convertValue)
            .withConvertMarker(convertMarkerFactory.create(ATTR_BLOB_ID))
            .build();
        _byDigest = indexFactory.create(RegistryBlob.class)
            .withTableName(TABLE_NAME)
            .withNoEncrypt(ATTR_BLOB_ID, ATTR_MD_ENCODED_STATE, ATTR_DIGEST, ATTR_MANIFEST_IDS)
            .withHashKeyName(ATTR_DIGEST)
            .withRangeKeyName(ATTR_BLOB_ID)
            .withConvertValue(_om::convertValue)
            .withConvertMarker(convertMarkerFactory.create(ATTR_DIGEST, ATTR_BLOB_ID))
            .build();
    }

    public RegistryBlob newRegistryBlob(String uploadedBy) {
        if ( null == uploadedBy || uploadedBy.isEmpty() ) {
            throw new IllegalArgumentException("uploadedBy is required parameter");
        }
        if ( null == _userDb.getUserByDomain(uploadedBy) ) {
            throw new IllegalArgumentException("Unknown uploadedBy="+uploadedBy);
        }
        RegistryBlob blob = RegistryBlob.builder()
            .blobId(CompactUUID.randomUUID().toString())
            .partIds(Collections.emptyList())
            .uploadedBy(uploadedBy)
            .build();
        _main.putItem(blob);
        return blob;
    }

    public RegistryBlob getRegistryBlobByDigest(String digest) {
        List<RegistryBlob> results =
            _byDigest.queryItems(digest, new PageIterator().pageSize(1)).list();
        if ( results.isEmpty() ) return null;
        return results.get(0);
    }

    public RegistryBlob getRegistryBlobById(String blobId) {
        return _main.getItem(blobId);
    }

    // Simply forgets about a blob (from the DB perspective), might still be in S3.
    public void forgetBlob(String blobId) {
        _main.deleteItem(blobId, null);
    }

    public void setUploadId(String blobId, String uploadId) {
        try {
            _main.updateItem(blobId, null)
                .set(ATTR_UPLOAD_ID, AttrType.STR, uploadId)
                .when((expr) -> expr.exists(ATTR_BLOB_ID));
        } catch ( RollbackException ex ) {
            throw new EntityNotFoundException("blobId="+blobId+" does not exist");
        }
    }

    public void addPart(String blobId, int partIndex, RegistryBlobPart partId, byte[] oldMDState, byte[] newMDState)
        throws EntityNotFoundException, ConcurrentModificationException
    {
        try {
            _main.updateItem(blobId, null)
                .listSet(ATTR_PART_IDS, partIndex, AttrType.MAP, partId)
                .set(ATTR_MD_ENCODED_STATE, AttrType.BIN, newMDState)
                .when((expr) -> expr.and(
                          expr.exists(ATTR_BLOB_ID),
                          ( null == oldMDState )
                          ? expr.not(expr.exists(ATTR_MD_ENCODED_STATE))
                          : expr.eq(ATTR_MD_ENCODED_STATE, oldMDState)));
        } catch ( RollbackException ex ) {
            // Doesn't exist, then throw EntityNotFoundException?
            RegistryBlob blob = _main.getItemOrThrow(blobId, null);
            throw new ConcurrentModificationException(
                "Expected mdState="+(null==oldMDState?"null":printHexBinary(oldMDState))+", but got="+
                (null == blob.getMdEncodedState()?"null":printHexBinary(blob.getMdEncodedState())));
        }
    }

    public void finishUpload(String blobId, byte[] currentMDState, String digest) {
        try {
            _main.updateItem(blobId, null)
                .remove(ATTR_PART_IDS)
                .remove(ATTR_MD_ENCODED_STATE)
                .remove(ATTR_UPLOAD_ID)
                .set(ATTR_DIGEST, digest)
                .when((expr) -> expr.eq(ATTR_MD_ENCODED_STATE, currentMDState));
        } catch ( RollbackException ex ) {
            throw new ConcurrentModificationException(
                "attempt to finish upload, but the digest state did not match");
        }
    }

    // Returns false if digest does not exist.
    public boolean addReference(String digest, String manifestId) {
        for ( int retry=0;;retry++ ) {
            RegistryBlob blob = getRegistryBlobByDigest(digest);
            if ( null == blob ) return false;
            try {
                _main.updateItem(blob.getBlobId(), null)
                    .setAdd(ATTR_MANIFEST_IDS, AttrType.STR, manifestId)
                    .when((expr) -> expr.exists(ATTR_BLOB_ID));
            } catch ( RollbackException ex ) {
                log.info(ex.getMessage(), ex);
                // Give up!
                if ( retry > 10 ) return false;
                continue;
            }
            break;
        }
        return true;
    }

    public void removeReference(String digest, String manifestId) {
        for ( PageIterator it : new PageIterator() ) {
            for ( RegistryBlob blob : _byDigest.queryItems(digest, it).list() ) {
                try {
                    _main.updateItem(blob.getBlobId(), null)
                        .setRemove(ATTR_MANIFEST_IDS, AttrType.STR, manifestId)
                        .when((expr) -> expr.exists(ATTR_BLOB_ID));
                } catch ( RollbackException ex ) {
                    // ignored, just don't want to create a "blank" record.
                }
            }
        }
    }
}
