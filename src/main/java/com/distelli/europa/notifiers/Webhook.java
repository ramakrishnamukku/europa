/*
  $Id: $
  @file Webhook.java
  @brief Contains the Webhook.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.net.URISyntaxException;

import com.distelli.europa.models.*;
import com.distelli.europa.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

@Log4j
@Accessors(prefix = "_")
public class Webhook
{
    @Getter
    private String _body;
    @Getter @Setter
    private URL _url;
    @Setter
    private String _secret;
    @Getter @Setter
    private String _name;
    @Getter
    private String _eventId;
    @Getter @Setter
    private WebhookRequest _request;
    @Getter @Setter
    private WebhookResponse _response;

    public Webhook(String content)
    {
        if(content == null)
            throw(new IllegalArgumentException("Invalid Webhook Content: "+content));
        _eventId = UUID.randomUUID().toString();
        _body = content;
    }

    public String getSignature()
    {
        if(_secret == null || _secret.trim().isEmpty())
            return null;
        return HmacSha.hmacSha256(_secret, _body);
    }

    public URI getURI()
        throws URISyntaxException
    {
        return _url.toURI();
    }

    // public void send()
    // {
    //     try {

    //         String signature = null;

    //         if(_secret != null && !_secret.trim().isEmpty())
    //             signature = HmacSha.hmacSha256(_secret, webhookBody);

    //         if(signature != null)
    //             _headers.put("x-europa-signature", signature);

    //         String contentType = "application/json";
    //         _headers.put("Content-Type", contentType);
    //         for(Map.Entry<String, String> entry : _headers.entrySet())
    //             httpRequest.addHeader(entry.getKey(), entry.getValue());
    //         httpRequest.setURI(_url.toURI());

    //         StringEntity entity = new StringEntity(webhookBody, StandardCharsets.UTF_8);
    //         entity.setContentType(contentType);
    //         ((HttpEntityEnclosingRequest)httpRequest).setEntity(entity);

    //         _webhookRequest = WebhookRequest
    //         .builder()
    //         .headers(_headers)
    //         .body(webhookBody)
    //         .build();
    //         HttpResponse httpResponse = _httpClient.execute(httpRequest);

    //         _webhookResponse = new WebhookResponse(httpResponse);
    //     } catch(Throwable t) {
    //         throw(new RuntimeException(t));
    //     }
    // }
}
