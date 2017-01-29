/*
  $Id: $
  @file ObjectStoreProvider.java
  @brief Contains the ObjectStoreProvider.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.guice;

import java.io.File;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.distelli.cred.CredPair;
import com.distelli.cred.CredProvider;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.models.StorageSettings;
import com.distelli.objectStore.ObjectStore;
import com.distelli.objectStore.ObjectStoreType;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ObjectStoreProvider implements Provider<ObjectStore>
{
    @Inject @Named("BASE")
    private ObjectStore.Factory _baseObjectStoreFactory;
    @Inject
    private Provider<StorageSettings> _storageSettingsProvider;

    private ObjectStore _objectStore;

    public ObjectStoreProvider()
    {

    }

    public ObjectStore get()
    {
        if(_objectStore != null)
            return _objectStore;
        final StorageSettings storageSettings = _storageSettingsProvider.get();
        if(storageSettings == null)
            throw(new ObjectStoreNotInitialized("Object Store is not initialized"));
        ObjectStore.Factory objectStoreFactory = getObjectStoreFactory(storageSettings);
        if(objectStoreFactory == null)
            throw(new ObjectStoreNotInitialized("Object Store is not initialized"));
        _objectStore = objectStoreFactory.create().build();
        ObjectStoreType osType = storageSettings.getOsType();
        if(osType == ObjectStoreType.DISK)
            _objectStore.createBucket(storageSettings.getOsBucket());
        return _objectStore;
    }

    private ObjectStore.Factory getObjectStoreFactory(final StorageSettings storageSettings)
    {
        if(storageSettings == null)
            return null;

        return new ObjectStore.Factory() {
            @Override
            public ObjectStore.Builder create() {
                ObjectStoreType osType = storageSettings.getOsType();
                File diskStorageRoot = null;
                URI endpointUri = null;
                CredProvider credProvider = null;

                switch(osType)
                {
                case S3:
                    String endpoint = storageSettings.getOsEndpoint();
                    if(endpoint == null)
                        throw(new RuntimeException("Invalid or Missing value for StorageSetting: osEndpoint"));
                    endpointUri = URI.create(endpoint);
                    final CredPair credPair = new CredPair()
                    .withKeyId(storageSettings.getOsCredKey())
                    .withSecret(storageSettings.getOsCredSecret());

                    credProvider = new CredProvider() {
                            public CredPair getCredPair() {
                                return credPair;
                            }
                        };
                    break;
                case DISK:
                    String storageRoot = storageSettings.getOsDiskRoot();
                    if(storageRoot == null)
                        throw(new RuntimeException("Invalid or Missing value for StorateSetting: osDiskRoot"));
                    diskStorageRoot = new File(storageRoot);
                    break;
                }

                return _baseObjectStoreFactory.create()
                .withObjectStoreType(osType)
                .withDiskStorageRoot(diskStorageRoot)
                .withEndpoint(endpointUri)
                .withCredProvider(credProvider);
            };
        };
    }
}
