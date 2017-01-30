/*
  $Id: $
  @file AjaxHelperMapImpl.java
  @brief Contains the AjaxHelperMapImpl.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxHelperMap;
import com.distelli.webserver.MatchedRoute;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class AjaxHelperMapImpl implements AjaxHelperMap<EuropaRequestContext>
{
    @Inject
    private Map<String, AjaxHelper> _ajaxHelperMap;
    @Inject
    private Map<String, Set<String>> _pathRestrictions;

    public AjaxHelperMapImpl()
    {

    }

    public AjaxHelper get(String operationName, EuropaRequestContext requestContext)
    {
        MatchedRoute matchedRoute = requestContext.getMatchedRoute();
        String path = matchedRoute.getPath();
        Set<String> pathRestrictions = _pathRestrictions.get(operationName);
        if(pathRestrictions != null)
        {
            if(!pathRestrictions.contains(path))
                return null;
        }

        return _ajaxHelperMap.get(operationName);
    }
}
