/*
  $Id: $
  @file EuropaTestConfig.java
  @brief Contains the EuropaTestConfig.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import lombok.*;
import java.io.File;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EuropaTestConfig
{
    @Getter @Setter
    private String awsAccessKey;
    @Getter @Setter
    private String awsSecretKey;
    @Getter @Setter
    private String awsRegion;

    @Getter @Setter
    private String dockerHubUsername;
    @Getter @Setter
    private String dockerHubPassword;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public EuropaTestConfig()
    {

    }

    private static String getenv(String var) {
        String val = System.getenv(var);
        if ( null == val ) throw new IllegalStateException("Missing Env Variable: "+var);
        return val;
    }

    public static final EuropaTestConfig fromEnvironment()
    {
        EuropaTestConfig testConfig = new EuropaTestConfig();
        testConfig.setAwsAccessKey(getenv("EUROPA_TEST_AWS_ACCESS_KEY"));
        testConfig.setAwsSecretKey(getenv("EUROPA_TEST_AWS_SECRET_KEY"));
        testConfig.setAwsRegion(getenv("EUROPA_TEST_AWS_REGION"));
        testConfig.setDockerHubUsername(getenv("EUROPA_TEST_DOCKER_HUB_USERNAME"));
        testConfig.setDockerHubPassword(getenv("EUROPA_TEST_DOCKER_HUB_PASSWORD"));
        return testConfig;
    }

    public static final EuropaTestConfig fromFile(File configFile)
    {
        try {
            EuropaTestConfig config = OBJECT_MAPPER.readValue(configFile, EuropaTestConfig.class);
            return config;
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }
    }
}
