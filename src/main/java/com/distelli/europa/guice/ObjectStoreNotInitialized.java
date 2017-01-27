/*
  $Id: $
  @file ObjectStoreNotInitialized.java
  @brief Contains the ObjectStoreNotInitialized.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import lombok.extern.log4j.Log4j;

@Log4j
public class ObjectStoreNotInitialized extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public ObjectStoreNotInitialized()
    {

    }
    public ObjectStoreNotInitialized(String message)
    {
        super(message);
    }

    public ObjectStoreNotInitialized(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ObjectStoreNotInitialized(Throwable cause)
    {
        super(cause);
    }
}
