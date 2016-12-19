/*
  $Id: $
  @file EuropaTestModule.java
  @brief Contains the EuropaTestModule.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import com.google.inject.AbstractModule;
import org.apache.log4j.Logger;

public class EuropaTestModule extends AbstractModule
{
    private static final Logger log = Logger.getLogger(EuropaTestModule.class);

    public EuropaTestModule()
    {

    }

    protected void configure()
    {
        bind(EuropaTestConfig.class).toProvider(new EuropaTestConfigProvider());
    }
}
