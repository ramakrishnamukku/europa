/*
  $Id: $
  @file WebhookResponse.java
  @brief Contains the WebhookResponse.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import lombok.Data;
import lombok.Builder;
import lombok.Singular;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse
{
    protected int httpStatusCode;
    protected String body;
    @Singular
    protected Map<String, String> headers;

    public WebhookResponse(String message)
    {
        this.body = message;
        this.httpStatusCode = 0;
    }

    public WebhookResponse(HttpResponse httpResponse)
    {
        this.httpStatusCode = httpResponse.getStatusLine().getStatusCode();
        Header[] headerList = httpResponse.getAllHeaders();
        if(this.headers == null)
            this.headers = new TreeMap<String, String>();
        for(Header header : headerList)
            this.headers.put(header.getName(), header.getValue());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = 0;
        byte[] buf = new byte[1024];
        HttpEntity entity = httpResponse.getEntity();
        if(entity == null)
            return;

        try {
            InputStream in = entity.getContent();
            if(in == null)
                return;
            while((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
            in.close();
            out.close();
            byte[] contentBytes = out.toByteArray();
            if(contentBytes != null && contentBytes.length > 0)
                this.body = new String(contentBytes, "UTF-8");
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }
    }
}
