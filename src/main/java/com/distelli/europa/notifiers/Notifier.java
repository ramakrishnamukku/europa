/*
  $Id: $
  @file Notifier.java
  @brief Contains the Notifier.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import com.distelli.europa.models.*;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;

@Log4j
public class Notifier
{
    @Inject
    private WebhookNotifier _webhookNotifier;

    public Notifier()
    {

    }

    public NotificationId notify(Notification notification, DockerImage image, ContainerRepo repo)
    {
        NotificationType notificationType = notification.getType();
        if(notificationType == null)
            return null;
        try {
            switch(notificationType)
            {
            case WEBHOOK:
                return _webhookNotifier.notify(notification, image, repo);
            case EMAIL:
            case SLACK:
            case HIPCHAT:
            default:
                return null;
            }
        } catch(Throwable t) {
            log.error("Failed notification: "+notification+
                      " for Image: "+image+
                      " and Repository: "+repo+": "+t.getMessage(), t);
        }
        return null;
    }
}
