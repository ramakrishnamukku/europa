/*
  $Id: $
  @file CreateLocalRepo.java
  @brief Contains the CreateLocalRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.models.ContainerRepo;
import com.distelli.europa.models.RegistryProvider;
import com.distelli.europa.util.PermissionCheck;
import com.distelli.utils.CompactUUID;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonSuccess;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class CreateLocalRepo extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    protected ContainerRepoDb _repoDb;
    @Inject
    protected PermissionCheck _permissionCheck;

    private final Pattern repoNamePattern = Pattern.compile("[a-zA-Z0-9_\\.-]+");

    public CreateLocalRepo()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        _permissionCheck.check(ajaxRequest, requestContext);

        String ownerDomain = requestContext.getOwnerDomain();
        String repoName = ajaxRequest.getParam("repoName", true);
        ContainerRepo repo = _repoDb.getRepo(ownerDomain, RegistryProvider.EUROPA, "", repoName);
        if(repo != null)
            throw(new AjaxClientException("The specified Repository already exists",
                                          AjaxErrors.Codes.RepoAlreadyExists,
                                          400));
        Matcher m = repoNamePattern.matcher(repoName);
        if(!m.matches())
            throw(new AjaxClientException("The Repo Name is invalid. It must match regex [a-zA-Z_.-]",
                                          AjaxErrors.Codes.BadRepoName,
                                          400));

        repo = ContainerRepo.builder()
            .domain(ownerDomain)
            .name(repoName)
            .region("")
            .provider(RegistryProvider.EUROPA)
            .local(true)
            .publicRepo(false)
            .build();

        repo.setOverviewId(CompactUUID.randomUUID().toString());
        repo.setId(CompactUUID.randomUUID().toString());
        _repoDb.save(repo);
        return JsonSuccess.Success;
    }
}
