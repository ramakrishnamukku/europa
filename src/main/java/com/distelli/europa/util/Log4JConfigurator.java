/*
  $Id: $
  @file Log4JConfigurator.java
  @brief Contains the Log4JConfigurator.java class

  All Rights Reserved.

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.util;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.TimeBasedRollingPolicy;

public class Log4JConfigurator
{
    private static final String LOGGING_LAYOUT = "%d{yyyy-MM-dd HH:mm:ss.S z}:[%p]:[%t]:%c:%m%n";
    private static final String DEFAULT_LOG_DIR = "logs/";

    public Log4JConfigurator()
    {

    }

    public static void configure(String logFilename)
    {
        configure(false, null, logFilename, true);
    }

    public static void configure(String logFilename, boolean rotateHourly)
    {
        configure(false, null, logFilename, rotateHourly);
    }

    public static void configure(File logDir, String logFilename, boolean rotateHourly)
    {
        configure(false, logDir, logFilename, rotateHourly);
    }

    public static void configure(File logDir, String logFilename)
    {
        configure(false, logDir, logFilename, true);
    }

    public static void configure(boolean logToConsole)
    {
        configure(true, null, null, true);
    }

    private static void setLogLevel(Logger logger, String level)
    {
        Level logLevel = Level.toLevel(level);
        if(logLevel.toString().equals(level))
            logger.setLevel(logLevel);
    }

    public static void setLogLevel(String level)
    {
        Logger logger = Logger.getRootLogger();
        setLogLevel(logger, level);
    }

    public static void setLogLevel(String name, String level)
    {
        if(name == null)
            return;

        Logger logger = Logger.getLogger(name);
        setLogLevel(logger, level);
    }

    public static void configure(boolean logToConsole, File logDir, String logFilename, boolean rotateHourly)
    {
        Logger rootLogger = Logger.getRootLogger();
        if(rootLogger.getAllAppenders().hasMoreElements())
            return;

        if(logToConsole)
        {
            rootLogger.addAppender(new ConsoleAppender(new PatternLayout(LOGGING_LAYOUT)));
        }
        else
        {
            if(logDir == null)
                logDir = new File("./"+DEFAULT_LOG_DIR);

            if(!logDir.exists())
                logDir.mkdirs();

            RollingFileAppender appender = new RollingFileAppender();
            TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();

            String datePattern = null;
            if(rotateHourly)
                datePattern = "{yyyy-MM-dd-HH}";
            else
                datePattern = "{yyyy-MM-dd}";

            rollingPolicy.setFileNamePattern(logDir.getAbsolutePath()+"/"+logFilename+".%d"+datePattern+".log.gz");
            rollingPolicy.setActiveFileName(logDir.getAbsolutePath()+"/"+logFilename+".log");
            rollingPolicy.activateOptions();

            appender.setRollingPolicy(rollingPolicy);
            appender.setLayout(new PatternLayout(LOGGING_LAYOUT));
            appender.setAppend(true);
            appender.activateOptions();
            rootLogger.addAppender(appender);
        }
    }
}
