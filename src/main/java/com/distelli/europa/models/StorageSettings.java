/*
  $Id: $
  @file StorageSettings.java
  @brief Contains the StorageSettings.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageSettings
{
    public static final String SETTING_OS_TYPE = "osType";
    public static final String SETTING_OS_ENDPOINT = "osEndpoint";
    public static final String SETTING_OS_BUCKET = "osBucket";
    public static final String SETTING_OS_CRED_KEY = "osCredKey";
    public static final String SETTING_OS_CRED_SECRET = "osCredSecret";
    public static final String SETTING_OS_DISK_STORAGE_ROOT = "osDiskRoot";
    public static final String SETTING_OS_PATH_PREFIX = "osPathPrefix";

    protected String osType;
    protected String osEndpoint;
    protected String osCredKey;
    protected String osCredSecret;
    protected String osBucket;
    protected String osDiskRoot;
    protected String osPathPrefix;
}
