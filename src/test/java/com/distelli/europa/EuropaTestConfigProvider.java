/*
  $Id: $
  @file EuropaTestConfigProvider.java
  @brief Contains the EuropaTestConfigProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;
import javax.inject.Provider;
import org.apache.log4j.Logger;

public class EuropaTestConfigProvider implements Provider<EuropaTestConfig>
{
    private static final Logger log = Logger.getLogger(EuropaTestConfigProvider.class);

    private EuropaTestConfig _europaTestConfig;

    public EuropaTestConfigProvider()
    {
        String testConfigFilePath = System.getenv("EUROPA_TEST_CONFIG");
        if(testConfigFilePath != null) {
            File testConfigFile = new File(testConfigFilePath);
            _europaTestConfig = EuropaTestConfig.fromFile(testConfigFile);
        } else {
            _europaTestConfig = EuropaTestConfig.fromEnvironment();
        }
    }

    @Override
    public EuropaTestConfig get() {
        return _europaTestConfig;
    }
}
