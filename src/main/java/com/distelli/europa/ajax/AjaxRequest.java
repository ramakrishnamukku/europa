/*
  $Id: $
  @file AjaxRequest.java
  @brief Contains the AjaxRequest.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class AjaxRequest
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected String operation = null;
    protected Map<String, String> params = null;
    protected JsonNode content = null;

    public AjaxRequest()
    {

    }

    public void setContent(JsonNode content)
    {
        this.content = content;
    }

    public Object getContent()
    {
        return this.content;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public String getOperation()
    {
        return this.operation;
    }

    public void setParams(Map<String, String> params)
    {
        this.params = params;
    }

    public Map<String, String> getParams()
    {
        return this.params;
    }

    public String getParam(String key) {
        if(this.params == null)
            return null;
        return this.params.get(key);
    }

    public static AjaxRequest fromJson(JsonNode node)
    {
        AjaxRequest ajaxRequest = new AjaxRequest();
        String operation = node.at("/op").asText();
        if(operation != null && !operation.trim().isEmpty())
            ajaxRequest.setOperation(operation);
        JsonNode paramsNode = node.at("/params");
        if(!paramsNode.isMissingNode())
        {
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String,String>>(){};
            HashMap<String, String> params = OBJECT_MAPPER.convertValue(paramsNode, typeRef);

            ajaxRequest.setParams(params);
        }
        JsonNode contentNode = node.at("/content");
        if(!contentNode.isMissingNode())
            ajaxRequest.setContent(contentNode);
        return ajaxRequest;
    }

    public static AjaxRequest fromQueryParams(Map<String, List<String>> queryParams)
    {
        AjaxRequest ajaxRequest = new AjaxRequest();
        String operation = getParam("op", queryParams);
        if(operation != null && !operation.trim().isEmpty())
            ajaxRequest.setOperation(operation);

        Map<String, String> params = null;
        for(String param : queryParams.keySet())
        {
            if(param.equalsIgnoreCase("op"))
                continue;
            String value = getParam(param, queryParams);
            if(value != null)
            {
                if(params == null)
                    params = new HashMap<String, String>();
                params.put(param, value);
            }
        }
        ajaxRequest.setParams(params);
        return ajaxRequest;
    }

    private static String getParam(String key, Map<String, List<String>> params)
    {
        if(params == null)
            return null;
        List<String> values = params.get(key);
        if(values == null || values.size() == 0)
            return null;
        return values.get(0);
    }

    @Override
    public String toString() {
        return String.format("AjaxRequest[content=%s, op=%s, params=%s]", content, operation, params);
    }
}
