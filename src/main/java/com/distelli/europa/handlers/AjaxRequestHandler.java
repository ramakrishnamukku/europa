/*
  $Id: $
  @file AjaxRequestHandler.java
  @brief Contains the AjaxRequestHandler.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.handlers;

import org.apache.log4j.Logger;

import org.apache.log4j.Logger;
import com.distelli.europa.webserver.*;
//import com.distelli.europa.models.*;
import com.distelli.europa.ajax.*;
import com.distelli.europa.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;
import java.util.Map;
import javax.inject.Inject;

@Singleton
public class AjaxRequestHandler extends RequestHandler
{
    private static final Logger log = Logger.getLogger(AjaxRequestHandler.class);

    @Inject
    private Map<String, AjaxHelper> _ajaxHelperMap;

    public AjaxRequestHandler()
    {

    }

    public WebResponse handleRequest(RequestContext requestContext)
    {
        try {
            AjaxRequest ajaxRequest = null;
            JsonNode jsonContent = requestContext.getJsonContent();
            if(jsonContent != null)
                ajaxRequest = AjaxRequest.fromJson(jsonContent);
            else
                ajaxRequest = AjaxRequest.fromQueryParams(requestContext.getQueryParams());

            String operation = ajaxRequest.getOperation();
            if(operation == null)
                return jsonError(JsonError.UnsupportedOperation);
            AjaxHelper ajaxHelper = _ajaxHelperMap.get(operation);
            if(ajaxHelper == null)
                return jsonError(JsonError.UnsupportedOperation);
            return toJson(ajaxHelper.get(ajaxRequest));
        } catch(Throwable t) {
            log.error(t.getMessage(), t);
            return toJson(JsonError.InternalServerError);
        }
    }
}
