/*
  $Id: $
  @file NotFoundHandler.java
  @brief Contains the NotFoundHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import com.distelli.ventura.*;

public class NotFoundHandler extends RequestHandler
{
    public NotFoundHandler()
    {

    }

    public WebResponse handleRequest(RequestContext requestContext)
    {
        return notFound("<h1>PAGE NOT FOUND</h1>");
    }
}
