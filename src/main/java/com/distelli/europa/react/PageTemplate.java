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

    // private static final String templateContent = "<!DOCTYPE html>"+
    // "<html lang=\"en\">"+
    // "<head>"+
    // "  <script src=\"/js/SSEPolyfill.js\"></script>"+
    // "  <script src=\"/public/common.js\"></script>"+
    // "  <script src=\"/public/vendor.js\"></script>"+
    // "  <script>"+
    // "    var PAGE_VIEW_MODEL = %s;"+
    // "  </script>"+
    // "</head>"+
    // "<body>"+
    // "  <div id=\"R\"></div>"+
    // "  <script src=\"/public/%s.js\"></script>"+
    // "</body>"+
    // "</html>";

    private static final String templateContent = "<!DOCTYPE html>"+
    "<html lang=\"en\">"+
    "  <head>"+
    "    <meta charset=\"utf-8\">"+
    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"+
    "    <title>Distelli Registry Action Monitor</title>"+
    "    <link rel=\"stylesheet\" href=\"/public/css/app.css\">"+
    "  </head>"+
    "  <body>"+
    "    <div id=\"root\"></div>"+
    "    <script type=\"text/javascript\">"+
    "      self.fetch = null;"+
    "    </script>"+
    "    <script src=\"/public/js/app.js\"></script>"+
    "    <script type=\"text/javascript\">"+
    "      var PAGE_PROPS = %s;"+
    "      window.MyApp.init({"+
    "        mount: 'root',"+
    "        props: PAGE_PROPS"+
    "      });"+
    "    </script>"+
    "  </body>"+
    "</html>";

    public PageTemplate()
    {

    }

    public WebResponse renderPage(RequestContext requestContext, String pageName, JSXProperties properties)
    {
        try {
            if(properties == null)
                properties = new JSXProperties(requestContext);
            String responseContent = String.format(templateContent, OBJECT_MAPPER.writeValueAsString(properties));
            return new WebResponse(200, responseContent);
        } catch(JsonProcessingException jpe) {
            throw(new WebServerException(jpe));
        }
    }
}
