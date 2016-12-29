/*
  $Id: $
  @file AjaxHelper.java
  @brief Contains the AjaxHelper.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import org.eclipse.jetty.http.HttpMethod;
import java.util.Set;
import java.util.HashSet;

public abstract class AjaxHelper
{
    protected Set<HttpMethod> supportedHttpMethods = new HashSet<HttpMethod>();

    public abstract Object get(AjaxRequest ajaxRequest);
    public boolean isMethodSupported(HttpMethod httpMethod) {
        if(supportedHttpMethods.size() == 0)
            return true;
        return supportedHttpMethods.contains(httpMethod);
    }
}
