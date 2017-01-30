/*
  $Id: $
  @file EuropaSetting.java
  @brief Contains the EuropaSetting.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EuropaSetting
{
    protected String domain;
    protected String key;
    protected String value;
    protected EuropaSettingType type;

    public static Map<String, String> asMap(List<EuropaSetting> settings) {
        Map<String, String> asMap = new LinkedHashMap<>();
        for ( EuropaSetting setting : settings ) {
            asMap.put(setting.getKey(), setting.getValue());
        }
        return asMap;
    }
}
