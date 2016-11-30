/*
  $Id: $
  @file RequestHandlerFactory.java
  @brief Contains the RequestHandlerFactory.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

import org.apache.log4j.Logger;
import com.google.inject.Injector;

public class RequestHandlerFactory
{
    private Injector _injector;
    public RequestHandlerFactory(Injector injector)
    {
        _injector = injector;
    }

    public RequestHandler getRequestHandler(MatchedRoute route) {
        return _injector.getInstance(route.getRequestHandler());
    }
}
