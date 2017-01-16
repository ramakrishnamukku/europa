/*
  $Id: $
  @file EuropaInjectorModule.java
  @brief Contains the EuropaInjectorModule.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import com.distelli.cred.CredPair;
import com.distelli.webserver.AjaxHelperMap;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.monitor.*;
import com.distelli.persistence.Index;
import com.distelli.persistence.TableDescription;
import com.distelli.persistence.impl.mysql.MysqlDataSource;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import java.net.URI;
import java.util.List;
import java.util.Arrays;
import javax.inject.Provider;
import lombok.extern.log4j.Log4j;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.db.SequenceDb;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.db.RegistryCredsDb;
import com.distelli.europa.db.NotificationsDb;
import com.distelli.europa.db.RepoEventsDb;
import com.distelli.europa.db.RegistryManifestDb;
import com.distelli.objectStore.*;

@Log4j
public class EuropaInjectorModule extends AbstractModule
{
    private static List<Provider<TableDescription>> TABLES = Arrays.asList(
        TokenAuthDb::getTableDescription,
        RegistryBlobDb::getTableDescription,
        SequenceDb::getTableDescription,
        ContainerRepoDb::getTableDescription,
        RegistryCredsDb::getTableDescription,
        NotificationsDb::getTableDescription,
        RepoEventsDb::getTableDescription,
        RegistryManifestDb::getTableDescription
        );

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
        for ( Provider<TableDescription> tableProvider : TABLES ) {
            tableBinder.addBinding().toProvider(tableProvider);
        }
    }
}
