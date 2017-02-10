/*
  $Id: $
  @file RegistryManifestList.java
  @brief Contains the RegistryManifestList.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryManifestList
{
    protected String nextMarker;
    protected String prevMarker;
    protected List<MultiTaggedManifest> manifests;
}
