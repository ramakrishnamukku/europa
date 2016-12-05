/*
  $Id: $
  @file JsonSuccess.java
  @brief Contains the JsonSuccess.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

public class JsonSuccess
{
    private boolean success = true;
    public static JsonSuccess Success = new JsonSuccess();

    public boolean getSuccess()
    {
        return success;
    }
}
