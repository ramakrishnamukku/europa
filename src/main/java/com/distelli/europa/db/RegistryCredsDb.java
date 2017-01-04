/*
  $Id: $
  @file RegistryCredsDb.java
  @brief Contains the RegistryCredsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.List;
import com.distelli.europa.Constants;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.Index;
import com.distelli.persistence.PageIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;

import com.distelli.europa.ajax.*;
import com.distelli.europa.models.*;
import com.distelli.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryCredsDb
{
    private Index<RegistryCred> _main;
    private Index<RegistryCred> _secondaryIndex;

    private EuropaConfiguration _europaConfiguration;

    private final ObjectMapper _om = new ObjectMapper();
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
        .put("sec", String.class, "secret")
        .put("region", String.class, "region")
        .put("desc", String.class, "description")
        .put("name", String.class, "name")
        .put("rp", RegistryProvider.class, "provider");
        return module;
    }

    @Inject
    protected RegistryCredsDb(Index.Factory indexFactory,
                              ConvertMarker.Factory convertMarkerFactory,
                              EuropaConfiguration europaConfiguration)
    {
        _europaConfiguration = europaConfiguration;
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
        .withIndexName("creds", "provider-name")
        .withNoEncrypt("hk", "id", "sidx")
        .withHashKeyName("hk")
        .withRangeKeyName("sidx")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "sidx"))
        .build();
    }

    private final String getHashKey(RegistryCred cred)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(cred == null)
                throw(new IllegalArgumentException("Invalid null cred in multi-tenant setup"));
            return getHashKey(cred.getDomain());
        }
        return Constants.DOMAIN_ZERO;
    }

    private final String getHashKey(String domain)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(domain == null)
                throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for RegistryCred"));

            return domain.toLowerCase();
        }
        return Constants.DOMAIN_ZERO;
    }

    private final void setHashKey(RegistryCred cred, String domain)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(domain == null)
                throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for RegistryCred"));

            cred.setDomain(domain);
        }
        cred.setDomain(null);
    }

    private final String getSecondaryRangeKey(RegistryProvider provider, String region, String name)
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
        if(_europaConfiguration.isMultiTenant() && cred.getDomain() == null)
            throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for RegistryCred: "+
                                               cred));
        _main.putItem(cred);
    }

    public List<RegistryCred> listAllCreds(String domain, PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(domain), pageIterator).list();
    }

    public List<RegistryCred> listCredsForProvider(String domain, RegistryProvider provider, PageIterator pageIterator)
    {
        String rangeKey = String.format("%s:", provider.toString().toLowerCase());
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
