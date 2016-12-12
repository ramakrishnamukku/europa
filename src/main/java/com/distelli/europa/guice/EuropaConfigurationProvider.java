/*
  $Id: $
  @file EuropaConfigurationProvider.java
  @brief Contains the EuropaConfigurationProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import com.distelli.europa.EuropaConfiguration;
import org.apache.log4j.Logger;
import javax.inject.Provider;

public class EuropaConfigurationProvider implements Provider<EuropaConfiguration>
{
    private static final Logger log = Logger.getLogger(EuropaConfigurationProvider.class);
    private EuropaConfiguration _europaConfiguration;

    public EuropaConfigurationProvider(EuropaConfiguration europaConfiguration)
    {
        _europaConfiguration = europaConfiguration;
    }

    @Override
    public EuropaConfiguration get() {
        return _europaConfiguration;
    }
}
