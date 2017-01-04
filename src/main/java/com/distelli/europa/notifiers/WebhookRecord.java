/*
  $Id: $
  @file WebhookRecord.java
  @brief Contains the WebhookRecord.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.io.IOException;

import com.distelli.europa.Constants;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.models.*;
import com.distelli.objectStore.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URL;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

@Log4j
@Data
public class WebhookRecord
{
    protected URL url;
    protected WebhookRequest request;
    protected WebhookResponse response;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static
    {
        OBJECT_MAPPER.setVisibilityChecker(new VisibilityChecker.Std(JsonAutoDetect.Visibility.NONE,  //getters
                                                                     JsonAutoDetect.Visibility.NONE,  //is-getters
                                                                     JsonAutoDetect.Visibility.NONE,  //setters
                                                                     JsonAutoDetect.Visibility.NONE,  //creator
                                                                     JsonAutoDetect.Visibility.ANY)); //field

        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public WebhookRecord()
    {

    }

    public WebhookRecord(WebhookRequest request, WebhookResponse response)
    {
        this.request = request;
        this.response = response;
    }

    public byte[] toJsonBytes()
        throws JsonProcessingException
    {
        return OBJECT_MAPPER.writeValueAsBytes(this);
    }

    public static WebhookRecord fromJsonBytes(byte[] recordBytes)
        throws IOException
    {
        return OBJECT_MAPPER.readValue(recordBytes, WebhookRecord.class);
    }
}
