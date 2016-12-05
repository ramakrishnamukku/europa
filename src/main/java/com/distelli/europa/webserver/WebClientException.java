/*
  $Id: $
  @file WebClientException.java
  @brief Contains the WebClientException.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

public class WebClientException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public WebClientException()
    {

    }

    public WebClientException(String message)
    {
        super(message);
    }

    public WebClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WebClientException(Throwable cause)
    {
        super(cause);
    }
}
