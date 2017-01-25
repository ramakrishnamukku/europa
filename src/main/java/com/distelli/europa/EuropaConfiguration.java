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
import lombok.extern.log4j.Log4j;
import com.distelli.objectStore.*;

@Log4j
public class EuropaConfiguration
{
    @Getter @Setter
    protected String dbEndpoint;
    @Getter @Setter
    protected String dbUser;
    @Getter @Setter
    protected String dbPass;
    @Getter @Setter
    protected int dbMaxPoolSize = 2;
    @Getter @Setter
    protected ObjectStoreConfig objectStore;
    @Getter @Setter
    protected EuropaStage stage;

    public static enum EuropaStage {
        alpha,
        beta,
        gamma,
        prod
    }

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static class ObjectStoreConfig {
        @Getter @Setter
        protected String type;
        @Getter @Setter
        protected String diskStorageRoot;
        @Getter @Setter
        protected String endpoint;
        @Getter @Setter
        protected String cred;
        @Getter @Setter
        protected String bucket;
        @Getter @Setter
        protected String pathPrefix;
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
            throw(new RuntimeException("Illegal value for config: objectStore.type. Expected S3 or DISK"));
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

    public static final EuropaConfiguration fromEnvironment() {
        String dbEndpoint = getEnvVar("EUROPA_DB_ENDPOINT");
        String dbUser = getEnvVar("EUROPA_DB_USER");
        String dbPass = getEnvVar("EUROPA_DB_PASS");
        int dbPoolSize = 2;
        String dbPoolSizeStr = null;
        try {
            dbPoolSizeStr = getEnvVar("EUROPA_DB_POOL_SIZE");
            dbPoolSize = Integer.parseInt(dbPoolSizeStr);
        } catch(Throwable t) {
            log.error("Invalid Value for Env Variable: EUROPA_DB_POOL_SIZE");
            dbPoolSize = 2;
        }
        String objectStoreType = getEnvVar("EUROPA_OS_TYPE");
        String objectStoreEndpoint = getEnvVar("EUROPA_OS_ENDPOINT");
        String objectStoreCredKey = getEnvVar("EUROPA_OS_CRED_KEY");
        String objectStoreCredSecret = getEnvVar("EUROPA_OS_CRED_SECRET");
        String objectStoreBucket = getEnvVar("EUROPA_OS_BUCKET");
        String objectStoreDiskStorageRoot = null;
        String objectStorePathPrefix = null;
        try {
            objectStorePathPrefix = getEnvVar("EUROPA_OS_PATH_PREFIX");
            objectStoreDiskStorageRoot = getEnvVar("EUROPA_OS_DISK_ROOT");
        } catch(IllegalStateException ise) {
            //these are not required variables
        }

        EuropaConfiguration config = new EuropaConfiguration();
        config.setDbEndpoint(dbEndpoint);
        config.setDbUser(dbUser);
        config.setDbPass(dbPass);
        config.setDbMaxPoolSize(dbPoolSize);

        ObjectStoreConfig osConfig = new ObjectStoreConfig();
        osConfig.setType(objectStoreType);
        osConfig.setDiskStorageRoot(objectStoreDiskStorageRoot);
        osConfig.setEndpoint(objectStoreEndpoint);
        osConfig.setCred(String.format("%s:%s", objectStoreCredKey, objectStoreCredSecret));
        osConfig.setBucket(objectStoreBucket);
        osConfig.setPathPrefix(objectStorePathPrefix);
        config.setObjectStore(osConfig);
        return config;
    }

    private static final String getEnvVar(String varName)
    {
        String value = System.getenv(varName);
        if(value != null)
            return value;
        throw(new IllegalStateException("Missing Env Variable: "+varName));
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
