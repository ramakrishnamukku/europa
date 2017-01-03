package com.distelli.europa.react;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.distelli.europa.EuropaConfiguration;
import com.distelli.ventura.*;

import javax.inject.Singleton;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;

@Log4j
@Singleton
public class PageTemplate
{
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
    "  <head>"+
    "    <meta charset=\"utf-8\">"+
    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"+
    "    <title>Europa Container Registry</title>"+
    "  </head>"+
    "  <body>"+
    "    <div id=\"root\"></div>"+
    "    <script type=\"text/javascript\">"+
    "      self.fetch = null;"+
    "    </script>"+
    "    <link rel=\"stylesheet\" href=\"/public/css/%s\">"+
    "    <script src=\"/public/js/%s\"></script>"+
    "    <script type=\"text/javascript\">"+
    "      var PAGE_PROPS = %s;"+
    "      window.MyApp.init({"+
    "        mount: 'root',"+
    "        props: PAGE_PROPS"+
    "      });"+
    "    </script>"+
    "  </body>"+
    "</html>";

    @Inject
    private EuropaConfiguration _europaConfiguration;

    public PageTemplate()
    {

    }

    public WebResponse renderPage(RequestContext requestContext)
    {

        return renderPage(requestContext, null);
    }

    public WebResponse renderPage(RequestContext requestContext, JSXProperties properties)
    {
        String cssName = "app.css";
        String appName = "app.js";
        if(_europaConfiguration.isProd()) {
            appName = "app.min.js";
            cssName = "app.min.css";
        }

        try {
            if(properties == null)
                properties = new JSXProperties(requestContext);
            String responseContent = String.format(templateContent,
                                                   cssName,
                                                   appName,
                                                   OBJECT_MAPPER.writeValueAsString(properties)
                                                   );
            return new WebResponse(200, responseContent);
        } catch(JsonProcessingException jpe) {
            throw(new WebServerException(jpe));
        }
    }
}
