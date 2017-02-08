/*
  $Id: $
  @file PermissionCheck.java
  @brief Contains the PermissionCheck.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.distelli.europa.registry.RegistryAccess;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.webserver.AjaxRequest;
import javax.inject.Inject;

public interface PermissionCheck
{
    public void checkRegistryAccess(String operationName, EuropaRequestContext requestContext);
    public void check(AjaxRequest ajaxRequest, EuropaRequestContext requestContext, Object... params);
    public <T> Map<T, Boolean> checkBatch(AjaxRequest ajaxRequest,
                                          EuropaRequestContext requestContext,
                                          List<T> objects);
    public static class Default implements PermissionCheck {
        @Inject
        private RegistryAccess _registryAccess;

        @Override
        public void checkRegistryAccess(String operationName, EuropaRequestContext requestContext) {
            if(!requestContext.isRegistryApiRequest())
                return;
            _registryAccess.checkAccess(operationName, requestContext);
        }
        public void check(AjaxRequest ajaxRequest, EuropaRequestContext requestContext, Object... params) {}
        public <T> Map<T, Boolean> checkBatch(AjaxRequest ajaxRequest, EuropaRequestContext requestContext, List<T> objects) {
            Map<T, Boolean> result = new HashMap<T, Boolean>();
            for(T obj : objects)
                result.put(obj, Boolean.TRUE);
            return result;
        }
    }
}
