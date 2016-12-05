/*
  $Id: $
  @file SaveContainerRepo.java
  @brief Contains the SaveContainerRepo.java class

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
public class SaveContainerRepo implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(SaveContainerRepo.class);

    @Inject
    private ContainerRepoDb _db;

    public SaveContainerRepo()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        ContainerRepo repo = ajaxRequest.convertContent(ContainerRepo.class,
                                                       true); //throw if null
        //Validate that the fields we want are non-null
        FieldValidator.validateNonNull(repo, "provider", "region", "name");
        //save in the db
        _db.save(repo);
        return JsonSuccess.Success;
    }
}
