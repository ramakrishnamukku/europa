/*
  $Id: $
  @file EuropaInjectorModule.java
  @brief Contains the EuropaInjectorModule.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.net.URI;

import com.distelli.cred.CredPair;
import com.google.inject.AbstractModule;
import com.distelli.persistence.Index;
import com.distelli.europa.EuropaConfiguration;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.distelli.persistence.impl.mysql.MysqlDataSource;
import com.distelli.europa.monitor.*;
import lombok.extern.log4j.Log4j;

@Log4j
public class EuropaInjectorModule extends AbstractModule
{
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
        bind(MysqlDataSource.class).toInstance(new MysqlDataSource() {
                public int getMaximumPoolSize()
                {
                    return _europaConfiguration.getDbMaxPoolSize();
                }
            });

        install(new FactoryModuleBuilder()
                .implement(MonitorTask.class, EcrMonitorTask.class)
                .build(EcrMonitorTask.Factory.class));

        install(new FactoryModuleBuilder()
                .implement(MonitorTask.class, GcrMonitorTask.class)
                .build(GcrMonitorTask.Factory.class));
    }
}
