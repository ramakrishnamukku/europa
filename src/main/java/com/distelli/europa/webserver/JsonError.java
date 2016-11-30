/*
  $Id: $
  @file JsonError.java
  @brief Contains the JsonError.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

public class JsonError
{
    //Standard Errors
    public static JsonError MalformedRequest = new JsonError("The request is Malformed", Codes.MalformedRequest, 400);
    public static JsonError UnsupportedHttpMethod = new JsonError("The HttpMethod is unsupported", Codes.UnsupportedHttpMethod, 400);
    public static JsonError UnsupportedOperation = new JsonError("The Operation is unsupported", Codes.UnsupportedOperation, 400);
    public static JsonError InternalServerError = new JsonError("An Internal Server Error occured", Codes.InternalServerError, 500);

    public static final class Codes {
        public static final String MalformedRequest = "MalformedRequest";
        public static final String UnsupportedHttpMethod = "UnsupportedHttpMethod";
        public static final String UnsupportedOperation = "UnsupportedOperation";
        public static final String InternalServerError = "InternalServerError";
    }

    private static class Error {
        private String message;
        private String code;

        public Error(String message, String code) {
            this.message = message;
            this.code = code;
        }
        public String getMessage()
        {
            return this.message;
        }

        public String getCode()
        {
            return this.code;
        }
    }

    private Error error;
    private int httpStatusCode = -1;

    public JsonError()
    {

    }

    public JsonError(String message, String code, int httpStatusCode)
    {
        this.error = new Error(message, code);
        this.httpStatusCode = httpStatusCode;
    }

    public JsonError(String message, String code)
    {
        this.error = new Error(message, code);
    }

    public Error getError()
    {
        return this.error;
    }

    protected int getHttpStatusCode()
    {
        return httpStatusCode;
    }
}
