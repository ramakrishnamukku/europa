/*
  $Id: $
  @file RouteSpec.java
  @brief Contains the RouteSpec.java class

  All Rights Reserved.

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.webserver;

import org.eclipse.jetty.http.HttpMethod;

public class RouteSpec
{
    private String _path = null;
    private HttpMethod _httpMethod = null;
    private Class<? extends RequestHandler> _requestHandler = null;

    public RouteSpec(String path, HttpMethod httpMethod, Class<? extends RequestHandler> requestHandler)
    {
        _path = path;
        _httpMethod = httpMethod;
        _requestHandler = requestHandler;
    }

    public String getPath() {
        return this._path;
    }

    public HttpMethod getHttpMethod() {
        return this._httpMethod;
    }

    public Class<? extends RequestHandler> getRequestHandler()
    {
        return _requestHandler;
    }
}
