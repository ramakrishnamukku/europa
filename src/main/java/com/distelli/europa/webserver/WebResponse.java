/*
  $Id: $
  @file WebResponse.java
  @brief Contains the WebResponse.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.

  TODO: Support "flushing"!
*/
package com.distelli.europa.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.Cookie;

public class WebResponse
{
    private int _httpStatusCode = 200;
    private String _contentType = "text/html";
    private byte[] _responseContent;
    private Map<String, String> _responseHeaders = new TreeMap<String, String>();
    private ResponseWriter _responseWriter = null;
    private List<Cookie> _cookies = new ArrayList<Cookie>();

    public WebResponse() {}

    public WebResponse(int httpStatusCode)
    {
        _httpStatusCode = httpStatusCode;
    }

    public WebResponse(String responseContent)
    {
        _responseContent = responseContent.getBytes();
    }

    public WebResponse(int httpStatusCode, String responseContent)
    {
        _responseContent = responseContent.getBytes();
        _httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return this._httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this._httpStatusCode = httpStatusCode;
    }

    public String getContentType() {
        return this._contentType;
    }

    public void setContentType(String contentType) {
        this._contentType = contentType;
    }

    public byte[] getResponseContent() {
        return this._responseContent;
    }

    public void setResponseContent(byte[] responseContent) {
        this._responseContent = responseContent;
    }

    public void setResponseHeader(String key, String value)
    {
        _responseHeaders.put(key, value);
    }

    public Map<String, String> getResponseHeaders()
    {
        return _responseHeaders;
    }

    public boolean hasContent()
    {
        return _responseContent != null;
    }

    public boolean hasResponseWriter()
    {
        return _responseWriter != null;
    }

    public void setResponseWriter(ResponseWriter responseWriter)
    {
        _responseWriter = responseWriter;
    }

    public void writeResponse(OutputStream out)
        throws IOException
    {
        if(_responseContent != null)
            out.write(_responseContent);
        else if(_responseWriter != null)
            _responseWriter.writeResponse(out);
        out.flush();
    }

    public void addCookie(Cookie cookie) {
        _cookies.add(cookie);
    }

    public List<Cookie> getCookies() {
        return Collections.unmodifiableList(_cookies);
    }

    public void close() {
        // Subclasses can do request post-processing here.
    }
}
