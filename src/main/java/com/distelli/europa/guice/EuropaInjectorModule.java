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

public class EuropaInjectorModule extends AbstractModule
{
    private static final Logger log = Logger.getLogger(EuropaInjectorModule.class);

    public EuropaInjectorModule()
    {

    }

    protected void configure()
    {
        URI endpoint = URI.create("ddb://us-east-1");
        CredPair creds = new CredPair()
        .withKeyId(System.getenv("KEY_ID"))
        .withSecret(System.getenv("SECRET"));

        bind(Index.Factory.class).toProvider(new IndexFactoryProvider(endpoint, creds));
    }
}
