/*
  $Id: $
  @file EuropaConfiguration.java
  @brief Contains the EuropaConfiguration.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EuropaConfiguration
{
    private String dbEndpoint;
    private String dbUser;
    private String dbPass;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public EuropaConfiguration()
    {

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

    public final String getDbPass() {
        return this.dbPass;
    }

    public final void setDbPass(String dbPass) {
        this.dbPass = dbPass;
    }

    public final String getDbUser() {
        return this.dbUser;
    }

    public final void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public final String getDbEndpoint() {
        return this.dbEndpoint;
    }

    public final void setDbEndpoint(String dbEndpoint) {
        this.dbEndpoint = dbEndpoint;
    }
}
