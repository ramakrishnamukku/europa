/*
  $Id: $
  @file GetContainerRepo.java
  @brief Contains the GetContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.ventura.*;
import com.distelli.europa.util.*;

import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class GetContainerRepo implements AjaxHelper
{
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
        String domain = ajaxRequest.getParam("domain");
        return _db.getRepo(domain, id);
    }
}
