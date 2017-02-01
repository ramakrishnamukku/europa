package com.distelli.europa.react;

import java.net.InetAddress;
import javax.inject.Inject;

import com.distelli.europa.models.DnsSettings;
import com.distelli.webserver.*;

import lombok.extern.log4j.Log4j;

@Log4j
public class JSXProperties
{
    private RequestContext _requestContext;
    private DnsSettings _dnsSettings;

    public JSXProperties(RequestContext requestContext) {
        _requestContext = requestContext;
    }
    public JSXProperties(RequestContext requestContext, DnsSettings dnsSettings)
    {
        _requestContext = requestContext;
        _dnsSettings = dnsSettings;
    }

    public String getDnsName()
    {
        if(_dnsSettings == null)
        {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch(Throwable t) {
                log.error(t.getMessage(), t);
                return null;
            }
        }
        return _dnsSettings.getDnsName();
    }
}
