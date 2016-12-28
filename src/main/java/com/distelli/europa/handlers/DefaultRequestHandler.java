/*
  $Id: $
  @file DefaultRequestHandler.java
  @brief Contains the DefaultRequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import com.distelli.ventura.*;
import lombok.extern.log4j.Log4j;

@Log4j
public class DefaultRequestHandler extends RequestHandler
{
    public DefaultRequestHandler()
    {

    }

    public WebResponse handleRequest(RequestContext requestContext)
    {
        return renderPage(requestContext, null);
    }
}
