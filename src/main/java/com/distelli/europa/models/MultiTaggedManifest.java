/*
  $Id: $
  @file MultiTaggedManifest.java
  @brief Contains the MultiTaggedManifest.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
   Same fields as the Registry Manifest but tags is a set and digests
   is removed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiTaggedManifest
{
    private String domain;
    private String containerRepoId;
    private TreeSet<String> tags;
    private String manifestId;
    private String uploadedBy;
    private String contentType;
    private Long virtualSize;
    private Long pushTime;

    public static MultiTaggedManifest fromRegistryManifest(RegistryManifest registryManifest)
    {
        MultiTaggedManifest multiTaggedManifest = MultiTaggedManifest
        .builder()
        .domain(registryManifest.getDomain())
        .containerRepoId(registryManifest.getContainerRepoId())
        .manifestId(registryManifest.getManifestId())
        .uploadedBy(registryManifest.getUploadedBy())
        .contentType(registryManifest.getContentType())
        .virtualSize(registryManifest.getVirtualSize())
        .pushTime(registryManifest.getPushTime())
        .build();

        multiTaggedManifest.addTag(registryManifest.getTag());
        return multiTaggedManifest;
    }

    public void addTag(String tag)
    {
        if(this.tags == null)
            this.tags = new TreeSet<String>();
        this.tags.add(tag);
    }
}
