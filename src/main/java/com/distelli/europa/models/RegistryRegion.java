/*
  $Id: $
  @file RegistryRegion.java
  @brief Contains the RegistryRegion.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryRegion
{
    protected String displayName;
    protected String regionCode;
}
