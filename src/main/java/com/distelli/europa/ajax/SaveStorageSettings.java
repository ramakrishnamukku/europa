/*
  $Id: $
  @file SaveStorageSettings.java
  @brief Contains the SaveStorageSettings.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.guice.ObjectStoreProvider;
import com.distelli.europa.models.EuropaSetting;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.europa.models.StorageSettings;
import com.distelli.europa.util.FieldValidator;
import com.distelli.objectStore.ObjectKey;
import com.distelli.objectStore.ObjectStore;
import com.distelli.objectStore.ObjectStoreType;
import com.distelli.utils.CompactUUID;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonError;
import com.distelli.webserver.JsonSuccess;

import lombok.extern.log4j.Log4j;
import static java.nio.charset.StandardCharsets.UTF_8;

@Log4j
@Singleton
public class SaveStorageSettings extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private SettingsDb _settingsDb;
    @Inject
    private Provider<StorageSettings> _storageSettingsProvider;
    @Inject
    private ObjectStoreProvider _objectStoreProvider;

    public SaveStorageSettings()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        StorageSettings storageSettings = _storageSettingsProvider.get();
        if(storageSettings != null)
            return JsonSuccess.Success;

        storageSettings = ajaxRequest.convertContent(StorageSettings.class,
                                                     true); //throw if null
        validateS3Creds(storageSettings);
        List<EuropaSetting> europaSettings = storageSettings.toEuropaSettings();
        for(EuropaSetting setting : europaSettings)
            _settingsDb.save(setting);

        return JsonSuccess.Success;
    }

    protected void validateS3Creds(StorageSettings storageSettings)
    {
        if(storageSettings.getOsType() != ObjectStoreType.S3)
            return;
        String bucket = storageSettings.getOsBucket();
        String awsAccessKey = storageSettings.getOsCredKey();
        String awsSecretKey = storageSettings.getOsCredSecret();

        String testKey = "test-europa-key-"+CompactUUID.randomUUID().toString();
        byte[] testData = ("TEST_DATA_FROM_EUROPA_OK_TO_DELETE-"+CompactUUID.randomUUID().toString())
            .getBytes(UTF_8);

        String pathPrefix = storageSettings.getOsPathPrefix();
        if(pathPrefix != null && !pathPrefix.trim().isEmpty())
            testKey = String.format("%s/%s",pathPrefix,testKey);

        ObjectKey objectKey = ObjectKey.builder()
        .bucket(bucket)
        .key(testKey)
        .build();

        ObjectStore objectStore = _objectStoreProvider.get(storageSettings);
        try {
            objectStore.put(objectKey, testData);
        } catch (EntityNotFoundException ex) {
            throw(new AjaxClientException("The provided bucket is invalid", AjaxErrors.Codes.BadS3Bucket, 400));
        } catch(AccessControlException ace) {
            throw(new AjaxClientException("The provided credentials are invalid", AjaxErrors.Codes.BadS3Credentials, 400));
        } catch(AmazonS3Exception as3e) {
            throw(new AjaxClientException("The provided s3 storage settings are invalid", AjaxErrors.Codes.BadS3Settings, 400));
        }

        //Now do a GetObject
        try
        {
            byte[] out = new byte[testData.length];
            objectStore.get(objectKey, (meta, in) -> {
                    new DataInputStream(in).readFully(out);
                    return null;
                });
            if(!Arrays.equals(out, testData))
                throw(new IllegalStateException("Error: Get Data does not match PutData: "
                                                + Arrays.toString(out) + " != " + Arrays.toString(testData)));
        }
        catch(IOException ioe) {
            throw(new RuntimeException(ioe));
        }
        catch(AccessControlException ex) {
            throw(new AjaxClientException("The provided s3 storage settings are invalid", AjaxErrors.Codes.BadS3Settings, 400));
        }

        try
        {
            objectStore.delete(objectKey);
        }
        catch(Exception e)
        {
            //best effort. Do nothing
        }
    }
}
