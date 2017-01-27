/*
  $Id: $
  @file DockerImage.java
  @brief Contains the DockerImage.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Builder;
import lombok.Singular;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerImage
{
    @Singular
    protected List<String> imageTags = null;
    protected String imageSha = null; // this is the MANIFEST sha!
    protected Long pushTime = null;
    protected Long imageSize = null; // this is the virtual size!

    public void addImageTag(String imageTag)
    {
        if(this.imageTags == null)
            imageTags = new ArrayList<String>();
        imageTags.add(imageTag);
    }
}
