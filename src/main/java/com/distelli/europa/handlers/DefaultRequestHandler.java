/*
  $Id: $
  @file DefaultRequestHandler.java
  @brief Contains the DefaultRequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import com.distelli.webserver.*;
import com.distelli.europa.react.PageTemplate;
import com.distelli.europa.react.JSXProperties;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;

@Log4j
public class DefaultRequestHandler extends RequestHandler
{
    @Inject
    private PageTemplate _pageTemplate;

    public DefaultRequestHandler()
    {

    }

    public WebResponse handleRequest(RequestContext requestContext)
    {
        return _pageTemplate.renderPage(requestContext);
    }
}
