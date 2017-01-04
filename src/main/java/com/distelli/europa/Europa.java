/*
  $Id: $
  @file Europa.java
  @brief Contains the Europa.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;

import com.distelli.europa.EuropaConfiguration.EuropaStage;
import com.distelli.europa.util.*;
import com.distelli.ventura.*;
import com.distelli.europa.guice.*;
import com.distelli.europa.monitor.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Stage;
import com.distelli.objectStore.impl.ObjectStoreModule;
import com.distelli.persistence.impl.PersistenceModule;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import com.distelli.objectStore.*;

@Log4j
public class Europa
{
    private RequestHandlerFactory _requestHandlerFactory = null;
    private RouteMatcher _routeMatcher = null;
    private int _port = 8080;

    @Inject
    private ObjectStore _objectStore;
    @Inject
    private ObjectKeyFactory _objectKeyFactory;

    @Inject
    private MonitorQueue _monitorQueue;
    private Thread _monitorThread;

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
        Log4JConfigurator.setLogLevel("com.distelli.ventura", "DEBUG");
        Log4JConfigurator.setLogLevel("com.distelli.gcr", "DEBUG");
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
        initialize();
        _requestHandlerFactory = new RequestHandlerFactory() {
                public RequestHandler getRequestHandler(MatchedRoute route) {
                    return injector.getInstance(route.getRequestHandler());
                }
            };
        _routeMatcher = Routes.getRouteMatcher();
    }

    private void initialize()
    {
        try {
            _objectStore.createBucket(_objectKeyFactory.getDefaultBucket());
        } catch(Throwable t) {
            log.error("Failed to create default bucket: "+_objectKeyFactory.getDefaultBucket()+
                      ": "+t.getMessage(), t);
        }
    }

    public void start()
    {
        RepoMonitor monitor = new RepoMonitor(_monitorQueue);
        _monitorThread = new Thread(monitor);
        _monitorThread.start();

        WebServlet servlet = new WebServlet(_routeMatcher, _requestHandlerFactory);
        WebServer webServer = new WebServer(_port, servlet, "/");
        webServer.setAssetsDirPath("./");
        // This is a little bit of a hack... passing in null causes
        // jetty to NPE. We only use this servlet from Routes.java
        // which is why we are using an arbitrary UUID here.
        webServer.setAssetsPath("/994194C9-5742-4284-A7A0-17DCC1D7E921");
        webServer.setCacheControl("max-age=300");
        webServer.setEtags(true);
        webServer.start();
    }

    public static void main(String[] args)
    {
        Europa europa = new Europa(args);
        europa.start();
    }
}
