/*
  $Id: $
  @file ObjectKeyFactoryProvider.java
  @brief Contains the ObjectKeyFactoryProvider.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import javax.inject.Provider;

import com.distelli.europa.models.StorageSettings;
import com.distelli.europa.util.ObjectKeyFactory;

import javax.inject.Inject;
import lombok.extern.log4j.Log4j;

@Log4j
public class ObjectKeyFactoryProvider implements Provider<ObjectKeyFactory>
{
    @Inject
    private Provider<StorageSettings> _storageSettingsProvider;

    private ObjectKeyFactory _objectKeyFactory;
    public ObjectKeyFactoryProvider()
    {

    }

    public ObjectKeyFactory get()
    {
        if(_objectKeyFactory != null)
            return _objectKeyFactory;
        StorageSettings storageSettings = _storageSettingsProvider.get();
        if(storageSettings == null)
            throw(new ObjectStoreNotInitialized("Object Store is not initialized"));
        _objectKeyFactory = new ObjectKeyFactory(storageSettings);
        return _objectKeyFactory;
    }
}
