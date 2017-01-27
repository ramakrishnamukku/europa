/*
  $Id: $
  @file EuropaInjectorModule.java
  @brief Contains the EuropaInjectorModule.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Provider;

import com.distelli.cred.CredPair;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.db.NotificationsDb;
import com.distelli.europa.db.PipelineDb;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.db.RegistryCredsDb;
import com.distelli.europa.db.RegistryManifestDb;
import com.distelli.europa.db.RepoEventsDb;
import com.distelli.europa.db.SequenceDb;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.monitor.*;
import com.distelli.objectStore.*;
import com.distelli.persistence.Index;
import com.distelli.persistence.TableDescription;
import com.distelli.persistence.impl.mysql.MysqlDataSource;
import com.distelli.webserver.AjaxHelperMap;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

import lombok.extern.log4j.Log4j;

@Log4j
public class EuropaInjectorModule extends AbstractModule
{
    protected List<Provider<TableDescription>> _tables = null;
    private EuropaConfiguration _europaConfiguration;

    public EuropaInjectorModule(EuropaConfiguration europaConfiguration)
    {
        _tables = new ArrayList<Provider<TableDescription>>();
        addTableDescription(TokenAuthDb.getTableDescription());
        addTableDescription(RegistryBlobDb.getTableDescription());
        addTableDescription(SequenceDb.getTableDescription());
        addTableDescription(ContainerRepoDb.getTableDescription());
        addTableDescription(RegistryCredsDb.getTableDescription());
        addTableDescription(NotificationsDb.getTableDescription());
        addTableDescription(RepoEventsDb.getTableDescription());
        addTableDescription(RegistryManifestDb.getTableDescription());
        addTableDescription(PipelineDb.getTableDescription());
        addTableDescription(SettingsDb.getTableDescription());

        _europaConfiguration = europaConfiguration;
    }

    protected void addTableDescription(final TableDescription tableDescription)
    {
        _tables.add(new Provider<TableDescription>() {
                public TableDescription get()
                {
                    return tableDescription;
                }
            });
    }

    protected void configureEuropaConfiguration()
    {
        bind(EuropaConfiguration.class).toProvider(new EuropaConfigurationProvider(_europaConfiguration));
    }

    protected void configure()
    {
        URI endpoint = URI.create(_europaConfiguration.getDbEndpoint());
        CredPair creds = new CredPair()
        .withKeyId(_europaConfiguration.getDbUser())
        .withSecret(_europaConfiguration.getDbPass());

        bind(Index.Factory.class).toProvider(new IndexFactoryProvider(endpoint, creds));
        configureEuropaConfiguration();
        bind(ObjectStore.Factory.class).toProvider(new ObjectStoreFactoryProvider(_europaConfiguration));
        bind(ObjectStore.class).toProvider(new ObjectStoreProvider());
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

        Multibinder<TableDescription> tableBinder =
            Multibinder.newSetBinder(binder(), TableDescription.class);
        for ( Provider<TableDescription> tableProvider : _tables ) {
            tableBinder.addBinding().toProvider(tableProvider);
        }
    }
}
