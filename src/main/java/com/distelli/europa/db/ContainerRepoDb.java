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

import com.distelli.europa.models.*;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContainerRepoDb
{
    private static final Logger log = Logger.getLogger(ContainerRepoDb.class);

    private Index<ContainerRepo> _main;

    private static final ObjectMapper om = new ObjectMapper();
    private static TransformModule createTransforms(TransformModule module) {
        module.createTransform(ContainerRepo.class)
        .put("hk", String.class,
             (item) -> getHashKey(item))
        .put("rk", String.class,
             (item) -> getRangeKey(item.getProvider(), item.getRegion(), item.getName()))
        .put("name", String.class, "name")
        .put("region", String.class, "region")
        .put("rp", RegistryProvider.class, "provider");
        return module;
    }

    private static final String getHashKey(ContainerRepo repo)
    {
        return "1";
    }

    private static final String getRangeKey(RegistryProvider provider, String region, String name)
    {
        return String.format("%s:%s:%s",
                             provider.toString().toLowerCase(),
                             region.toLowerCase(),
                             name.toLowerCase());
    }

    @Inject
    protected ContainerRepoDb(Index.Factory indexFactory, ConvertMarker.Factory convertMarkerFactory) {
        om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(ContainerRepo.class)
        .withTableName("repos")
        .withNoEncrypt("hk", "rk")
        .withHashKeyName("hk")
        .withRangeKeyName("rk")
        .withConvertValue(om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "rk"))
        .build();
    }

    public void save(ContainerRepo repo)
    {
        String region = repo.getRegion();
        if(region == null || region.contains(":"))
            throw(new IllegalArgumentException("Invalid Region "+region+" in Container Repo"));
        String name = repo.getName();
        if(name == null || name.contains(":"))
            throw(new IllegalArgumentException("Invalid Name "+name+" in Container Repo"));
        _main.putItem(repo);
    }

    public void deleteRepo(RegistryProvider provider, String region, String name)
    {
        _main.deleteItem(getHashKey(null),
                         getRangeKey(provider, region, name));
    }

    public List<ContainerRepo> listRepos(PageIterator pageIterator)
    {
        return _main.queryItems(getHashKey(null), pageIterator).list();
    }

    public List<ContainerRepo> listRepos(RegistryProvider provider, PageIterator pageIterator)
    {
        String rangeKey = String.format("%s:", provider.toString().toLowerCase());
        return _main.queryItems(getHashKey(null), pageIterator)
        .beginsWith(rangeKey)
        .list();
    }

    public List<ContainerRepo> listRepos(RegistryProvider provider, String region, PageIterator pageIterator)
    {
        String rangeKey = String.format("%s:%s:",
                                        provider.toString().toLowerCase(),
                                        region.toLowerCase());
        return _main.queryItems(getHashKey(null), pageIterator)
        .beginsWith(rangeKey)
        .list();
    }

    public ContainerRepo getRepo(RegistryProvider provider, String region, String name)
    {
        return _main.getItem(getHashKey(null),
                             getRangeKey(provider, region, name));
    }
}
