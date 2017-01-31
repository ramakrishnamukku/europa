/*
  $Id: $
  @file ContainerRepoDb.java
  @brief Contains the ContainerRepoDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import com.distelli.europa.Constants;
import com.distelli.europa.ajax.*;
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
import com.distelli.webserver.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.util.Map;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ContainerRepoDb extends BaseDb
{
    private Index<ContainerRepo> _main;
    private Index<ContainerRepo> _secondaryIndex;
    private Index<ContainerRepo> _byCredId;

    private final ObjectMapper _om = new ObjectMapper();

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName("repos")
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("hk", AttrType.STR))
                    .rangeKey(attr("id", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName("hk-sidx-index")
                    .hashKey(attr("hk", AttrType.STR))
                    .rangeKey(attr("sidx", AttrType.STR))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName("hk-cid-index")
                    .hashKey(attr("hk", AttrType.STR))
                    .rangeKey(attr("cid", AttrType.STR))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(ContainerRepo.class)
        .put("hk", String.class,
             (item) -> getHashKey(item),
             (item, domain) -> setHashKey(item, domain))
        .put("id", String.class,
             (item) -> item.getId().toLowerCase(),
             (item, id) -> item.setId(id.toLowerCase()))
        .put("sidx", String.class,
             (item) -> getSecondaryKey(item.getProvider(), item.getRegion(), item.getName()))
        .put("prov", RegistryProvider.class, "provider")
        .put("region", String.class, "region")
        .put("name", String.class, "name")
        .put("rid", String.class, "registryId")
        .put("cid", String.class, "credId")
        .put("pr", Boolean.class, "publicRepo")
        .put("oid", String.class, "overviewId")
        .put("lr", Boolean.class, "local")
        .put("lst", Long.class, "lastSyncTime")
        .put("levent", RepoEvent.class, "lastEvent");
        return module;
    }

    private final String getHashKey(ContainerRepo repo)
    {
        return getHashKey(repo.getDomain());
    }

    private final String getHashKey(String domain)
    {
        return domain.toLowerCase();
    }

    private final void setHashKey(ContainerRepo repo, String domain)
    {
        repo.setDomain(domain);
    }

    private final String getSecondaryKey(RegistryProvider provider, String region, String name)
    {
        return String.format("%s:%s:%s",
                             provider.toString().toLowerCase(),
                             region.toLowerCase(),
                             name.toLowerCase());
    }

    @Inject
    protected ContainerRepoDb(Index.Factory indexFactory,
                              ConvertMarker.Factory convertMarkerFactory) {
        _om.registerModule(createTransforms(new TransformModule()));
        _om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        _main = indexFactory.create(ContainerRepo.class)
        .withTableName("repos")
        .withNoEncrypt("hk", "id", "sidx", "cid")
        .withHashKeyName("hk")
        .withRangeKeyName("id")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "id"))
        .build();

        _secondaryIndex = indexFactory.create(ContainerRepo.class)
        .withIndexName("repos", "hk-sidx-index")
        .withNoEncrypt("hk", "id", "sidx", "cid")
        .withHashKeyName("hk")
        .withRangeKeyName("sidx")
        .withConvertValue(_om::convertValue)
            // Custom convert marker implementation to support /v2/_catalog API:
            .withConvertMarker(new ConvertMarker() {
                    public String toMarker(Map<String, Object> attributes, boolean hasHashKey) {
                        if ( hasHashKey ) {
                            return attributes.get("sidx") + ":" +
                                attributes.get("id");
                        }
                        // TODO: implement...
                        throw new UnsupportedOperationException("scan is not supported");
                    }
                    public Attribute[] fromMarker(Object hk, String marker) {
                        String[] attrs = marker.split(":", 4);
                        if ( null == attrs || attrs.length != 4 ) {
                            throw new IllegalArgumentException(
                                "Expected marker in format <provider>:<region>:<name>:<id> got="+marker);
                        }
                        return new Attribute[] {
                            new Attribute()
                            .withName("hk")
                            .withValue(hk),
                            new Attribute()
                            .withName("sidx")
                            .withValue(getSecondaryKey(
                                           RegistryProvider.valueOf(attrs[0].toUpperCase()),
                                           attrs[1],
                                           attrs[2])),
                            new Attribute()
                            .withName("id")
                            .withValue(attrs[3])
                        };
                    }
                })
        .build();

        _byCredId = indexFactory.create(ContainerRepo.class)
        .withIndexName("repos", "hk-cid-index")
        .withNoEncrypt("hk", "id", "sidx", "cid")
        .withHashKeyName("hk")
        .withRangeKeyName("cid")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "cid", "id"))
        .build();
    }

    public void save(ContainerRepo repo)
    {
        String region = repo.getRegion();
        if(region == null || region.contains(":"))
            throw(new AjaxClientException("Invalid Region "+region+" in Container Repo", JsonError.Codes.BadContent, 400));
        String name = repo.getName();
        if(name == null || name.contains(":"))
            throw(new AjaxClientException("Invalid Name "+name+" in Container Repo", JsonError.Codes.BadContent, 400));
        String id = repo.getId();
        if(id == null)
            throw(new IllegalArgumentException("Invalid id "+id+" in container repo"));
        if(repo.getDomain() == null)
            throw(new IllegalArgumentException("Invalid null domain for ContainerRepo: "+repo));
        _main.putItem(repo);
    }

    public void deleteRepo(String domain, String id)
    {
        _main.deleteItem(getHashKey(domain),
                         id.toLowerCase());
    }

    public List<ContainerRepo> listRepos(String domain, PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(domain), pageIterator).list();
    }

    public List<ContainerRepo> listRepos(String domain,
                                         RegistryProvider provider,
                                         PageIterator pageIterator)
    {
        String rangeKey = String.format("%s:", provider.toString().toLowerCase());
        return _secondaryIndex.queryItems(getHashKey(domain), pageIterator)
        .beginsWith(rangeKey)
        .list();
    }

    public List<ContainerRepo> listEuropaRepos(String domain,
                                               PageIterator pageIterator)
    {
        String marker = pageIterator.getMarker();
        String newMarker = null;
        if ( null != marker ) {
            String sidx = getSecondaryKey(
                RegistryProvider.EUROPA,
                "",
                marker);
            ContainerRepo repo = null;
            for ( PageIterator it : new PageIterator().pageSize(100) ) {
                List<ContainerRepo> list = _secondaryIndex.queryItems(getHashKey(domain), it)
                    .eq(sidx)
                    .list();
                if ( list.size() > 0 ) repo = list.get(list.size()-1);
            }
            if ( null == repo ) {
                newMarker = sidx + ":";
            } else {
                newMarker = _secondaryIndex.toMarker(repo, true);
            }
            pageIterator.marker(newMarker);
        }

        String rangeKey = String.format("%s:", RegistryProvider.EUROPA.toString().toLowerCase());
        try {
            return _secondaryIndex.queryItems(getHashKey(domain), pageIterator)
                .beginsWith(rangeKey)
                .list();
        } finally {
            String outMarker = pageIterator.getMarker();
            if ( null != outMarker ) {
                if ( newMarker != null && newMarker.equals(outMarker) ) {
                    // restore...
                    pageIterator.marker(marker);
                } else if ( outMarker.startsWith(rangeKey) ) {
                    String[] attrs = outMarker.split(":", 4);
                    if ( attrs.length != 4 ) throw new IllegalStateException("Unexpected marker="+marker);
                    pageIterator.marker(attrs[2]);
                } else {
                    throw new IllegalStateException(
                        "Expected marker to begin with "+rangeKey+" marker="+outMarker);
                }
            }
        }
    }

    public List<ContainerRepo> listRepos(String domain,
                                         RegistryProvider provider,
                                         String region,
                                         PageIterator pageIterator)
    {
        String rangeKey = String.format("%s:%s:",
                                        provider.toString().toLowerCase(),
                                        region.toLowerCase());
        return _main.queryItems(getHashKey(domain), pageIterator)
        .beginsWith(rangeKey)
        .list();
    }

    public ContainerRepo getRepo(String domain,
                                 RegistryProvider provider,
                                 String region,
                                 String name)
    {
        List<ContainerRepo> repos =
            _secondaryIndex.queryItems(getHashKey(domain), new PageIterator().pageSize(1))
            .eq(getSecondaryKey(provider, region, name))
            .list();
        if ( repos.size() < 1 ) return null;
        return repos.get(0);
    }

    public ContainerRepo getLocalRepo(String domain,
                                      String name)
    {
        for(PageIterator iter : new PageIterator().pageSize(1000))
        {
            List<ContainerRepo> repos = _secondaryIndex.queryItems(getHashKey(domain), iter)
            .eq(getSecondaryKey(RegistryProvider.EUROPA, "", name))
            .list();

            for(ContainerRepo repo : repos)
            {
                if(repo.isLocal())
                    return repo;
            }
        }

        return null;
    }

    public boolean repoExists(String domain,
                              RegistryProvider provider,
                              String region,
                              String name)
    {
        return null != getRepo(domain, provider, region, name);
    }

    public ContainerRepo getRepo(String domain, String id)
    {
        return _main.getItem(getHashKey(domain),
                             id.toLowerCase());
    }

    public void setLastEvent(String domain, String id, RepoEvent lastEvent)
    {
        _main.updateItem(getHashKey(domain),
                         id.toLowerCase())
        .set("levent", lastEvent)
        .when((expr) -> expr.eq("id", id.toLowerCase()));
    }

    public void setRepoPublic(String domain, String id)
    {
        _main.updateItem(getHashKey(domain),
                         id.toLowerCase())
        .set("pr", true)
        .when((expr) -> expr.eq("id", id.toLowerCase()));
    }

    public void setRepoPrivate(String domain, String id)
    {
        _main.updateItem(getHashKey(domain),
                         id.toLowerCase())
        .set("pr", false)
        .when((expr) -> expr.eq("id", id.toLowerCase()));
    }

    public void setLastSyncTime(String domain, String id, long lastSyncTime)
    {
        _main.updateItem(getHashKey(domain),
                         id.toLowerCase())
        .set("lst", lastSyncTime)
        .when((expr) -> expr.eq("id", id.toLowerCase()));
    }

    public List<ContainerRepo> listReposByCred(String domain, String credId, PageIterator pageIterator)
    {
        return _byCredId.queryItems(getHashKey(domain),
                                    pageIterator)
        .eq(credId.toLowerCase())
        .list();
    }
}
