/*
  $Id: $
  @file RequestHandler.java
  @brief Contains the RequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.distelli.europa.react.*;
import javax.inject.Inject;

public abstract class RequestHandler
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        // Support deserializing interfaces:
        OBJECT_MAPPER.registerModule(new MrBeanModule());
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.getJsonFactory().setCharacterEscapes(new HTMLCharacterEscapes());
    }

    @Inject
    private PageTemplate _pageTemplate;

    public RequestHandler()
    {

    }

    public abstract WebResponse handleRequest(RequestContext requestContext);

    public WebResponse redirect(String location)
    {
        WebResponse webResponse = new WebResponse();
        webResponse.setHttpStatusCode(302);
        webResponse.setResponseHeader(WebConstants.LOCATION_HEADER, location);
        return webResponse;
    }

    public WebResponse redirect(WebResponse response, String location)
    {
        response.setHttpStatusCode(302);
        response.setResponseHeader(WebConstants.LOCATION_HEADER, location);
        return response;
    }

    public WebResponse ok(String content)
    {
        WebResponse webResponse = new WebResponse();
        webResponse.setHttpStatusCode(200);
        webResponse.setResponseContent(content.getBytes());
        return webResponse;
    }

    public WebResponse ok(WebResponse response)
    {
        response.setHttpStatusCode(200);
        return response;
    }

    public WebResponse badRequest(String content)
    {
        WebResponse webResponse = new WebResponse();
        webResponse.setHttpStatusCode(400);
        webResponse.setResponseContent(content.getBytes());
        return webResponse;
    }

    public WebResponse badRequest(WebResponse response)
    {
        response.setHttpStatusCode(400);
        return response;
    }

    public WebResponse toJson(String key, String value)
    {
        Map<String, String> model = new HashMap<String, String>();
        model.put(key, value);
        return toJson(model);
    }

    public WebResponse notFound(String content)
    {
        WebResponse webResponse = new WebResponse();
        webResponse.setHttpStatusCode(404);
        webResponse.setResponseContent(content.getBytes());
        return webResponse;
    }

    public WebResponse ok(ResponseWriter responseWriter)
    {
        WebResponse response = new WebResponse(200);
        response.setResponseWriter(responseWriter);
        return response;
    }

    public WebResponse toJson(ResponseWriter responseWriter)
    {
        WebResponse response = new WebResponse();
        response.setHttpStatusCode(200);
        response.setContentType(WebConstants.CONTENT_TYPE_JSON);
        response.setResponseWriter(responseWriter);
        return response;
    }

    public WebResponse toJson(final Object obj, int httpResponseCode)
    {
        WebResponse response = new WebResponse();
        response.setHttpStatusCode(httpResponseCode);
        response.setContentType(WebConstants.CONTENT_TYPE_JSON);
        ResponseWriter responseWriter = new ResponseWriter() {
                public void writeResponse(OutputStream out)
                    throws IOException {
                    OBJECT_MAPPER.writeValue(out, obj);
                }
            };
        response.setResponseWriter(responseWriter);
        return response;
    }

    public WebResponse jsonError(JsonError jsonError)
    {
        int httpStatusCode = jsonError.getHttpStatusCode();
        if(httpStatusCode == -1)
            httpStatusCode = 400;
        return toJson(jsonError, httpStatusCode);
    }

    public WebResponse toJson(final Object obj)
    {
        return toJson(obj, 200);
    }

    public WebResponse renderPage(RequestContext requestContext, String pageName, JSXProperties properties)
    {
        return _pageTemplate.renderPage(requestContext, pageName, properties);
    }

    public WebResponse renderPage(RequestContext requestContext, JSXProperties properties)
    {
        return renderPage(requestContext, "app", properties);
    }
}
