/*
  $Id: $
  @file IndexFactoryProvider.java
  @brief Contains the IndexFactoryProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import com.distelli.cred.CredPair;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import lombok.extern.log4j.Log4j;

@Log4j
public class IndexFactoryProvider implements Provider<Index.Factory>
{
    @Inject @Named("BASE")
    private Index.Factory _baseIndexFactory;

    private URI _endpoint;
    private CredPair _creds;
    private Index.Factory _indexFactory;

    public IndexFactoryProvider(URI defaultEndpoint, CredPair defaultCreds) {
        this(defaultEndpoint, defaultCreds, 1.0f);
    }

    public IndexFactoryProvider(URI defaultEndpoint, CredPair defaultCreds, float scaleFactor)
    {
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
        return _indexFactory;
    }

}
