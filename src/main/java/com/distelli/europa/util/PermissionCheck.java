/*
  $Id: $
  @file PermissionCheck.java
  @brief Contains the PermissionCheck.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.webserver.AjaxRequest;

public interface PermissionCheck
{
    public default void check(AjaxRequest ajaxRequest, EuropaRequestContext requestContext, Object... params)
    {

    }
}
