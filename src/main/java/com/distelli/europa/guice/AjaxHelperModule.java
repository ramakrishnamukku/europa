/*
  $Id: $
  @file AjaxHelperModule.java
  @brief Contains the AjaxHelperModule.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.guice;

import java.util.HashSet;
import java.util.Set;

import com.distelli.europa.ajax.*;
import com.distelli.webserver.*;
import com.google.inject.TypeLiteral;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import lombok.extern.log4j.Log4j;

@Log4j
public class AjaxHelperModule extends AbstractModule
{
    public AjaxHelperModule()
    {

    }

    protected void configure()
    {
        // Add ajax bindings here
        addBinding(ListPipelines.class);
        addBinding(GetPipeline.class);
        addBinding(SetPipelineContainerRepoId.class);
        addBinding(NewPipeline.class);
        addBinding(RemovePipeline.class);
        addBinding(AddPipelineComponent.class);
        addBinding(MovePipelineComponent.class);
        addBinding(RemovePipelineComponent.class);

        addBinding(GetRegionsForProvider.class);
        addBinding(ListReposInRegistry.class);

        //Cred CRUD helpers
        addBinding(SaveRegistryCreds.class);
        addBinding(ListRegistryCreds.class);
        addBinding(GetRegistryCreds.class);
        addBinding(DeleteRegistryCreds.class);
        addBinding(SaveGcrServiceAccountCreds.class);

        //Container CRUD helpers
        addBinding(SaveContainerRepo.class);
        addBinding(CreateLocalRepo.class);
        addBinding(GetContainerRepo.class);
        addBinding(ListContainerRepos.class);
        addBinding(DeleteContainerRepo.class);
        addBinding(TestWebhookDelivery.class);
        addBinding(ListRepoEvents.class);
        addBinding(ListRepoManifests.class);
        addBinding(SaveRepoNotification.class);
        addBinding(DeleteRepoNotification.class);
        addBinding(ListRepoNotifications.class);
        addBinding(GetNotificationRecord.class);
        addBinding(RedeliverWebhook.class);

        //Token CRUD helpers
        addBinding(ListAuthTokens.class);
        addBinding(CreateAuthToken.class);
        addBinding(SetAuthTokenStatus.class);
        addBinding(DeleteAuthToken.class);

        addBinding(SaveStorageSettings.class);
        addBinding(GetStorageSettings.class);
        addBinding(UpdateStorageCreds.class);

        bind(AjaxHelperMap.class).to(AjaxHelperMapImpl.class);
    }

    protected void addBinding(Class<? extends AjaxHelper> clazz)
    {
        addBinding(clazz, null);
    }

    protected void addBinding(Class<? extends AjaxHelper> clazz, String... paths)
    {
        MapBinder<String, AjaxHelper> mapbinder = MapBinder.newMapBinder(binder(), String.class, AjaxHelper.class);
        mapbinder.addBinding(clazz.getSimpleName()).to(clazz);

        MapBinder<String, Set<String>> pathRestrictionBinder = MapBinder.newMapBinder(binder(),
                                                                                      new TypeLiteral<String>(){},
                                                                                      new TypeLiteral<Set<String>>(){});
        if(paths != null)
        {
            Set<String> pathSet = new HashSet<String>();
            for(String path : paths)
                pathSet.add(path);
            pathRestrictionBinder.addBinding(clazz.getSimpleName()).toInstance(pathSet);
        }
    }
}
