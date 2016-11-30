/*
  $Id: $
  @file IndexFactoryProvider.java
  @brief Contains the IndexFactoryProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import com.distelli.cred.CredPair;
import com.distelli.persistence.Index;

public class IndexFactoryProvider implements Provider<Index.Factory>
{
    private static final Logger log = Logger.getLogger(IndexFactoryProvider.class);

    @Inject @Named("BASE")
    private Index.Factory _baseIndexFactory;
    private URI _endpoint;
    private CredPair _creds;

    public IndexFactoryProvider(URI defaultEndpoint, CredPair defaultCreds)
    {
        _endpoint = defaultEndpoint;
        _creds = defaultCreds;
    }

    @Override
    public Index.Factory get() {
        return new Index.Factory() {
            @Override
            public <T> Index.Builder<T> create(Class<T> type) {
                return _baseIndexFactory.create(type)
                .withTableNameFormat("prefix-%s")
                .withEndpoint(_endpoint)
                .withCredProvider(() -> _creds);
            }
        };
    }
}
