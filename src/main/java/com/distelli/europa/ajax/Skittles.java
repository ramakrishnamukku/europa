/*
  $Id: $
  @file Skittles.java
  @brief Contains the Skittles.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.europa.util.*;
import org.apache.log4j.Logger;
import java.util.Map;
import java.util.HashMap;
import com.distelli.europa.webserver.*;
import com.distelli.europa.db.*;
import javax.inject.Inject;

/**
   This is a dummy operation for example purposes only
*/
public class Skittles implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(Skittles.class);

    @Inject
    ProjectDb _projectDb;

    public Skittles()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Hello", "World");
        String projectName = ajaxRequest.getParam("ProjectName");
        return map;
    }
}
