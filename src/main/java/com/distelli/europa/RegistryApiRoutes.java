/*
  $Id: $
  @file RegistryApiRoutes.java
  @brief Contains the RegistryApiRoutes.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import com.distelli.webserver.RouteMatcher;
import com.distelli.europa.handlers.*;
import lombok.extern.log4j.Log4j;

@Log4j
public class RegistryApiRoutes
{
    private static final RouteMatcher ROUTES = new RouteMatcher();
    public static RouteMatcher getRouteMatcher() {
        return ROUTES;
    }

    static {
        //Add the routes below this line
        ROUTES.add("GET", "/v2", RegistryVersionCheck.class);

        // Manifest:
        ROUTES.add("PUT", "/v2/:name/manifests/:reference", RegistryManifestPush.class);
        ROUTES.add("GET", "/v2/:name/manifests/:reference", RegistryManifestPull.class);
        ROUTES.add("HEAD", "/v2/:name/manifests/:reference", RegistryManifestExists.class);
        ROUTES.add("DELETE", "/v2/:name/manifests/:reference", RegistryManifestDelete.class);

        // Blobs:
        ROUTES.add("GET", "/v2/:name/blobs/:digest", RegistryLayerPull.class);
        ROUTES.add("HEAD", "/v2/:name/blobs/:digest", RegistryLayerExists.class);
        ROUTES.add("DELETE", "/v2/:name/blobs/:digest", RegistryLayerDelete.class);

        // Blob Upload:
        ROUTES.add("POST", "/v2/:name/blobs/uploads", RegistryLayerUploadBegin.class);
        ROUTES.add("PUT", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadFinish.class);
        ROUTES.add("PATCH", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadChunk.class);
        ROUTES.add("GET", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadProgress.class);
        ROUTES.add("DELETE", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadCancel.class);

        ROUTES.add("GET", "/v2/:name/tags/list", RegistryTagList.class);
        ROUTES.add("GET", "/v2/_catalog", RegistryCatalog.class);

        ROUTES.add("GET", "/v2/token", RegistryTokenHandler.class);
        //TODO: set a default route matcher that returns an error json for the registry API
        ROUTES.setDefaultRequestHandler(RegistryDefault.class);
    }
}
