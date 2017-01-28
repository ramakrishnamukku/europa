/*
  $Id: $
  @file IndexFactoryProvider.java
  @brief Contains the IndexFactoryProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import javax.inject.Singleton;
import com.distelli.cred.CredPair;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.Schema;
import com.distelli.persistence.TableDescription;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class IndexFactoryProvider implements Provider<Index.Factory>
{
    private static final double NS_IN_MS = 1000000.0;

    @Inject @Named("BASE")
    private Index.Factory _baseIndexFactory;

    @Inject @Named("BASE")
    private Schema.Factory _baseSchemaFactory;

    @Inject
    private Set<TableDescription> _tableDescriptions;

    private URI _endpoint;
    private CredPair _creds;
    private boolean _init = false;
    private Throwable _initFailure = null;
    private Index.Factory _indexFactory;
    private float _scaleFactor;

    public IndexFactoryProvider(URI defaultEndpoint, CredPair defaultCreds) {
        this(defaultEndpoint, defaultCreds, 1.0f);
    }

    public IndexFactoryProvider(URI defaultEndpoint, CredPair defaultCreds, float scaleFactor)
    {
        _scaleFactor = scaleFactor;
        _endpoint = defaultEndpoint;
        _creds = defaultCreds;
        _indexFactory = new Index.Factory() {
                @Override
                public <T> Index.Builder<T> create(Class<T> type) {
                    return _baseIndexFactory.create(type)
                        .withTableNameFormat("%s.europa") //TODO: Add prefix support
                        .withEndpoint(_endpoint)
                        .withCredProvider(() -> _creds);
                }
            };
    }

    @Override
    public synchronized Index.Factory get() {
            if ( ! _init)
                initSchema();
            if ( null != _initFailure )
                throw new RuntimeException(_initFailure);
        return _indexFactory;
    }

    private static TableDescription scale(TableDescription table, float scaleFactor) {
        for ( IndexDescription index : table.getIndexes() ) {
            index.setReadCapacity(Long.valueOf(Math.round(scaleFactor*index.getReadCapacity())));
            index.setWriteCapacity(Long.valueOf(Math.round(scaleFactor*index.getWriteCapacity())));
        }
        return table;
    }

    // We do this on a separate thread since it may take a long time...
    private void initSchema() {
        long t0 = System.nanoTime();
        try {
            log.info("DB schema initializing");
            _baseSchemaFactory.create()
                .withTableNameFormat("%s.europa") //TODO: Add prefix support
                .withEndpoint(_endpoint)
                .withCredProvider(() -> _creds)
                .build()
                .createMissingTablesOrIndexes(_tableDescriptions);
        } catch ( Throwable ex ) {
            _initFailure = ex;
        } finally {
            log.info("DB schema initialized in "+(System.nanoTime()-t0)/NS_IN_MS+"ms");
            _init = true;
        }
    }
}
