/*
  $Id: $
  @file StaticContentRequestHandler.java
  @brief Contains the StaticContentRequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import java.io.*;
import org.apache.log4j.Logger;
import com.distelli.europa.webserver.*;

public class StaticContentRequestHandler extends RequestHandler
{
    private static final Logger log = Logger.getLogger(StaticContentRequestHandler.class);

    public StaticContentRequestHandler()
    {

    }

    public WebResponse handleRequest(RequestContext requestContext)
    {
        String path = requestContext.getPath();
        if(!path.startsWith("/public/"))
            return notFound("<h1>PAGE NOT FOUND</h1>");

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
        } catch(FileNotFoundException fnfe) {
            return notFound("<h1>PAGE NOT FOUND</h1>");
        }

        webResponse.setHttpStatusCode(200);
        return webResponse;
    }
}
