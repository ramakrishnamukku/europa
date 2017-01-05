/*
  $Id: $
  @file StaticContentErrorHandler.java
  @brief Contains the StaticContentErrorHandler.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.handlers;

import java.io.IOException;
import java.io.OutputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import com.distelli.europa.react.JSXProperties;
import com.distelli.europa.react.PageTemplate;
import com.distelli.webserver.*;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class StaticContentErrorHandler extends ErrorHandler
{
    @Inject
    private PageTemplate _pageTemplate;
    public StaticContentErrorHandler()
    {

    }

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
        throws IOException
    {
        WebResponse webResponse = _pageTemplate.renderPage(null);
        response.setContentType("text/html");
        OutputStream out = response.getOutputStream();
        webResponse.writeResponse(out);
    }
}
