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

    private final ObjectMapper _om = new ObjectMapper();
    public static TableDescription getTableDescription() {
        return TableDescription.builder()
        .tableName("settings")
        .indexes(Arrays.asList(IndexDescription.builder()
                               .hashKey(attr("dom", AttrType.STR))
                               .rangeKey(attr("rk", AttrType.STR))
                               .indexType(IndexType.MAIN_INDEX)
                               .readCapacity(1L)
                               .writeCapacity(1L)
                               .build()))
        .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(EuropaSetting.class)
        .put("dom", String.class,
             (setting) -> setting.getDomain().toLowerCase(),
             (setting, dom) -> setting.setDomain(dom))
        .put("rk", String.class,
             (setting) -> toRK(setting))
        .put("key", String.class, "key")
        .put("val", String.class, "value")
        .put("type", EuropaSettingType.class, "type");
        return module;
    }

    private String toRK(EuropaSetting setting) {
        return toRK(setting.getType(), setting.getKey());
    }

    private String toRK(EuropaSettingType type, String key) {
        return _dbKey.build(type.toString(), key);
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
    }

    public void save(EuropaSetting europaSetting) {
        if ( null == europaSetting.getDomain() || europaSetting.getDomain().trim().isEmpty()) {
            throw new IllegalArgumentException("domain must be non-null and non-empty");
        }
        if ( null == europaSetting.getType() ) {
            throw new IllegalArgumentException("type must be non-null");
        }
        if ( null == europaSetting.getKey() || europaSetting.getKey().trim().isEmpty()) {
            throw new IllegalArgumentException("key must be non-null and non-empty");
        }
        _main.putItem(europaSetting);
    }

    public void saveIfNotExists(EuropaSetting europaSetting) {
        if ( null == europaSetting.getDomain() || europaSetting.getDomain().trim().isEmpty()) {
            throw new IllegalArgumentException("domain must be non-null and non-empty");
        }
        if ( null == europaSetting.getType() ) {
            throw new IllegalArgumentException("type must be non-null");
        }
        if ( null == europaSetting.getKey() || europaSetting.getKey().trim().isEmpty()) {
            throw new IllegalArgumentException("key must be non-null and non-empty");
        }
        _main.putItemIfNotExists(europaSetting);
    }

    public void delete(String domain, EuropaSettingType type, String key) {
        _main.deleteItem(domain.toLowerCase(), toRK(type, key));
    }

    public EuropaSetting getSetting(String domain, EuropaSettingType type, String key) {
        return _main.getItem(domain.toLowerCase(), toRK(type, key));
    }

    public List<EuropaSetting> listRootSettingsByType(EuropaSettingType type) {
        return listSettingsByType(Constants.DOMAIN_ZERO, type);
    }

    public List<EuropaSetting> listSettingsByType(String domain, EuropaSettingType type) {
        return _main.queryItems(domain.toLowerCase(), new PageIterator().pageSize(1000))
        .beginsWith(_dbKey.buildPrefix(type.toString()))
        .list();
    }
}
