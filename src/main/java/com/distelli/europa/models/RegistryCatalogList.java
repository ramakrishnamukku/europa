/*
  $Id: $
  @file RegistryCatalogList.java
  @brief Contains the RegistryCatalogList.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryCatalogList
{
    @Singular
    private List<String> repositories;
}
