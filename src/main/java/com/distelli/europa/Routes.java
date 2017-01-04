/*
  $Id: $
  @file Routes.java
  @brief Contains the Routes.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import com.distelli.ventura.RouteMatcher;
import com.distelli.europa.handlers.*;

public class Routes
{
    private static final RouteMatcher ROUTES = new RouteMatcher();
    public static RouteMatcher getRouteMatcher() {
        return ROUTES;
    }

    static {
        //Add the routes below this line
        //    ROUTES.add("GET", "/:username/path/foo", FooRequestHandler.class);

        //Ajax Routes
        ROUTES.add("GET", "/ajax", AjaxRequestHandler.class);
        ROUTES.add("POST", "/ajax", AjaxRequestHandler.class);

        ROUTES.add("GET", "/v2", RegistryVersionCheck.class);

        ROUTES.add("PUT", "/v2/:name/manifests/:reference", RegistryManifestPush.class);
        ROUTES.add("GET", "/v2/:name/manifests/:reference", RegistryManifestPull.class);
        ROUTES.add("DELETE", "/v2/:name/manifests/:reference", RegistryManifestDelete.class);

        ROUTES.add("GET", "/v2/:name/blobs/:digest", RegistryLayerPull.class);
        ROUTES.add("HEAD", "/v2/:name/blobs/:digest", RegistryLayerExists.class);
        ROUTES.add("DELETE", "/v2/:name/blobs/:digest", RegistryLayerDelete.class);

        // Must support multipart uploads:
        // http://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html

        // ?digest=<digest> ?mount=<digest>&from=<repository name>
        ROUTES.add("POST", "/v2/:name/blobs/uploads/", RegistryLayerUploadBegin.class);
        ROUTES.add("PUT", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadFinish.class);
//        ROUTES.add("PATCH", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadChunk.class);
        ROUTES.add("GET", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadProgress.class);
        ROUTES.add("DELETE", "/v2/:name/blobs/uploads/:uuid", RegistryLayerUploadCancel.class);

        ROUTES.add("GET", "/v2/:name/tags/list", RegistryTagList.class);
        ROUTES.add("GET", "/v2/_catalog", RegistryCatalog.class);

        ROUTES.add("GET", "/public/images/*", "static-home");
        ROUTES.add("GET", "/public/css/*", "static-home");
        ROUTES.add("GET", "/public/js/*", "static-home");
        ROUTES.add("GET", "/public/images/registry-icons/*", "static-home");
        ROUTES.add("GET", "/public/images/timeline-icons/*", "static-home");

        ROUTES.setDefaultRequestHandler(DefaultRequestHandler.class);
    }
}
