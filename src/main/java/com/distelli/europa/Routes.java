/*
  $Id: $
  @file Routes.java
  @brief Contains the Routes.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import com.distelli.europa.webserver.RouteMatcher;
import com.distelli.europa.handlers.*;

public class Routes
{
    private static final RouteMatcher ROUTES = new RouteMatcher();
    public static RouteMatcher getRouteMatcher() {
        return ROUTES;
    }

    static {
        //Add the routes below this line
        //    ROUTES.add("GET", "/:username/path/foo", FooRequestHandler.class);

        //Ajax Routes
        ROUTES.add("GET", "/ajax", AjaxRequestHandler.class);
        ROUTES.add("POST", "/ajax", AjaxRequestHandler.class);

        //set the default request handler TODO: Remove this and set
        //the static content request handler as the default (or maybe
        //the NotFoundRequestHandler)!
        ROUTES.setDefaultRequestHandler(DefaultRequestHandler.class);
    }
}
