/*
  $Id: $
  @file ContainerRepoDb.java
  @brief Contains the ContainerRepoDb.java class

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

import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.Constants;
import com.distelli.europa.ajax.*;
import com.distelli.europa.models.*;
import com.distelli.ventura.*;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContainerRepoDb
{
    private static final Logger log = Logger.getLogger(ContainerRepoDb.class);

    private Index<ContainerRepo> _main;
    private Index<ContainerRepo> _secondaryIndex;

    private EuropaConfiguration _europaConfiguration;

    private final ObjectMapper _om = new ObjectMapper();
    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(ContainerRepo.class)
        .put("hk", String.class,
             (item) -> getHashKey(item),
             (item, domain) -> setHashKey(item, domain))
        .put("id", String.class,
             (item) -> item.getId().toLowerCase(),
             (item, id) -> item.setId(id))
        .put("sidx", String.class,
             (item) -> getSecondaryKey(item.getProvider(), item.getRegion(), item.getName()))
        .put("prov", RegistryProvider.class, "provider")
        .put("region", String.class, "region")
        .put("name", String.class, "name")
        .put("rid", String.class, "registryId")
        .put("cid", String.class, "credId");
        return module;
    }

    private final String getHashKey(ContainerRepo repo)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(repo == null)
                throw(new IllegalArgumentException("Invalid null repo in multi-tenant setup"));
            return getHashKey(repo.getDomain());
        }
        return Constants.DOMAIN_ZERO;
    }

    private final String getHashKey(String domain)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(domain == null)
                throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for ContainerRepo"));

            return domain.toLowerCase();
        }
        return Constants.DOMAIN_ZERO;
    }

    private final void setHashKey(ContainerRepo repo, String domain)
    {
        if(_europaConfiguration.isMultiTenant())
        {
            if(domain == null)
                throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for ContainerRepo"));

            repo.setDomain(domain);
        }
        repo.setDomain(null);
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
                              ConvertMarker.Factory convertMarkerFactory,
                              EuropaConfiguration europaConfiguration) {
        _europaConfiguration = europaConfiguration;
        _om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(ContainerRepo.class)
        .withTableName("repos")
        .withNoEncrypt("hk", "id", "sidx")
        .withHashKeyName("hk")
        .withRangeKeyName("id")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "id"))
        .build();

        _secondaryIndex = indexFactory.create(ContainerRepo.class)
        .withIndexName("repos", "provider-index")
        .withNoEncrypt("hk", "id", "sidx")
        .withHashKeyName("hk")
        .withRangeKeyName("sidx")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "sidx"))
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
        if(_europaConfiguration.isMultiTenant() && repo.getDomain() == null)
            throw(new IllegalArgumentException("Invalid null domain in multi-tenant setup for ContainerRepo: "+
                                               repo));
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

    public ContainerRepo getRepo(String domain, String id)
    {
        return _main.getItem(getHashKey(domain),
                             id.toLowerCase());
    }
}
