/*
  $Id: $
  @file WebhookNotifier.java
  @brief Contains the WebhookNotifier.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.net.MalformedURLException;
import java.net.URL;

import com.distelli.europa.models.*;

import lombok.extern.log4j.Log4j;

@Log4j
public class WebhookNotifier
{
    public WebhookNotifier() {

    }

    public NotificationId notify(Notification notification, DockerImage image, ContainerRepo repo)
    {
        ImagePushWebhookContent content = new ImagePushWebhookContent();
        content.setImage(image);
        content.setRepository(repo);

        Webhook<ImagePushWebhookContent> wh = new Webhook<ImagePushWebhookContent>(content);
        try {
            wh.setUrl(new URL(notification.getTarget()));
        } catch(MalformedURLException mue) {
            //the url is malformed. Lets not notify
            return null;
        }
        wh.setSecret(notification.getSecret());
        wh.setWebhookName(content.getEvent());
        wh.send();
        WebhookRequest request = wh.getWebhookRequest();
        WebhookResponse response = wh.getWebhookResponse();

        NotificationId nfId = NotificationId
        .builder()
        .id(wh.getEventId())
        .type(NotificationType.WEBHOOK)
        .build();

        //TODO: Save the request and response in the object store
        //using the canonical nfId
        return nfId;
    }
}
