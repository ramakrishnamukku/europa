/*
  $Id: $
  @file AjaxHelperModule.java
  @brief Contains the AjaxHelperModule.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import org.apache.log4j.Logger;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.distelli.europa.ajax.*;
import com.distelli.europa.webserver.*;

public class AjaxHelperModule extends AbstractModule
{
    private static final Logger log = Logger.getLogger(AjaxHelperModule.class);

    public AjaxHelperModule()
    {

    }

    protected void configure()
    {
        //Add ajax bindings here
        //Cred CRUD helpers
        addBinding(SaveRegistryCreds.class);
        addBinding(ListRegistryCreds.class);
        addBinding(GetRegistryCreds.class);
        addBinding(DeleteRegistryCreds.class);
        //Container CRUD helpers
        addBinding(SaveContainerRepo.class);
        addBinding(GetContainerRepo.class);
        addBinding(ListContainerRepos.class);
        addBinding(DeleteContainerRepo.class);
    }

    private void addBinding(Class<? extends AjaxHelper> clazz)
    {
        MapBinder<String, AjaxHelper> mapbinder = MapBinder.newMapBinder(binder(), String.class, AjaxHelper.class);
        mapbinder.addBinding(clazz.getSimpleName()).to(clazz);
    }
}
