/*
  $Id: $
  @file EuropaConfiguration.java
  @brief Contains the EuropaConfiguration.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import com.distelli.objectStore.*;

@Log4j
public class EuropaConfiguration
{
    @Getter @Setter
    protected String dbEndpoint;
    @Getter @Setter
    protected String dbUser;
    @Getter @Setter
    protected String dbPass;
    @Getter @Setter
    protected int dbMaxPoolSize = 2;
    @Getter @Setter
    protected EuropaStage stage;

    public static enum EuropaStage {
        alpha,
        beta,
        gamma,
        prod
    }

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public EuropaConfiguration()
    {

    }

    public static final EuropaConfiguration fromEnvironment() {
        String dbEndpoint = getEnvVar("EUROPA_DB_ENDPOINT");
        String dbUser = getEnvVar("EUROPA_DB_USER");
        String dbPass = getEnvVar("EUROPA_DB_PASS");
        int dbPoolSize = 2;
        String dbPoolSizeStr = null;
        try {
            dbPoolSizeStr = getEnvVar("EUROPA_DB_POOL_SIZE");
            dbPoolSize = Integer.parseInt(dbPoolSizeStr);
        } catch(Throwable t) {
            log.error("Invalid Value for Env Variable: EUROPA_DB_POOL_SIZE");
            dbPoolSize = 2;
        }

        EuropaConfiguration config = new EuropaConfiguration();
        config.setDbEndpoint(dbEndpoint);
        config.setDbUser(dbUser);
        config.setDbPass(dbPass);
        config.setDbMaxPoolSize(dbPoolSize);
        return config;
    }

    private static final String getEnvVar(String varName)
    {
        String value = System.getenv(varName);
        if(value != null)
            return value;
        throw(new IllegalStateException("Missing Env Variable: "+varName));
    }

    public static final EuropaConfiguration fromFile(File configFile)
    {
        try {
            EuropaConfiguration config = OBJECT_MAPPER.readValue(configFile, EuropaConfiguration.class);
            return config;
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }
    }

    public boolean isProd()
    {
        return stage == EuropaStage.prod;
    }
}
