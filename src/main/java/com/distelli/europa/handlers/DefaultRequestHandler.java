/*
  $Id: $
  @file DefaultRequestHandler.java
  @brief Contains the DefaultRequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import org.apache.log4j.Logger;
import com.distelli.europa.webserver.*;

public class DefaultRequestHandler extends RequestHandler
{
    private static final Logger log = Logger.getLogger(DefaultRequestHandler.class);

    public DefaultRequestHandler()
    {

    }

    public WebResponse handleRequest(RequestContext requestContext)
    {
        return renderPage(requestContext, null);
    }
}
