/*
  $Id: $
  @file TestWebhookDelivery.java
  @brief Contains the TestWebhookDelivery.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.inject.Inject;

import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.models.*;
import com.distelli.europa.notifiers.*;
import com.distelli.europa.util.*;
import com.distelli.webserver.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class TestWebhookDelivery extends AjaxHelper
{
    @Inject
    protected WebhookClient _webhookClient;

    public TestWebhookDelivery()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest)
    {
        Notification notification = ajaxRequest.convertContent("/notification", Notification.class,
                                                               true);
        FieldValidator.validateNonNull(notification, "type", "target");
        FieldValidator.validateEquals(notification, "type", NotificationType.WEBHOOK);

        Webhook webhook = null;
        ImagePushWebhookContent content = new ImagePushWebhookContent();
        DockerImage image = DockerImage
        .builder()
        .imageSha("d573f3ab519f9e83046bbc66d9f68fb11c3a7037")
        .imageSize(100000L)
        .pushTime(System.currentTimeMillis())
        .imageTag("12345")
        .build();

        content.setImage(image);
        content.setRepository(ContainerRepo
                              .builder()
                              .id(UUID.randomUUID().toString())
                              .name("test-repo")
                              .credId(UUID.randomUUID().toString())
                              .region("us-east-1")
                              .provider(RegistryProvider.ECR)
                              .build());
        try {
            webhook = new Webhook(WebhookNotifier.contentToString(content));
        } catch(JsonProcessingException jpe) {
            throw(new RuntimeException(jpe));
        }

        URL url = null;
        try {
            url = new URL(notification.getTarget());
            webhook.setUrl(url);
        } catch(MalformedURLException mue) {
            throw(new AjaxClientException("Invalid Target URL on Webhook Notification: "+notification.getTarget(),
                                          JsonError.Codes.BadContent, 400));
        }
        webhook.setSecret(notification.getSecret());
        webhook.setName(content.getEvent());
        if(log.isDebugEnabled())
            log.debug("Sending Webhook: "+webhook.getEventId()+" for Image: "+image);
        _webhookClient.send(webhook);
        WebhookRequest request = webhook.getRequest();
        WebhookResponse response = webhook.getResponse();

        NotificationId nfId = NotificationId
        .builder()
        .id(webhook.getEventId())
        .type(NotificationType.WEBHOOK)
        .build();

        WebhookRecord record = new WebhookRecord(request, response);
        record.setUrl(url);
        return record;
    }
}
