/*
  $Id: $
  @file Europa.java
  @brief Contains the Europa.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;

import org.apache.log4j.Logger;
import com.distelli.europa.util.*;
import com.distelli.europa.webserver.*;
import com.distelli.europa.guice.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Stage;
import com.distelli.persistence.impl.PersistenceModule;

public class Europa
{
    private static final Logger log = Logger.getLogger(Europa.class);

    private RequestHandlerFactory _requestHandlerFactory = null;
    private RouteMatcher _routeMatcher = null;
    private int _port = 8080;
    public Europa(String[] args)
    {
        CmdLineArgs cmdLineArgs = new CmdLineArgs(args);
        //before we start the injector we have to initialize the
        //Logger, VitoConfig and Oxygen

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

        EuropaConfiguration europaConfiguration = EuropaConfiguration.fromFile(new File(configFilePath));
        Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                 new PersistenceModule(),
                                                 new AjaxHelperModule(),
                                                 new EuropaInjectorModule(europaConfiguration));
        injector.injectMembers(this);
        _requestHandlerFactory = new RequestHandlerFactory(injector);
        _routeMatcher = Routes.getRouteMatcher();
    }

    public void start()
    {
        WebServlet servlet = new WebServlet(_routeMatcher, _requestHandlerFactory);
        WebServer webServer = new WebServer(_port, servlet, "/");
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
