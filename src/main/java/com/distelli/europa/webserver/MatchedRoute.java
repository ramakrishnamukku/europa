/*
  $Id: $
  @file MatchedRoute.java
  @brief Contains the MatchedRoute.java class

  All Rights Reserved.

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.webserver;

import java.util.Map;
import org.eclipse.jetty.http.HttpMethod;

public class MatchedRoute
{
    private RouteSpec _routeSpec = null;
    private Map<String, String> _routeParams = null;

    public MatchedRoute(RouteSpec routeSpec, Map<String, String> routeParams)
    {
        _routeSpec = routeSpec;
        _routeParams = routeParams;
    }

    public String getPath()
    {
        return _routeSpec.getPath();
    }

    public HttpMethod getHttpMethod()
    {
        return _routeSpec.getHttpMethod();
    }

    public Map<String, String> getParams()
    {
        return _routeParams;
    }

    public Class<? extends RequestHandler> getRequestHandler()
    {
        return _routeSpec.getRequestHandler();
    }
}
