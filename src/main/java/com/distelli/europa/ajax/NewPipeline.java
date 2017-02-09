package com.distelli.europa.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.distelli.persistence.*;
import com.distelli.webserver.*;
import com.google.inject.Singleton;
import org.eclipse.jetty.http.HttpMethod;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;
import com.distelli.europa.util.PermissionCheck;
import com.distelli.europa.EuropaRequestContext;

@Log4j
@Singleton
public class NewPipeline extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private PipelineDb _db;
    @Inject
    protected PermissionCheck _permissionCheck;

    public NewPipeline()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        _permissionCheck.check(ajaxRequest.getOperation(), requestContext);

        String domain = requestContext.getOwnerDomain();
        String name = ajaxRequest.getParam("name", true);
        PageIterator pageIterator = new PageIterator().pageSize(100);

        Pipeline pipeline = Pipeline.builder()
                                    .domain(domain)
                                    .name(name)
                                    .build();

        _db.createPipeline(pipeline);

        return _db.listByDomain(domain, pageIterator);
    }
}
