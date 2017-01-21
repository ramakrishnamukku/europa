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

@Log4j
@Singleton
public class AddPipelineComponent extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private PipelineDb _db;

    public AddPipelineComponent()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String pipelineId = ajaxRequest.getParam("pipelineId", true);
        String destinationContainerRepoId = ajaxRequest.getParam("destinationContainerRepoId", true);

        PipelineComponent component = PCCopyToRepository.builder()
            .destinationContainerRepoId(destinationContainerRepoId)
            .build();

        _db.addPipelineComponent(pipelineId, component, null);

        return _db.getPipeline(pipelineId);
    }
}
