/*
  $Id: $
  @file RegistryCredsDb.java
  @brief Contains the RegistryCredsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.List;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.Index;
import com.distelli.persistence.PageIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;

import com.distelli.europa.ajax.*;
import com.distelli.europa.models.*;
import com.distelli.europa.webserver.*;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RegistryCredsDb
{
    private static final Logger log = Logger.getLogger(RegistryCredsDb.class);

    private Index<RegistryCred> _main;
    private Index<RegistryCred> _secondaryIndex;

    private static final ObjectMapper om = new ObjectMapper();
    private static TransformModule createTransforms(TransformModule module) {
        module.createTransform(RegistryCred.class)
        .put("hk", String.class,
             (item) -> getHashKey(item))
        .put("id", String.class, "id")
        .put("sidx", String.class,
             (item) -> getSecondaryRangeKey(item.getProvider(),
                                            item.getRegion(),
                                            item.getName()))
        .put("ctime", Long.class, "created")
        .put("kid", String.class, "key")
        .put("sec", String.class, "secret")
        .put("region", String.class, "region")
        .put("desc", String.class, "description")
        .put("name", String.class, "name")
        .put("rp", RegistryProvider.class, "provider");
        return module;
    }

    @Inject
    protected RegistryCredsDb(Index.Factory indexFactory, ConvertMarker.Factory convertMarkerFactory) {
        om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(RegistryCred.class)
        .withTableName("creds")
        .withNoEncrypt("hk", "id", "pidx")
        .withHashKeyName("hk")
        .withRangeKeyName("id")
        .withConvertValue(om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "rk"))
        .build();

        _secondaryIndex = indexFactory.create(RegistryCred.class)
        .withIndexName("creds", "provider-name")
        .withNoEncrypt("hk", "id", "sidx")
        .withHashKeyName("hk")
        .withRangeKeyName("sidx")
        .withConvertValue(om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "sidx"))
        .build();
    }

    private static final String getHashKey(RegistryCred cred)
    {
        return "1";
    }

    private static final String getSecondaryRangeKey(RegistryProvider provider, String region, String name)
    {
        return String.format("%s:%s:%s",
                             provider.toString().toLowerCase(),
                             region.toLowerCase(),
                             name.toLowerCase());
    }

    public void save(RegistryCred cred)
    {
        String region = cred.getRegion();
        String name = cred.getName();
        if(region == null || region.contains(":"))
            throw(new AjaxClientException("Invalid Region "+region+" in Registry Cred", JsonError.Codes.BadContent, 400));
        if(name == null || name.contains(":"))
            throw(new AjaxClientException("Invalid Name "+name+" in Registry Cred", JsonError.Codes.BadContent, 400));
        String id = cred.getId();
        if(id == null)
            throw(new IllegalArgumentException("Invalid id "+id+" in Registry Cred"));
        _main.putItem(cred);
    }

    public List<RegistryCred> listAllCreds(PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(null), pageIterator).list();
    }

    public List<RegistryCred> listCredsForProvider(RegistryProvider provider, PageIterator pageIterator)
    {
        String rangeKey = String.format("%s:", provider.toString().toLowerCase());
        return _secondaryIndex.queryItems(getHashKey(null), pageIterator)
        .beginsWith(rangeKey)
        .list();
    }

    public RegistryCred getCred(String id)
    {
        return _main.getItem(getHashKey(null),
                             id.toLowerCase());
    }

    public RegistryCred getCred(RegistryProvider provider, String region, String name)
    {
        return _secondaryIndex.getItem(getHashKey(null),
                                       getSecondaryRangeKey(provider, region, name));
    }

    public void deleteCred(String id)
    {
        _main.deleteItem(getHashKey(null),
                         id.toLowerCase());
    }
}
