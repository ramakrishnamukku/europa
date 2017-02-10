/*
  $Id: $
  @file RepoEventList.java
  @brief Contains the RepoEventList.java class

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
public class RepoEventList
{
    protected String nextMarker;
    protected String prevMarker;
    protected List<RepoEvent> events;
}
