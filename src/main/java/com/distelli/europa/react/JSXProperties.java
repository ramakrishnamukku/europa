package com.distelli.europa.react;

import lombok.extern.log4j.Log4j;
import com.distelli.webserver.*;

@Log4j
public class JSXProperties
{
    private RequestContext _requestContext;

    public JSXProperties(RequestContext requestContext)
    {
        _requestContext = requestContext;
    }
}
