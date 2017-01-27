/*
  $Id: $
  @file SettingsDb.java
  @brief Contains the SettingsDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import com.distelli.europa.models.EuropaSetting;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.distelli.europa.Constants;
import com.distelli.europa.db.BaseDb;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class SettingsDb extends BaseDb
{
    private Index<EuropaSetting> _main;
    private Index<EuropaSetting> _byType;

    private final ObjectMapper _om = new ObjectMapper();
    public static TableDescription getTableDescription() {
        return TableDescription.builder()
        .tableName("settings")
        .indexes(Arrays.asList(IndexDescription.builder()
                               .hashKey(attr("dom", AttrType.STR))
                               .rangeKey(attr("key", AttrType.STR))
                               .indexType(IndexType.MAIN_INDEX)
                               .readCapacity(1L)
                               .writeCapacity(1L)
                               .build(),
                               IndexDescription.builder()
                               .indexName("dom-type-index")
                               .hashKey(attr("dom", AttrType.STR))
                               .rangeKey(attr("type", AttrType.STR))
                               .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                               .readCapacity(1L)
                               .writeCapacity(1L)
                               .build()))
        .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(EuropaSetting.class)
        .put("dom", String.class, "domain")
        .put("key", String.class, "key")
        .put("val", String.class, "value")
        .put("type", EuropaSettingType.class, "type");
        return module;
    }

    @Inject
    public SettingsDb(Index.Factory indexFactory,
                      ConvertMarker.Factory convertMarkerFactory)
    {
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(EuropaSetting.class)
        .withTableDescription(getTableDescription())
        .withConvertValue(_om::convertValue)
        .build();

        _byType = indexFactory.create(EuropaSetting.class)
        .withTableDescription(getTableDescription(), "dom-type-index")
        .withConvertValue(_om::convertValue)
        .build();
    }

    public void save(EuropaSetting europaSetting) {
        _main.putItem(europaSetting);
    }

    public List<EuropaSetting> listRootSettingsByType(EuropaSettingType type) {
        return listSettingsByType(Constants.DOMAIN_ZERO, type);
    }

    public List<EuropaSetting> listSettingsByType(String domain, EuropaSettingType type) {
        return _byType.queryItems(domain.toLowerCase(), new PageIterator().pageSize(1000))
        .eq(type.toString().toLowerCase())
        .list();
    }
}
