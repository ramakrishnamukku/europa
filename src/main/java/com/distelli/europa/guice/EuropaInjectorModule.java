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
import javax.inject.Singleton;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import com.distelli.cred.CredPair;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.db.NotificationsDb;
import com.distelli.europa.db.PipelineDb;
import com.distelli.europa.db.MonitorDb;
import com.distelli.europa.db.RegistryBlobDb;
import com.distelli.europa.db.RegistryCredsDb;
import com.distelli.europa.db.RegistryManifestDb;
import com.distelli.europa.db.RepoEventsDb;
import com.distelli.europa.db.SequenceDb;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.europa.models.DnsSettings;
import com.distelli.europa.models.Monitor;
import com.distelli.europa.models.SslSettings;
import com.distelli.europa.models.StorageSettings;
import com.distelli.europa.registry.RegistryAccess;
import com.distelli.europa.monitor.*;
import com.distelli.europa.clients.DockerHubClient;
import com.distelli.europa.util.ObjectKeyFactory;
import com.distelli.europa.util.PermissionCheck;
import com.distelli.objectStore.*;
import com.distelli.persistence.Index;
import com.distelli.persistence.TableDescription;
import com.distelli.persistence.impl.mysql.MysqlDataSource;
import com.distelli.webserver.AjaxHelperMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import okhttp3.ConnectionPool;
import lombok.extern.log4j.Log4j;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
import com.distelli.gcr.GcrClient;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import com.google.inject.multibindings.OptionalBinder;

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
        addTableDescription(MonitorDb.getTableDescription());

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

    @Provides @Singleton
    protected ScheduledExecutorService getScheduledExecutorService() {

        AtomicInteger threadCounter = new AtomicInteger();
        ThreadFactory threadFactory = (runnable) -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            int threadCount = threadCounter.incrementAndGet();
            thread.setName(String.format("ScheduledExecutorService-%d", threadCount));
            return thread;
        };


        ScheduledThreadPoolExecutor threadPool =
            new ScheduledThreadPoolExecutor(10, threadFactory);
        threadPool.setKeepAliveTime(60, TimeUnit.SECONDS);
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        threadPool.shutdownNow();
                        threadPool.awaitTermination(30, TimeUnit.SECONDS);
                    } catch ( Throwable ex ) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            });
        return threadPool;
    }

    @Provides @Singleton
    protected Monitor getMonitor(MonitorDb monitorDb) {
        return monitorDb.startMonitor();
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

        OptionalBinder.newOptionalBinder(binder(), PermissionCheck.class)
            .setDefault().to(PermissionCheck.Default.class);
        OptionalBinder.newOptionalBinder(binder(), RegistryAccess.class)
            .setDefault().to(RegistryAccess.Default.class);
        ConnectionPool sharedPool = new ConnectionPool(20, 5, TimeUnit.MINUTES);
        bind(ConnectionPool.class)
            .toInstance(sharedPool);
        bind(GcrClient.Builder.class)
            .toProvider(() -> new GcrClient.Builder().connectionPool(sharedPool));
        bind(DockerHubClient.Builder.class)
            .toProvider(() -> new DockerHubClient.Builder().connectionPool(sharedPool));
        bind(Index.Factory.class).toProvider(new IndexFactoryProvider(endpoint, creds));
        configureEuropaConfiguration();
        bind(ObjectStore.class).toProvider(new ObjectStoreProvider());
        bind(DnsSettings.class).toProvider(new DnsSettingsProvider());
        bind(StorageSettings.class).toProvider(new StorageSettingsProvider());
        bind(SslSettings.class).toProvider(new SslSettingsProvider());
        bind(SslContextFactory.class).toProvider(new SslContextFactoryProvider());
        bind(ObjectKeyFactory.class).toProvider(new ObjectKeyFactoryProvider());
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

        install(new FactoryModuleBuilder()
                .implement(MonitorTask.class, DockerHubMonitorTask.class)
                .build(DockerHubMonitorTask.Factory.class));

        Multibinder<TableDescription> tableBinder =
            Multibinder.newSetBinder(binder(), TableDescription.class);
        for ( Provider<TableDescription> tableProvider : _tables ) {
            tableBinder.addBinding().toProvider(tableProvider);
        }
    }
}
