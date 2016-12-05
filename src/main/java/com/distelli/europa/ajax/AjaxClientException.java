/*
  $Id: $
  @file AjaxClientException.java
  @brief Contains the AjaxClientException.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;
import com.distelli.europa.webserver.*;

public class AjaxClientException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private JsonError _jsonError;
    public AjaxClientException()
    {

    }

    public AjaxClientException(JsonError jsonError)
    {
        _jsonError = jsonError;
    }

    public AjaxClientException(String message, String code, int httpStatusCode)
    {
        super(message);
        _jsonError = new JsonError(message, code, httpStatusCode);
    }

    public AjaxClientException(String message)
    {
        super(message);
    }

    public AjaxClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AjaxClientException(Throwable cause)
    {
        super(cause);
    }

    public JsonError getJsonError()
    {
        return _jsonError;
    }
}
