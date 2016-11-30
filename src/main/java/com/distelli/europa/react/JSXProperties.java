/*
  $Id: $
  @file JSXProperties.java
  @brief Contains the JSXProperties.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.react;

import org.apache.log4j.Logger;
import com.distelli.europa.webserver.*;

public class JSXProperties
{
    private static final Logger log = Logger.getLogger(JSXProperties.class);

    private RequestContext _requestContext;

    public JSXProperties(RequestContext requestContext)
    {
        _requestContext = requestContext;
    }
}
