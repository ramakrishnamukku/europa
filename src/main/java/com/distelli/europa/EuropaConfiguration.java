/*
  $Id: $
  @file EuropaConfiguration.java
  @brief Contains the EuropaConfiguration.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.objectStore.*;

public class EuropaConfiguration
{
    @Getter @Setter
    private String dbEndpoint;
    @Getter @Setter
    private String dbUser;
    @Getter @Setter
    private String dbPass;
    @Getter @Setter
    private int dbMaxPoolSize = 2;
    @Getter @Setter
    private ObjectStoreConfig objectStore;
    @Getter @Setter
    private EuropaStage stage;

    public static enum EuropaStage {
        alpha,
        beta,
        gamma,
        prod
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static class ObjectStoreConfig {
        @Getter @Setter
        private String type;
        @Getter @Setter
        private String diskStorageRoot;
        @Getter @Setter
        private String endpoint;
        @Getter @Setter
        private String cred;
        @Getter @Setter
        private String bucket;
        @Getter @Setter
        private String pathPrefix;
    }

    public EuropaConfiguration()
    {

    }

    public ObjectStoreConfig getObjectStoreConfig()
    {
        return this.objectStore;
    }

    public void validate()
    {
        if(this.objectStore == null)
            throw(new RuntimeException("Invalid or Missing value for config: objectStore"));
        if(this.objectStore.type == null)
            throw(new RuntimeException("Invalid or Missing value for config: objectStore.type"));
        if(this.objectStore.bucket == null)
            throw(new RuntimeException("Invalid or Missing value for config: objectStore.bucket"));
        ObjectStoreType objectStoreType = null;
        try {
            objectStoreType = ObjectStoreType.valueOf(this.objectStore.type.toUpperCase());
        } catch(Throwable t) {
            throw(new RuntimeException("Illegal value for config: objectStore.bucket. Expected S3 or DISK"));
        }
        switch(objectStoreType)
        {
        case S3:
            if(this.objectStore.endpoint == null)
                throw(new RuntimeException("Invalid or Missing value for config: objectStore.endpoint"));
            if(this.objectStore.cred == null)
                throw(new RuntimeException("Invalid or Missing value for config: objectStore.cred"));
            break;
        case DISK:
            if(this.objectStore.diskStorageRoot == null)
                throw(new RuntimeException("Invalid or Missing value for config: objectStore.diskStorageRoot"));
            break;
        }
    }

    public static final EuropaConfiguration fromFile(File configFile)
    {
        try {
            EuropaConfiguration config = OBJECT_MAPPER.readValue(configFile, EuropaConfiguration.class);
            return config;
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }
    }

    public boolean isProd()
    {
        return stage == EuropaStage.prod;
    }
}
