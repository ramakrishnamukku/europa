/*
  $Id: $
  @file PageTemplate.java
  @brief Contains the PageTemplate.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.react;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.distelli.europa.webserver.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PageTemplate
{
    private static final Logger log = Logger.getLogger(PageTemplate.class);
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        // Support deserializing interfaces:
        OBJECT_MAPPER.registerModule(new MrBeanModule());
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.getJsonFactory().setCharacterEscapes(new HTMLCharacterEscapes());
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private static final String templateContent = "<!DOCTYPE html>"+
    "<html lang=\"en\">"+
    "<head>"+
    "  <script src=\"/js/SSEPolyfill.js\"></script>"+
    "  <script src=\"/public/common.js\"></script>"+
    "  <script src=\"/public/vendor.js\"></script>"+
    "  <script>"+
    "    var PAGE_VIEW_MODEL = %s;"+
    "  </script>"+
    "</head>"+
    "<body>"+
    "  <div id=\"R\"></div>"+
    "  <script src=\"/public/%s.js\"></script>"+
    "</body>"+
    "</html>";

    public PageTemplate()
    {

    }

    public WebResponse renderPage(RequestContext requestContext, String pageName, JSXProperties properties)
    {
        try {
            if(properties == null)
                properties = new JSXProperties(requestContext);
            String responseContent = String.format(templateContent, OBJECT_MAPPER.writeValueAsString(properties), pageName);
            return new WebResponse(200, responseContent);
        } catch(JsonProcessingException jpe) {
            throw(new WebServerException(jpe));
        }
    }
}
