package com.distelli.europa.db;

import com.distelli.europa.Constants;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.models.*;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.objectStore.ObjectPartId;
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
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryBlobDb {
    private static final String TABLE_NAME = "rblob";
    private static final String ATTR_BLOB_ID = "id";
    private static final String ATTR_DIGEST = "md";
    private static final String ATTR_PART_IDS = "parts";
    private static final String ATTR_UPLOADED_BY = "by";
    private static final String ATTR_UPLOAD_ID = "up";

    private Index<RegistryBlob> _main;
    private Index<RegistryBlob> _byDigest;

    private EuropaConfiguration _europaConfiguration;
    private final ObjectMapper _om = new ObjectMapper();

    @Inject
    private UserDb _userDb;

    private static AttrDescription attr(String name, AttrType type) {
        return AttrDescription.builder()
            .attrName(name)
            .attrType(type)
            .build();
    }

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
            .put(ATTR_PART_IDS, new TypeReference<List<ObjectPartId>>(){}, "partIds")
            .put(ATTR_UPLOADED_BY, String.class, "uploadedBy")
            .put(ATTR_UPLOAD_ID, String.class, "uploadId");
        module.createTransform(ObjectPartId.class)
            .put("n", Integer.class, "partNum")
            .put("i", String.class, "partId");
        return module;
    }

    @Inject
    public RegistryBlobDb(Index.Factory indexFactory,
                          ConvertMarker.Factory convertMarkerFactory,
                          EuropaConfiguration europaConfiguration) {
        _europaConfiguration = europaConfiguration;
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(RegistryBlob.class)
            .withTableName(TABLE_NAME)
            .withNoEncrypt(ATTR_DIGEST)
            .withHashKeyName(ATTR_BLOB_ID)
            .withConvertValue(_om::convertValue)
            .withConvertMarker(convertMarkerFactory.create(ATTR_BLOB_ID))
            .build();
        _byDigest = indexFactory.create(RegistryBlob.class)
            .withTableName(TABLE_NAME)
            .withNoEncrypt(ATTR_BLOB_ID)
            .withHashKeyName(ATTR_DIGEST)
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

    public void addPart(String blobId, ObjectPartId partId) {
        try {
            _main.updateItem(blobId, null)
                .listAdd(ATTR_PART_IDS, AttrType.MAP, partId)
                .when((expr) -> expr.exists(ATTR_BLOB_ID));
        } catch ( RollbackException ex ) {
            throw new EntityNotFoundException("blobId="+blobId+" does not exist");
        }
    }
}
