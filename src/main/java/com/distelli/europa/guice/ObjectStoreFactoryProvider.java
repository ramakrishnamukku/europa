/*
  $Id: $
  @file ObjectStoreFactoryProvider.java
  @brief Contains the ObjectStoreFactoryProvider.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.guice;

import java.io.File;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.distelli.cred.CredPair;
import com.distelli.cred.CredProvider;
import com.distelli.europa.EuropaConfiguration.ObjectStoreConfig;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.objectStore.*;
import com.distelli.objectStore.impl.*;

import lombok.extern.log4j.Log4j;

@Log4j
public class ObjectStoreFactoryProvider implements Provider<ObjectStore.Factory>
{
    @Inject @Named("BASE")
    private ObjectStore.Factory _baseObjectStoreFactory;
    private EuropaConfiguration _europaConfiguration;

    public ObjectStoreFactoryProvider(EuropaConfiguration europaConfiguration)
    {
        _europaConfiguration = europaConfiguration;
    }

    @Override
    public ObjectStore.Factory get() {
        return new ObjectStore.Factory() {
            @Override
            public ObjectStore.Builder create() {
                final ObjectStoreConfig osConfig = _europaConfiguration.getObjectStoreConfig();
                if(osConfig == null)
                    throw(new RuntimeException("Invalid or Missing value for config: objectStore"));

                String osType = osConfig.getType();
                if(osType == null)
                    throw(new RuntimeException("Invalid or Missing value for config: objectStore.type"));

                ObjectStoreType objectStoreType = ObjectStoreType.valueOf(osType.toUpperCase());
                File diskStorageRoot = null;
                URI endpointUri = null;
                CredProvider credProvider = null;

                switch(objectStoreType)
                {
                case S3:
                    String endpoint = osConfig.getEndpoint();
                    if(endpoint == null)
                        throw(new RuntimeException("Invalid or Missing value for config: objectStore.endpoint"));
                    endpointUri = URI.create(endpoint);
                    final CredPair credPair = getCredPair(osConfig);
                    credProvider = new CredProvider() {
                            public CredPair getCredPair() {
                                return credPair;
                            }
                        };
                case DISK:
                    String storageRoot = osConfig.getDiskStorageRoot();
                    if(storageRoot == null)
                        throw(new RuntimeException("Invalid or Missing value for config: objectStore.diskStorageRoot"));
                    diskStorageRoot = new File(storageRoot);
                }

                return _baseObjectStoreFactory.create()
                .withObjectStoreType(objectStoreType)
                .withDiskStorageRoot(diskStorageRoot)
                .withEndpoint(endpointUri)
                .withCredProvider(credProvider);
            }
        };
    }

    private CredPair getCredPair(ObjectStoreConfig osConfig)
    {
        String cred = osConfig.getCred();
        if(cred == null)
            throw(new RuntimeException("Invalid or Missing value for config: objectStore.cred"));
        String[] parts = cred.split(":");
        return new CredPair()
            .withKeyId(parts[0])
            .withSecret(parts.length > 1 ? parts[1] : null);
    }
}
