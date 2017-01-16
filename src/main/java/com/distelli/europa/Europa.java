/*
  $Id: $
  @file Europa.java
  @brief Contains the Europa.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import com.distelli.europa.EuropaConfiguration.EuropaStage;
import com.distelli.europa.guice.*;
import com.distelli.europa.monitor.*;
import com.distelli.europa.util.*;
import com.distelli.utils.Log4JConfigurator;
import com.distelli.europa.handlers.StaticContentErrorHandler;
import com.distelli.europa.filters.RegistryAuthFilter;
import com.distelli.objectStore.*;
import com.distelli.objectStore.impl.ObjectStoreModule;
import com.distelli.persistence.impl.PersistenceModule;
import com.distelli.webserver.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Stage;

import lombok.extern.log4j.Log4j;

@Log4j
public class Europa
{
    protected RequestHandlerFactory _requestHandlerFactory = null;
    protected RequestContextFactory _requestContextFactory = null;
    protected RequestFilter[] _registryApiFilters = null;
    protected RequestFilter[] _webappFilters = null;
    protected StaticContentErrorHandler _staticContentErrorHandler = null;
    protected int _port = 8080;

    @Inject
    protected ObjectStore _objectStore;
    @Inject
    protected ObjectKeyFactory _objectKeyFactory;

    @Inject
    protected MonitorQueue _monitorQueue;
    protected Thread _monitorThread;

    protected RouteMatcher _webappRouteMatcher = null;
    protected RouteMatcher _registryApiRouteMatcher = null;

    public Europa(String[] args)
    {
        CmdLineArgs cmdLineArgs = new CmdLineArgs(args);
        boolean logToConsole = cmdLineArgs.hasOption(Constants.LOG_TO_CONSOLE_ARG);
        // Initialize Logging
        File logsDir = new File("./logs/");
        if(!logsDir.exists())
            logsDir.mkdirs();

        if(logToConsole)
            Log4JConfigurator.configure(true);
        else
            Log4JConfigurator.configure(logsDir, "Europa");
        Log4JConfigurator.setLogLevel("INFO");
        Log4JConfigurator.setLogLevel("com.distelli.europa", "DEBUG");
        Log4JConfigurator.setLogLevel("com.distelli.webserver", "DEBUG");
        Log4JConfigurator.setLogLevel("com.distelli.gcr", "DEBUG");
        Log4JConfigurator.setLogLevel("com.distelli.europa.monitor", "ERROR");
        Log4JConfigurator.setLogLevel("com.distelli.webserver", "ERROR");
        String configFilePath = cmdLineArgs.getOption("config");
        if(configFilePath == null)
        {
            log.fatal("Missing value for arg --config");
            System.exit(1);
        }

        String portStr = cmdLineArgs.getOption("port");
        if(portStr != null)
        {
            try {
                _port = Integer.parseInt(portStr);
            } catch(NumberFormatException nfe) {
                log.fatal("Invalid value for --port "+portStr);
                System.exit(1);
            }
        }

        EuropaStage stage = EuropaStage.prod;
        String stageArg = cmdLineArgs.getOption("stage");
        if(stageArg != null) {
            try {
                stage = EuropaStage.valueOf(stageArg);
            } catch(Throwable t) {
                throw(new RuntimeException("Invalid value for stage: "+stageArg, t));
            }
        }

        EuropaConfiguration europaConfiguration = EuropaConfiguration.fromFile(new File(configFilePath));
        europaConfiguration.setStage(stage);
        europaConfiguration.validate();
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new PersistenceModule(),
                                                       new AjaxHelperModule(),
                                                       new ObjectStoreModule(),
                                                       new EuropaInjectorModule(europaConfiguration));
        injector.injectMembers(this);
        initialize(injector);
    }

    protected void initialize(Injector injector)
    {
        _registryApiFilters = new RequestFilter[] {
            injector.getInstance(RegistryAuthFilter.class)
        };

        try {
            _objectStore.createBucket(_objectKeyFactory.getDefaultBucket());
        } catch(Throwable t) {
            log.error("Failed to create default bucket: "+_objectKeyFactory.getDefaultBucket()+
                      ": "+t.getMessage(), t);
        }

        _requestContextFactory = new RequestContextFactory() {
                public RequestContext getRequestContext(HTTPMethod httpMethod, HttpServletRequest request) {
                    return new EuropaRequestContext(httpMethod, request);
                }
            };

        _requestHandlerFactory = new RequestHandlerFactory() {
                public RequestHandler getRequestHandler(MatchedRoute route) {
                    return injector.getInstance(route.getRequestHandler());
                }
            };
        _staticContentErrorHandler = injector.getInstance(StaticContentErrorHandler.class);
        _registryApiRouteMatcher = RegistryApiRoutes.getRouteMatcher();
        _webappRouteMatcher = WebAppRoutes.getRouteMatcher();
    }

    public void start()
    {
        RepoMonitor monitor = new RepoMonitor(_monitorQueue);
        _monitorThread = new Thread(monitor);
        _monitorThread.start();

        WebServlet servlet = new WebServlet(_webappRouteMatcher, _requestHandlerFactory);
        servlet.setRequestContextFactory(_requestContextFactory);
        if(_webappFilters != null)
            servlet.setRequestFilters(_webappFilters);
        WebServer webServer = new WebServer(_port, servlet, "/");

        ServletHolder staticHolder = new ServletHolder(DefaultServlet.class);
        staticHolder.setInitParameter("resourceBase", "./public");
        staticHolder.setInitParameter("dirAllowed","true");
        staticHolder.setInitParameter("pathInfoOnly","true");
        staticHolder.setInitParameter("etags", "true");
        staticHolder.setInitParameter("cacheControl", "max-age=3600");
        webServer.addStandardServlet("/public/*", staticHolder);

        WebServlet registryApiServlet = new WebServlet(_registryApiRouteMatcher, _requestHandlerFactory);
        servlet.setRequestContextFactory(_requestContextFactory);
        registryApiServlet.setRequestContextFactory(new RequestContextFactory() {
                public RequestContext getRequestContext(HTTPMethod method, HttpServletRequest request) {
                    return new RequestContext(method, request, false);
                }
            });
        registryApiServlet.setRequestFilters(_registryApiFilters);
        webServer.addWebServlet("/v2/*", registryApiServlet);

        webServer.setErrorHandler(_staticContentErrorHandler);
        webServer.start();
    }

    public static void main(String[] args)
    {
        Europa europa = new Europa(args);
        europa.start();
    }
}
