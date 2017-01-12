/*
  $Id: $
  @file AjaxHelperMapImpl.java
  @brief Contains the AjaxHelperMapImpl.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.util.Map;
import com.distelli.webserver.AjaxHelperMap;
import com.distelli.webserver.AjaxHelper;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;

@Log4j
public class AjaxHelperMapImpl implements AjaxHelperMap
{
    @Inject
    private Map<String, AjaxHelper> _ajaxHelperMap;

    public AjaxHelperMapImpl()
    {

    }

    public AjaxHelper get(String operationName)
    {
        return _ajaxHelperMap.get(operationName);
    }
}
