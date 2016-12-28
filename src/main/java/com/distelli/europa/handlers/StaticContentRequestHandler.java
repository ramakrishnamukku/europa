/*
  $Id: $
  @file StaticContentRequestHandler.java
  @brief Contains the StaticContentRequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import com.distelli.ventura.*;
import lombok.extern.log4j.Log4j;

@Log4j
public class StaticContentRequestHandler extends RequestHandler
{
    private static Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
            put("json", "application/json");
            put("ico", "image/x-icon");
            put("css", "text/css");
            put("xml", "text/xml");
            put("txt", "text/plain");
            put("js", "application/javascript");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("svg", "image/svg+xml");
        }};

    public StaticContentRequestHandler()
    {

    }

    public WebResponse handleRequest(RequestContext requestContext)
    {
        String path = requestContext.getPath();
        if(!path.startsWith("/public/") && !path.startsWith("/assets/"))
            return renderPage(requestContext, null);

        WebResponse webResponse = new WebResponse();
        File staticFile = new File("./"+path);
        try {
            final FileInputStream fileIn = new FileInputStream(staticFile);
            ResponseWriter responseWriter = new ResponseWriter() {
                    public void writeResponse(OutputStream out)
                        throws IOException
                    {
                        int bytesIn = 0;
                        byte[] buf = new byte[1024];
                        while((bytesIn = fileIn.read(buf)) != -1)
                            out.write(buf, 0, bytesIn);
                        out.flush();
                    }
                };
            webResponse.setResponseWriter(responseWriter);
            int dot = path.lastIndexOf('.');
            String ext = path.substring(dot+1);
            if ( dot > 0 && MIME_TYPES.containsKey(ext) ) {
                webResponse.setContentType(MIME_TYPES.get(ext));
            } else {
                webResponse.setContentType("text/html");
            }
        } catch(FileNotFoundException fnfe) {
            return notFound("<h1>PAGE NOT FOUND</h1>");
        }

        webResponse.setHttpStatusCode(200);
        return webResponse;
    }
}
