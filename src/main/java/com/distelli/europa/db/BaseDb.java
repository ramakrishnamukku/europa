/*
  $Id: $
  @file BaseDb.java
  @brief Contains the BaseDb.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import lombok.extern.log4j.Log4j;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;


@Log4j
public class BaseDb
{
    protected static AttrDescription attr(String name, AttrType type) {
        return AttrDescription.builder()
            .attrName(name)
            .attrType(type)
            .build();
    }
}
