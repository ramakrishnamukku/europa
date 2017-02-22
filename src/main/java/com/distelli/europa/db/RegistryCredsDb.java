/*
  $Id: $
  @file RegistryCredsDb.java
  @brief Contains the RegistryCredsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import com.distelli.europa.Constants;
import com.distelli.persistence.IndexDescription;
import com.distelli.europa.ajax.*;
import com.distelli.europa.models.*;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.*;
import com.distelli.persistence.TableDescription;
import com.distelli.persistence.AttrType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryCredsDb extends BaseDb
{
    private Index<RegistryCred> _main;
    private Index<RegistryCred> _secondaryIndex;

    private final ObjectMapper _om = new ObjectMapper();
    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName("creds")
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
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(RegistryCred.class)
        .put("hk", String.class,
             (item) -> getHashKey(item),
             (item, domain) -> setHashKey(item, domain))
        .put("id", String.class,
             (item) -> item.getId().toLowerCase(),
             (item, id) -> item.setId(id.toLowerCase()))
        .put("sidx", String.class,
             (item) -> getSecondaryRangeKey(item.getProvider(),
                                            item.getRegion(),
                                            item.getName()))
        .put("ctime", Long.class, "created")
        .put("kid", String.class, "key")
        .put("unam", String.class, "username")
        .put("sec", String.class, "secret")
        .put("pwd", String.class, "password")
        .put("region", String.class, "region")
        .put("desc", String.class, "description")
        .put("name", String.class, "name")
        .put("endpt", String.class, "endpoint")
        .put("rp", RegistryProvider.class, "provider");
        return module;
    }

    @Inject
    protected RegistryCredsDb(Index.Factory indexFactory,
                              ConvertMarker.Factory convertMarkerFactory)
    {
        _om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(RegistryCred.class)
        .withTableName("creds")
        .withNoEncrypt("hk", "id", "pidx")
        .withHashKeyName("hk")
        .withRangeKeyName("id")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "rk"))
        .build();

        _secondaryIndex = indexFactory.create(RegistryCred.class)
        .withIndexName("creds", "hk-sidx-index")
        .withNoEncrypt("hk", "id", "sidx")
        .withHashKeyName("hk")
        .withRangeKeyName("sidx")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "id", "sidx"))
        .build();
    }

    private final String getHashKey(RegistryCred cred)
    {
        return getHashKey(cred.getDomain());
    }

    private final String getHashKey(String domain)
    {
        return domain.toLowerCase();
    }

    private final void setHashKey(RegistryCred cred, String domain)
    {
        cred.setDomain(domain);
    }

    private final String getSecondaryRangeKey(RegistryProvider provider, String region, String name)
    {
        return _dbKey.build(provider.toString().toLowerCase(),
                            region.toLowerCase(),
                            name.toLowerCase());
    }

    public void save(RegistryCred cred)
    {
        String region = cred.getRegion();
        String name = cred.getName();
        if(region == null)
            throw(new AjaxClientException("Invalid Region "+region+" in Registry Cred", JsonError.Codes.BadContent, 400));
        if(name == null || name.trim().isEmpty())
            throw(new AjaxClientException("Invalid Name "+name+" in Registry Cred", JsonError.Codes.BadContent, 400));
        String id = cred.getId();
        if(id == null)
            throw(new IllegalArgumentException("Invalid id "+id+" in Registry Cred"));
        if(cred.getDomain() == null)
            throw(new IllegalArgumentException("Invalid null domain for RegistryCred: "+cred));
        _main.putItem(cred);
    }

    public List<RegistryCred> listAllCreds(String domain, PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(domain), pageIterator).list();
    }

    public List<RegistryCred> listCredsForProvider(String domain, RegistryProvider provider, PageIterator pageIterator)
    {
        String rangeKey = _dbKey.buildPrefix(provider.toString().toLowerCase());
        return _secondaryIndex.queryItems(getHashKey(domain), pageIterator)
        .beginsWith(rangeKey)
        .list();
    }

    public RegistryCred getCred(String domain, String id)
    {
        return _main.getItem(getHashKey(domain),
                             id.toLowerCase());
    }

    public RegistryCred getCred(String domain, RegistryProvider provider, String region, String name)
    {
        return _secondaryIndex.getItem(getHashKey(domain),
                                       getSecondaryRangeKey(provider, region, name));
    }

    public void deleteCred(String domain, String id)
    {
        _main.deleteItem(getHashKey(domain),
                         id.toLowerCase());
    }
}
