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
        _europaTestConfig = EuropaTestConfig.fromFile(new File("./EuropaTestConfig.json"));
    }

    @Override
    public EuropaTestConfig get() {
        return _europaTestConfig;
    }
}
