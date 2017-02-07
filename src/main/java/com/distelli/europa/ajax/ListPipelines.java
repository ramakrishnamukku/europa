package com.distelli.europa.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.util.PermissionCheck;

@Log4j
@Singleton
public class ListPipelines extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private PipelineDb _db;
    @Inject
    protected PermissionCheck _permissionCheck;

    public ListPipelines()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String domain = requestContext.getOwnerDomain();
        PageIterator pageIterator = new PageIterator().pageSize(1000);
        List<Pipeline> pipelineList = _db.listByDomain(domain, pageIterator);
        if(pipelineList == null || pipelineList.size() == 0)
            return pipelineList;
        Map<Pipeline, Boolean> permissionResult = _permissionCheck.checkBatch(ajaxRequest,
                                                                              requestContext,
                                                                              pipelineList);
        List<Pipeline> retval = new ArrayList<Pipeline>();
        for(Pipeline pipeline : pipelineList)
        {
            Boolean allow = permissionResult.get(pipeline);
            if(allow == null || allow == Boolean.FALSE)
                continue;
            retval.add(pipeline);
        }
        return retval;
    }
}
