/*
  $Id: $
  @file TokenScope.java
  @brief Contains the TokenScope.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.registry;

import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
@Log4j
public class TokenScope
{
    private static final String PULL_OPERATION = "pull";

    protected String repoName;
    protected String operationName;
    protected String scopeName;

    protected TokenScope()
    {

    }

    public static TokenScope fromString(String scope)
    {
        TokenScope tokenScope = new TokenScope();
        if(scope == null)
            return tokenScope;
        String[] parts = scope.split(":");
        if(parts.length != 3)
        {
            log.error("Invalid token scope: "+scope);
            return tokenScope;
        }

        tokenScope.setScopeName(parts[0]);
        tokenScope.setRepoName(parts[1]);
        tokenScope.setOperationName(parts[2]);
        return tokenScope;
    }

    public boolean isPullRequest()
    {
        if(this.operationName == null)
            return false;
        if(this.operationName.equalsIgnoreCase(PULL_OPERATION))
            return true;
        return false;
    }

    public String getRepoName()
    {
        return this.repoName;
    }
}
