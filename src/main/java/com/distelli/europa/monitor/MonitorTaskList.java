/*
  $Id: $
  @file MonitorTaskList.java
  @brief Contains the MonitorTaskList.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.monitor;

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
public class MonitorTaskList
{
    @Singular
    protected List<MonitorTask> tasks;
}
