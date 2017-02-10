package com.distelli.europa.db;

import com.distelli.europa.Tag;
import com.distelli.europa.models.*;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.Attribute;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import lombok.extern.log4j.Log4j;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import java.util.ArrayList;

@Log4j
@Singleton
public class RegistryManifestDb extends BaseDb {
    private static final String TABLE_NAME = "rmanifest";

    private Index<RegistryManifest> _main;
    private Index<RegistryManifest> _byRepoManifestId;

    private final ObjectMapper _om = new ObjectMapper();

    @Inject
    private RegistryBlobDb _blobDb;

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName(TABLE_NAME)
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("dom", AttrType.STR))
                    .rangeKey(attr("rk", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName("dom-rmid-index")
                    .hashKey(attr("dom", AttrType.STR))
                    .rangeKey(attr("rmid", AttrType.STR))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(RegistryManifest.class)
            .put("dom", String.class, "domain")
            .put("rk", String.class, (manifest) -> toRK(manifest.getContainerRepoId(), manifest.getTag()))
            .put("repo", String.class, "containerRepoId")
            .put("tag", String.class, "tag")
            .put("id", String.class, "manifestId")
            .put("rmid", String.class, (manifest) -> toRepoManifestIdRK(manifest.getContainerRepoId(), manifest.getManifestId()))
            .put("mds", new TypeReference<Set<String>>(){}, "digests")
            .put("by", String.class, "uploadedBy")
            .put("ty", String.class, "contentType")
            .put("vsz", Long.class, "virtualSize")
            .put("ts", Long.class, "pushTime");
        return module;
    }

    private String toRK(String repoId, String tag) {
        if ( null == repoId ) throw new NullPointerException("containerRepoId can not be null");
        if ( null == tag ) throw new NullPointerException("tag can not be null");
        return repoId + "/" + tag;
    }

    private String toRepoManifestIdRK(String repoId, String manifestId)
    {
        if(manifestId == null || manifestId.trim().isEmpty())
            manifestId = "";
        return String.format("%s:%s",
                             repoId.toLowerCase(),
                             manifestId.toLowerCase());
    }

    @Inject
    protected RegistryManifestDb(Index.Factory indexFactory,
                                 ConvertMarker.Factory convertMarkerFactory) {
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(RegistryManifest.class)
            .withTableDescription(getTableDescription())
            .withConvertValue(_om::convertValue)
            // Custom convert marker implementation to support tag pagination:
            .withConvertMarker(new ConvertMarker() {
                    public String toMarker(Map<String, Object> attributes, boolean hasHashKey) {
                        if ( hasHashKey ) {
                            return (String)attributes.get("rk");
                        }
                        throw new UnsupportedOperationException("scan is not supported");
                    }
                    public Attribute[] fromMarker(Object hk, String marker) {
                        if ( null == hk ) {
                            throw new UnsupportedOperationException("scan is not supported");
                        }
                        return new Attribute[] {
                            new Attribute()
                            .withName("dom")
                            .withValue(hk),
                            new Attribute()
                            .withName("rk")
                            .withValue(marker)
                        };
                    }
                })
            .build();

        _byRepoManifestId = indexFactory.create(RegistryManifest.class)
        .withTableDescription(getTableDescription(), "dom-rmid-index")
        .withConvertValue(_om::convertValue)
        .build();
    }

    /**
     * Overwrites with a new registry manifest, potentially
     */
    public RegistryManifest put(RegistryManifest manifest) throws UnknownDigests {
        if ( null == manifest.getDomain() || manifest.getDomain().isEmpty()) {
            throw new IllegalArgumentException("domain is required parameter");
        }
        // Validate uploadedBy:
        if ( null == manifest.getUploadedBy() || manifest.getUploadedBy().isEmpty() ) {
            throw new IllegalArgumentException("uploadedBy is required parameter");
        }
        if ( null == manifest.getContainerRepoId() || manifest.getContainerRepoId().isEmpty() ) {
            throw new IllegalArgumentException("containerRepoId is required parameter");
        }
        if ( null == manifest.getTag() || manifest.getTag().isEmpty() ) {
            throw new IllegalArgumentException("tag is required parameter");
        }
        if ( null == manifest.getContentType() || ! manifest.getContentType().matches("^[^/]{1,127}/[^/]{1,127}$") ) {
            throw new IllegalArgumentException(
                "Illegal contentType="+manifest.getContentType()+" expected to match [^/]{1,127}/[^/]{1,127}");
        }

        String manifestId = manifest.getManifestId();
        if ( null == manifestId || ! Tag.isDigest(manifestId) ) {
            throw new IllegalArgumentException(
                "Illegal manifestId="+manifestId+" expected to match sha256:[0-9a-f]{64}");
        }

        // Validate digests (and add references):
        Set<String> digests = manifest.getDigests();
        if ( null == digests ) digests = Collections.emptySet();
        Set<String> unknownDigests = new HashSet<>();
        long totalSize = 0;
        for ( String digest : digests ) {
            Long size = _blobDb.addReference(digest, manifestId);
            if ( null == size ) {
                unknownDigests.add(digest);
            } else {
                totalSize += size;
            }
        }
        manifest.setVirtualSize(totalSize);
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
        RegistryManifest old = null;
        try {
            old = _main.putItem(manifest);
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
        return old;
    }

    public void remove(String domain, String repoId, String tag) {
        if ( null == domain ) domain = "d0";
        _main.deleteItem(domain, toRK(repoId, tag));
    }

    public RegistryManifest getManifestByRepoIdTag(String domain, String repoId, String tag) {
        if ( null == domain ) domain = "d0";
        return _main.getItem(domain, toRK(repoId, tag));
    }

    public List<RegistryManifest> listManifestsByRepoId(String domain, String repoId, PageIterator iterator) {
        if ( null == domain ) domain = "d0";

        String beginsWith = toRK(repoId, "");
        String marker = iterator.getMarker();
        String newMarker = null;
        if ( null != marker ) {
            newMarker = toRK(repoId, marker);
            iterator.marker(newMarker);
        }
        try {
            return _main.queryItems(domain, iterator)
                .beginsWith(beginsWith)
                .list();
        } finally {
            String finalMarker = iterator.getMarker();
            if ( null != finalMarker ) {
                if ( finalMarker.equals(newMarker) ) {
                    // Restore!
                    iterator.marker(marker);
                } else if ( finalMarker.startsWith(beginsWith) ) {
                    iterator.marker(finalMarker.substring(beginsWith.length()));
                } else {
                    throw new IllegalStateException(
                        "Expected marker to begin with "+beginsWith+", but got "+finalMarker);
                }
            }
        }
    }

    public List<MultiTaggedManifest> listMultiTaggedManifest(String domain, String repoId, PageIterator outerIter) {
        // Maybe we should throw an exception?
        if ( outerIter.getPageSize() <= 0 ) {
            return Collections.emptyList();
        }

        List<MultiTaggedManifest> result = new ArrayList<>();
        MultiTaggedManifest multiManifest = null;
        RegistryManifest lastManifest = null;
        RegistryManifest firstManifest = null;

        // We do page size 2x since we KNOW every manifest will have at least
        // a sha256 entry and possibly one or more tag entries. Ideally we only
        // do a single iteration of the outer loop.
        for ( PageIterator iter : new PageIterator()
                  .pageSize(outerIter.getPageSize()*2)
                  .marker(outerIter.getMarker())
                  .setIsForward(outerIter.isForward()) )
        {
            for ( RegistryManifest manifest : _byRepoManifestId.queryItems(domain.toLowerCase(), iter)
                      .beginsWith(toRepoManifestIdRK(repoId, null))
                      .list() )
            {
                // Update firstManifest:
                if ( null == firstManifest ) firstManifest = manifest;

                if ( null == multiManifest ||
                     ! multiManifest.getManifestId().equals(manifest.getManifestId()) )
                {
                    if ( result.size() >= outerIter.getPageSize() ) {
                        // Result set is full:
                        outerIter.setMarker(_byRepoManifestId.toMarker(lastManifest, true));
                        outerIter.setPrevMarker(_byRepoManifestId.toMarker(firstManifest, true));
                        return result;
                    }
                    // Add new MultiTaggedManifest:
                    multiManifest = MultiTaggedManifest.fromRegistryManifest(manifest);
                    result.add(multiManifest);
                } else if ( null != manifest.getTag() ) {
                    // Add tag to MultiTaggedManifest:
                    multiManifest.addTag(manifest.getTag());
                }
                // Update lastManifest
                lastManifest = manifest;
            }
        }
        // No more results:
        outerIter.setPrevMarker(outerIter.getMarker() == null ? null :
                                _byRepoManifestId.toMarker(firstManifest, true));
        outerIter.setMarker(null);
        return result;
    }
}
