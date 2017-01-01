/*
  $Id: $
  @file ObjectStoreProvider.java
  @brief Contains the ObjectStoreProvider.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.guice;

import java.io.File;
import javax.inject.Provider;

import com.distelli.europa.EuropaConfiguration;
import com.distelli.objectStore.*;
import com.distelli.objectStore.impl.*;

import javax.inject.Inject;
import lombok.extern.log4j.Log4j;

@Log4j
public class ObjectStoreProvider implements Provider<ObjectStore>
{
    @Inject
    private ObjectStore.Factory _objectStoreFactory;

    private ObjectStore _objectStore;

    public ObjectStoreProvider()
    {

    }

    public ObjectStore get()
    {
        if(_objectStore == null)
            _objectStore = _objectStoreFactory.create().build();
        return _objectStore;
    }
}
