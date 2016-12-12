/*
  $Id: $
  @file EuropaInjectorModule.java
  @brief Contains the EuropaInjectorModule.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.net.URI;

import org.apache.log4j.Logger;
import com.distelli.cred.CredPair;
import com.google.inject.AbstractModule;
import com.distelli.persistence.Index;
import com.distelli.europa.EuropaConfiguration;

public class EuropaInjectorModule extends AbstractModule
{
    private static final Logger log = Logger.getLogger(EuropaInjectorModule.class);

    private EuropaConfiguration _europaConfiguration;

    public EuropaInjectorModule(EuropaConfiguration europaConfiguration)
    {
        _europaConfiguration = europaConfiguration;
    }

    protected void configure()
    {
        URI endpoint = URI.create(_europaConfiguration.getDbEndpoint());
        CredPair creds = new CredPair()
        .withKeyId(_europaConfiguration.getDbUser())
        .withSecret(_europaConfiguration.getDbPass());

        bind(Index.Factory.class).toProvider(new IndexFactoryProvider(endpoint, creds));
        bind(EuropaConfiguration.class).toProvider(new EuropaConfigurationProvider(_europaConfiguration));
    }
}
