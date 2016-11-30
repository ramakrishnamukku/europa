/*
  $Id: $
  @file RouteMatcher.java
  @brief Contains the RouteMatcher.java class

  All Rights Reserved.

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.webserver;

import org.eclipse.jetty.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
   The route matcher matches HTTP routes against a resourceURI

   Wildcards - * matches anything
*/
public class RouteMatcher
{
    private ArrayList<RouteSpec> _routes = null;
    private Class<? extends RequestHandler> _defaultRequestHandler = null;

    public RouteMatcher()
    {
        _routes = new ArrayList<RouteSpec>();
    }

    public List<RouteSpec> getAllRoutes()
    {
        return _routes;
    }

    public void add(String httpMethod, String path, Class<? extends RequestHandler> requestHandler)
    {
        HttpMethod httpMethodEnum = HttpMethod.valueOf(httpMethod);
        RouteSpec routeSpec = new RouteSpec(path, httpMethodEnum, requestHandler);
        _routes.add(routeSpec);
    }

    public void setDefaultRequestHandler(Class<? extends RequestHandler> requestHandler)
    {
        _defaultRequestHandler = requestHandler;
    }

    public MatchedRoute match(HttpMethod httpMethod, String path)
    {
        Map<String, String> routeParams = new HashMap<String, String>();
        for(RouteSpec routeSpec : _routes)
        {
            if(matches(routeSpec.getPath(), routeSpec.getHttpMethod(), httpMethod, path, routeParams))
            {
                MatchedRoute matchedRoute = new MatchedRoute(routeSpec, routeParams);
                return matchedRoute;
            }
        }

        if(_defaultRequestHandler == null)
            return null;
        //else return the default route
        RouteSpec defaultRouteSpec = new RouteSpec(path, httpMethod, _defaultRequestHandler);
        MatchedRoute defaultRoute = new MatchedRoute(defaultRouteSpec, null);
        return defaultRoute;
    }

    private static boolean matches(String route, HttpMethod routeMethod, HttpMethod requestMethod, String resourceURI, Map<String, String> routeParams)
    {
        return matches(route, new HttpMethod[]{routeMethod}, requestMethod, resourceURI, routeParams);
    }

    private static boolean matches(String route, HttpMethod[] routeMethods, HttpMethod requestMethod, String resourceURI, Map<String, String> routeParams)
    {
        boolean methodMatches = false;
        for(HttpMethod method : routeMethods)
        {
            if(method == requestMethod)
            {
                methodMatches = true;
                break;
            }
        }

        if(!methodMatches)
            return false;

        String regexRoute = route.replaceAll(":[a-zA-Z0-9_\\\\-\\\\.~]*", "(*)");
        regexRoute = regexRoute.replaceAll("\\*", "[a-zA-Z0-9_\\\\-\\\\.~]*");

        Pattern pattern = Pattern.compile(regexRoute);
        Matcher matcher = pattern.matcher(resourceURI);

        boolean matches = matcher.matches();
        if(!matches)
            return false;

        int groupCount = matcher.groupCount();
        if(groupCount <= 0)
            return true;

        if(routeParams == null)
            return true;

        String[] parts = route.split("/");
        int index = 1;
        for(String part : parts)
        {
            if(part.startsWith(":"))
            {
                String paramVal = matcher.group(index);
                if(paramVal != null && paramVal.length() > 0)
                    routeParams.put(part.substring(1), paramVal);
                index++;
            }
        }

        return true;
    }
}
