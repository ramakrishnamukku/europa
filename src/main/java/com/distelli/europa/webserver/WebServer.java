/*
  $Id: $
  @file WebServer.java
  @brief Contains the WebServer.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer implements Runnable
{
    private static final Logger log = Logger.getLogger(WebServer.class);
    private int _port = 5050;
    private HttpServlet _servlet = null;
    private String _path = null;
    private String _assetsPath = "/_assets/*";
    private String _assetsDirPath = "./assets";
    private int _sessionMaxAge = 2592000; //default is 30 days
    private String _cacheControl = "no-cache";
    private boolean _etags = true;

    public WebServer(int port, HttpServlet servlet, String path)
    {
        _port = port;
        _servlet = servlet;
        _path = path;
    }

    public void setAssetsPath(String assetsPath)
    {
        _assetsPath = assetsPath;
    }

    public void setAssetsDirPath(String assetsDirPath)
    {
        _assetsDirPath = assetsDirPath;
    }

    public void setSessionMaxAge(int sessionMaxAge)
    {
        _sessionMaxAge = sessionMaxAge;
    }

    public void setCacheControl(String cacheControl)
    {
        _cacheControl = cacheControl;
    }

    public void setEtags(boolean etags)
    {
        _etags = true;
    }

    public void run()
    {
        Thread.currentThread().setName("WebServer");
        try
        {
            Server server = new Server(_port);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            SessionManager sessionManager = new HashSessionManager();
            context.setSessionHandler(new SessionHandler(sessionManager));
            context.setContextPath(_path);
            context.setInitParameter("org.eclipse.jetty.servlet.MaxAge", ""+_sessionMaxAge);
            server.setHandler(context);

            ServletHolder servletHolder = new ServletHolder(_servlet);
            context.addServlet(servletHolder, _path);

            // add static assets servlet
            ServletHolder staticHolder = new ServletHolder("static-home", DefaultServlet.class);
            staticHolder.setInitParameter("resourceBase", _assetsDirPath);
            staticHolder.setInitParameter("dirAllowed", "false");
            staticHolder.setInitParameter("pathInfoOnly", "true");
            if(_etags)
                staticHolder.setInitParameter("etags", "true");
            if(_cacheControl != null)
                staticHolder.setInitParameter("cacheControl", _cacheControl);
            context.addServlet(staticHolder, _assetsPath);

            server.start();
            server.join();
        }
        catch(Throwable t)
        {
            throw(new RuntimeException(t));
        }
    }

    public void start()
    {
        run();
    }
}
