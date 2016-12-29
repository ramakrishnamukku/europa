/*
  $Id: $
  @file Constants.java
  @brief Contains the Constants.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import java.util.regex.Pattern;

public class Constants
{
    public static final String LOG_TO_CONSOLE_ARG = "log-to-console";
    public static final String DOMAIN_ZERO = "d0";
    public static final Pattern REGISTRY_CRED_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");
}
