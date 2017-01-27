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
}
