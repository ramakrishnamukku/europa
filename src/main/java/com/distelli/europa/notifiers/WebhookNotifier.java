/*
  $Id: $
  @file WebhookNotifier.java
  @brief Contains the WebhookNotifier.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Provider;
import javax.inject.Inject;

import com.distelli.europa.*;
import com.distelli.europa.models.*;
import com.distelli.europa.util.*;
import com.distelli.objectStore.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import lombok.extern.log4j.Log4j;

@Log4j
public class WebhookNotifier
{
    @Inject
    protected Provider<ObjectStore> _objectStoreProvider;
    @Inject
    protected Provider<ObjectKeyFactory> _objectKeyFactoryProvider;
    @Inject
    protected WebhookClient _webhookClient;

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

    public WebhookNotifier() {

    }

    public static String contentToString(ImagePushWebhookContent content)
        throws JsonProcessingException
    {
        return OBJECT_MAPPER.writeValueAsString(content);
    }

    public NotificationId notify(WebhookRecord record)
    {
        WebhookRequest request = record.getRequest();
        Webhook webhook = new Webhook(request.getBody());
        webhook.setUrl(record.getUrl());

        webhook.setSecret(record.getSecret());
        webhook.setName(ImagePushWebhookContent.EVENT_NAME);
        if(log.isDebugEnabled())
            log.debug("Sending Webhook: "+webhook.getEventId()+" for Record: "+record);
        _webhookClient.send(webhook);
        request = webhook.getRequest();
        WebhookResponse response = webhook.getResponse();

        NotificationId nfId = NotificationId
        .builder()
        .id(webhook.getEventId())
        .type(NotificationType.WEBHOOK)
        .build();

        record.setNotificationId(nfId.toCanonicalId());
        record.setRequest(request);
        record.setResponse(response);
        record.setUrl(record.getUrl());
        record.setSecret(record.getSecret());
        record.setNotificationTime(System.currentTimeMillis());
        saveNotificationRecord(nfId, record);
        return nfId;
    }

    public NotificationId notify(Notification notification, DockerImage image, ContainerRepo repo)
    {
        ImagePushWebhookContent content = new ImagePushWebhookContent();
        content.setImage(image);
        content.setRepository(repo);

        Webhook webhook = null;
        try {
            webhook = new Webhook(contentToString(content));
        } catch(JsonProcessingException jpe) {
            log.error("Skipping Webhook notification for Image: "+image+
                      " and repo: "+repo+
                      ". Malformed content: "+content);
            return null;
        }
        URL url = null;
        try {
            url = new URL(notification.getTarget());
            webhook.setUrl(url);
        } catch(MalformedURLException mue) {
            if(log.isDebugEnabled())
                log.debug("Skipping Webhook notification for Image: "+image+
                          " and repo: "+repo+
                          ". Malformed URL: "+notification.getTarget());
            return null;
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
        record.setSecret(notification.getSecret());
        record.setNotificationId(nfId.toCanonicalId());
        record.setNotificationTime(System.currentTimeMillis());
        saveNotificationRecord(nfId, record);
        return nfId;
    }

    public void saveNotificationRecord(NotificationId notificationId, WebhookRecord record)
    {
        try {
            ObjectKeyFactory objectKeyFactory = _objectKeyFactoryProvider.get();
            ObjectKey objectKey = objectKeyFactory.forWebhookRecord(notificationId);
            byte[] recordBytes = record.toJsonBytes();
            if(log.isDebugEnabled())
                log.debug("Saving WebhookRecord: "+record+
                          " for NotificationId: "+notificationId+
                          " to ObjectKey: "+objectKey);
            ObjectStore objectStore = _objectStoreProvider.get();
            objectStore.put(objectKey, recordBytes);
        } catch(Throwable t) {
            log.error("Failed to write WebhookRecord: "+record+": "+t.getMessage(), t);
        }
    }
}
