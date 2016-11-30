/*
  $Id: $
  @file WebServlet.java
  @brief Contains the WebServlet.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpMethod;

public class WebServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(WebServlet.class);

    private RouteMatcher _routeMatcher = null;
    private RequestHandlerFactory _requestHandlerFactory = null;
    public WebServlet(RouteMatcher routeMatcher, RequestHandlerFactory requestHandlerFactory)
    {
        _routeMatcher = routeMatcher;
        _requestHandlerFactory = requestHandlerFactory;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        handleRequest(HttpMethod.GET, request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        handleRequest(HttpMethod.PUT, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        handleRequest(HttpMethod.POST, request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        handleRequest(HttpMethod.DELETE, request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        handleRequest(HttpMethod.TRACE, request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        handleRequest(HttpMethod.HEAD, request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        handleRequest(HttpMethod.OPTIONS, request, response);
    }

    public void handleRequest(HttpMethod httpMethod, HttpServletRequest request, HttpServletResponse response)
        throws ServletException
    {
        OutputStream out = null;
        WebResponse webResponse = null;
        RequestContext requestContext = null;
        boolean writeGZipped = false;
        try
        {
            requestContext = new RequestContext(request, httpMethod);
            webResponse = handleRequest(requestContext, request, response);
            if(webResponse != null)
            {
                if(log.isDebugEnabled())
                    log.debug("Writing HttpStatusCode: "+webResponse.getHttpStatusCode()+
                              " for Request: "+requestContext.getRequestId());
                response.setStatus(webResponse.getHttpStatusCode());

                if(log.isDebugEnabled())
                    log.debug("Writing HttpStatusCode: "+webResponse.getContentType()+
                              " for Request: "+requestContext.getRequestId());

                response.setContentType(webResponse.getContentType());
                response.setCharacterEncoding("UTF-8");

                Map<String, String> responseHeaders = webResponse.getResponseHeaders();
                writeGZipped = requestContext.isGZipAccepted();
                if(responseHeaders != null && responseHeaders.size() > 0)
                {
                    for(Map.Entry<String, String> entry : responseHeaders.entrySet())
                    {
                        String headerName = entry.getKey();
                        if(writeGZipped && headerName != null && headerName.equalsIgnoreCase(WebConstants.CONTENT_ENCODING_HEADER))
                            writeGZipped = false;

                        response.setHeader(headerName, entry.getValue());
                    }
                }

                if(writeGZipped)
                    response.setHeader(WebConstants.CONTENT_ENCODING_HEADER, "gzip");

                for(Cookie cookie : webResponse.getCookies())
                    response.addCookie(cookie);
            }
        }
        catch(WebClientException wce)
        {
            //override the webResponse
            if(log.isDebugEnabled())
                log.debug("Caught WebClientException: "+wce.getMessage(), wce);
            webResponse = new WebResponse(400);
            String msg = wce.getMessage();
            if(msg != null)
                webResponse.setResponseContent(msg.getBytes());
        }
        catch(Throwable t)
        {
            throw(new ServletException(t));
        }

        try
        {
            if(webResponse != null)
            {
                out = response.getOutputStream();
                if(writeGZipped) out = new GZIPOutputStream(out, true);
                webResponse.writeResponse(out);
                out.close();
            }
        }
        catch(Throwable t)
        {
            throw(new ServletException(t));
        } finally {
            if(webResponse != null) webResponse.close();
        }
    }

    private WebResponse handleRequest(RequestContext requestContext,
                                      HttpServletRequest request,
                                      HttpServletResponse response)
    {
        HttpMethod httpMethod = requestContext.getHttpMethod();
        MatchedRoute route = _routeMatcher.match(httpMethod, requestContext.getPath());
        if(route == null)
            throw(new IllegalStateException("No Route for Path: "+requestContext.getPath()));

        Map<String, String> routeParams = route.getParams();
        requestContext.addRouteParams(routeParams);

        RequestHandler requestHandler = _requestHandlerFactory.getRequestHandler(route);
        if(log.isDebugEnabled())
            log.debug("Calling RequestHandler: "+requestHandler.toString()+" for Request: "+requestContext.getRequestId());
        WebResponse webResponse = requestHandler.handleRequest(requestContext);
        if(log.isDebugEnabled())
            log.debug("Received WebResponse: "+webResponse+" for request: "+requestContext.getRequestId());
        return webResponse;
    }
}
