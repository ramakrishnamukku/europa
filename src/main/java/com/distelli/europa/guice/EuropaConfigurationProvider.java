/*
  $Id: $
  @file EuropaConfigurationProvider.java
  @brief Contains the EuropaConfigurationProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import com.distelli.europa.EuropaConfiguration;
import javax.inject.Provider;
import lombok.extern.log4j.Log4j;

@Log4j
public class EuropaConfigurationProvider implements Provider<EuropaConfiguration>
{
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
