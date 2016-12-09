/*
  $Id: $
  @file GetContainerRepo.java
  @brief Contains the GetContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import org.apache.log4j.Logger;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.webserver.*;
import com.distelli.europa.util.*;

import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetContainerRepo implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(GetContainerRepo.class);

    @Inject
    private ContainerRepoDb _db;

    public GetContainerRepo()
    {

    }

    /**
       Params:
       - id (reqired)
    */
    public Object get(AjaxRequest ajaxRequest)
    {
        String id = ajaxRequest.getParam("id",
                                         true); //throw if missing
        return _db.getRepo(id);
    }
}
