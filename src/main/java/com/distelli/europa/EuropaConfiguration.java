/*
  $Id: $
  @file EuropaConfiguration.java
  @brief Contains the EuropaConfiguration.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;

import lombok.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EuropaConfiguration
{
    @Getter @Setter
    private String dbEndpoint;
    @Getter @Setter
    private String dbUser;
    @Getter @Setter
    private String dbPass;
    @Getter @Setter
    private boolean multiTenant = false;

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
}
