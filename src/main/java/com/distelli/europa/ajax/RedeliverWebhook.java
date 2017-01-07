/*
  $Id: $
  @file RedeliverWebhook.java
  @brief Contains the RedeliverWebhook.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;

import com.distelli.europa.models.*;
import com.distelli.europa.notifiers.*;
import com.distelli.europa.util.*;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonError;
import com.distelli.objectStore.*;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RedeliverWebhook extends AjaxHelper
{
    @Inject
    private WebhookNotifier _webhookNotifier;
    @Inject
    private ObjectKeyFactory _objectKeyFactory;
    @Inject
    private ObjectStore _objectStore;

    public RedeliverWebhook()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest)
    {
        String id = ajaxRequest.getParam("notificationId",
                                         true); //throw if missing
        NotificationId notificationId = NotificationId.fromCanonicalId(id);
        NotificationType type = notificationId.getType();
        if(type != NotificationType.WEBHOOK)
            throw(new AjaxClientException("Invalid WebhookId: "+id, JsonError.Codes.BadContent, 400));
        ObjectKey objectKey = _objectKeyFactory.forWebhookRecord(notificationId);
        WebhookRecord record = null;
        try {
            byte[] recordBytes = _objectStore.get(objectKey);
            record = WebhookRecord.fromJsonBytes(recordBytes);
        } catch(EntityNotFoundException enfe) {
            throw(new AjaxClientException("Nonexistent WebhookId: "+id, JsonError.Codes.BadContent, 400));
        } catch(IOException ioe) {
            throw(new RuntimeException(ioe));
        }
        NotificationId nfId = _webhookNotifier.notify(record);
        return nfId;
    }
}
