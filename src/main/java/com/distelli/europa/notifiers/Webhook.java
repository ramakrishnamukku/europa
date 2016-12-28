/*
  $Id: $
  @file Webhook.java
  @brief Contains the Webhook.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import java.nio.charset.StandardCharsets;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import com.distelli.europa.util.*;
import com.distelli.europa.models.*;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

@Log4j
@Accessors(prefix = "_")
public class Webhook<T>
{
    private CloseableHttpClient _httpClient;
    private Map<String, String> _headers;
    private T _webhookContent;

    @Getter @Setter
    private URL _url;
    @Getter @Setter
    private String _secret;
    @Getter @Setter
    private String _webhookName;
    @Getter
    private String _eventId;
    @Getter
    private WebhookRequest _webhookRequest;
    @Getter
    private WebhookResponse _webhookResponse;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static
    {
        OBJECT_MAPPER.setVisibilityChecker(new VisibilityChecker.Std(JsonAutoDetect.Visibility.NONE,  //getters
                                                                     JsonAutoDetect.Visibility.NONE,  //is-getters
                                                                     JsonAutoDetect.Visibility.NONE,  //setters
                                                                     JsonAutoDetect.Visibility.NONE,  //creator
                                                                     JsonAutoDetect.Visibility.ANY)); //field

        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Webhook(T webhookContent)
    {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(10);
        _httpClient = clientBuilder.build();
        _webhookContent = webhookContent;
        if(_webhookContent == null)
            throw(new IllegalArgumentException("Invalid Webhook Content: "+webhookContent));
    }

    public void send()
    {
        try {
            HttpRequestBase httpRequest = new HttpPost();
            if(_headers == null)
                _headers = new HashMap<String, String>();
            _headers.put("x-europa-event", _webhookName);
            _eventId = UUID.randomUUID().toString();
            String signature = null;
            String webhookBody = OBJECT_MAPPER.writeValueAsString(_webhookContent);
            if(_secret != null)
                signature = HmacSha.hmacSha256(_secret, webhookBody);

            if(signature != null)
                _headers.put("x-europa-signature", signature);
            _headers.put("x-europa-event-id", _eventId);
            String contentType = "application/json";
            _headers.put("Content-Type", contentType);
            for(Map.Entry<String, String> entry : _headers.entrySet())
                httpRequest.addHeader(entry.getKey(), entry.getValue());
            httpRequest.setURI(_url.toURI());

            StringEntity entity = new StringEntity(webhookBody, StandardCharsets.UTF_8);
            entity.setContentType(contentType);
            ((HttpEntityEnclosingRequest)httpRequest).setEntity(entity);

            _webhookRequest = WebhookRequest
            .builder()
            .headers(_headers)
            .body(webhookBody)
            .build();
            HttpResponse httpResponse = _httpClient.execute(httpRequest);

            _webhookResponse = new WebhookResponse(httpResponse);
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }
    }
}
