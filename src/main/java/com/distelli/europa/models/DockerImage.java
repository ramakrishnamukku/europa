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

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerImage
{
    @Singular
    private List<String> imageTags = null;
    private String imageSha = null;
    private Long pushTime = null;
    private Long imageSize = null;

    public void addImageTag(String imageTag)
    {
        if(this.imageTags == null)
            imageTags = new ArrayList<String>();
        imageTags.add(imageTag);
    }
}
