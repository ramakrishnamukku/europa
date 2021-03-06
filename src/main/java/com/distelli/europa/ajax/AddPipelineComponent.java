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
import com.distelli.europa.models.PipelineComponent;
import com.distelli.europa.models.PCCopyToRepository;
import com.google.inject.Singleton;
import org.eclipse.jetty.http.HttpMethod;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.util.PermissionCheck;
import java.util.Map;
import java.util.HashMap;

@Log4j
@Singleton
public class AddPipelineComponent extends AjaxHelper<EuropaRequestContext>
{
    private static final Map<String, Class<? extends PipelineComponent>> TYPES = new HashMap<>();
    static {
        TYPES.put("CopyToRepository", PCCopyToRepository.class);
    }

    @Inject
    private PipelineDb _db;
    @Inject
    protected PermissionCheck _permissionCheck;

    public AddPipelineComponent()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String typeName = ajaxRequest.getParam("type", true);
        String pipelineId = ajaxRequest.getParam("pipelineId", true);
        _permissionCheck.check(ajaxRequest.getOperation(), requestContext, pipelineId);

        Class<? extends PipelineComponent> type = TYPES.get(typeName);

        PipelineComponent component = ajaxRequest.convertContent(type, true);
        component.validate("content@"+typeName);

        _db.addPipelineComponent(
            pipelineId,
            component,
            ajaxRequest.getParam("beforeComponentId"));

        return _db.getPipeline(pipelineId);
    }
}
