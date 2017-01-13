/*
  $Id: $
  @file WebAppRoutes.java
  @brief Contains the WebAppRoutes.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import com.distelli.webserver.AjaxRequestHandler;
import com.distelli.webserver.RouteMatcher;
import com.distelli.europa.handlers.*;
import lombok.extern.log4j.Log4j;

@Log4j
public class WebAppRoutes
{
    private static final RouteMatcher ROUTES = new RouteMatcher();

    public static RouteMatcher getRouteMatcher() {
        return ROUTES;
    }

    static {
        //Add the routes below this line
        //Ajax Routes
        ROUTES.add("GET", "/ajax", AjaxRequestHandler.class);
        ROUTES.add("POST", "/ajax", AjaxRequestHandler.class);

        ROUTES.setDefaultRequestHandler(DefaultRequestHandler.class);
    }
}
