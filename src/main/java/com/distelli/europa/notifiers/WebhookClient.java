/*
  $Id: $
  @file WebhookClient.java
  @brief Contains the WebhookClient.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import com.distelli.europa.models.*;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import com.google.inject.Singleton;

import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import lombok.Getter;
import lombok.Setter;

@Log4j
@Accessors(prefix = "_")
@Singleton
public class WebhookClient
{
    private CloseableHttpClient _httpClient;

    public WebhookClient()
    {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(10);
        clientBuilder.setConnectionManager(connManager);
        _httpClient = clientBuilder.build();
    }

    public void send(Webhook webhook)
    {
        try {
            Map<String, String> headers = new HashMap<String, String>();
            HttpRequestBase httpRequest = new HttpPost();
            headers.put("x-europa-event", webhook.getName());
            headers.put("x-europa-event-id", webhook.getEventId());
            String signature = webhook.getSignature();
            if(signature != null)
                headers.put("x-europa-signature", signature);
            httpRequest.setURI(webhook.getURI());

            for(Map.Entry<String, String> entry : headers.entrySet())
                httpRequest.addHeader(entry.getKey(), entry.getValue());

            String webhookBody = webhook.getBody();
            StringEntity entity = new StringEntity(webhookBody, StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            ((HttpEntityEnclosingRequest)httpRequest).setEntity(entity);

            WebhookRequest webhookRequest = WebhookRequest
            .builder()
            .headers(headers)
            .body(webhookBody)
            .build();
            webhook.setRequest(webhookRequest);

            HttpResponse httpResponse = _httpClient.execute(httpRequest);
            WebhookResponse webhookResponse = new WebhookResponse(httpResponse);

            webhook.setResponse(webhookResponse);
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }
    }
}
