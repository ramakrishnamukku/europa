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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public EuropaTestConfig()
    {

    }

    public static final EuropaTestConfig fromEnvironment()
    {
        String awsAccessKey = System.getenv("EUROPA_TEST_AWS_ACCESS_KEY");
        if(awsAccessKey == null)
            throw(new IllegalStateException("Missing Env Variable: EUROPA_TEST_AWS_ACCESS_KEY"));
        String awsSecretKey = System.getenv("EUROPA_TEST_AWS_SECRET_KEY");
        if(awsSecretKey == null)
            throw(new IllegalStateException("Missing Env Variable: EUROPA_TEST_AWS_SECRET_KEY"));
        String awsRegion = System.getenv("EUROPA_TEST_AWS_REGION");
        if(awsRegion == null)
            throw(new IllegalStateException("Missing Env Variable: EUROPA_TEST_AWS_REGION_KEY"));
        EuropaTestConfig testConfig = new EuropaTestConfig();
        testConfig.setAwsAccessKey(awsAccessKey);
        testConfig.setAwsSecretKey(awsSecretKey);
        testConfig.setAwsRegion(awsRegion);
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
