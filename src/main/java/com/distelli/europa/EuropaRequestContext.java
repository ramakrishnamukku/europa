/*
  $Id: $
  @file EuropaRequestContext.java
  @brief Contains the EuropaRequestContext.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa;

import com.distelli.webserver.HTTPMethod;
import javax.servlet.http.HttpServletRequest;
import com.distelli.webserver.RequestContext;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lombok.experimental.Accessors;

@Log4j
@ToString
@Accessors(prefix="_")
public class EuropaRequestContext extends RequestContext
{
    @Getter @Setter
    protected String _requesterDomain = Constants.DOMAIN_ZERO;
    @Getter @Setter
    protected String _requesterUsername = null;
    @Getter @Setter
    protected String _ownerDomain = Constants.DOMAIN_ZERO;
    @Getter @Setter
    protected String _ownerUsername = null;
    @Getter @Setter
    protected boolean _ajaxRequest = false;
    @Getter @Setter
    protected boolean _registryApiRequest = false;

    public EuropaRequestContext(HTTPMethod httpMethod, HttpServletRequest request, boolean unmarshallJson)
    {
        super(httpMethod, request, unmarshallJson);
    }

    public EuropaRequestContext(HTTPMethod httpMethod, HttpServletRequest request)
    {
        super(httpMethod, request, true);
    }
}
