/*
  $Id: $
  @file GetNotificationRecord.java
  @brief Contains the GetNotificationRecord.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import java.io.IOException;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import com.distelli.europa.models.*;
import com.distelli.europa.notifiers.*;
import com.distelli.europa.util.*;
import com.distelli.objectStore.*;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.RequestContext;
import com.google.inject.Singleton;
import com.distelli.europa.EuropaRequestContext;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class GetNotificationRecord extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    protected ObjectStore _objectStore;
    @Inject
    protected ObjectKeyFactory _objectKeyFactory;

    public GetNotificationRecord()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String id = ajaxRequest.getParam("notificationId",
                                         true); //throw if missing
        NotificationId notificationId = NotificationId.fromCanonicalId(id);
        NotificationType type = notificationId.getType();
        switch(type)
        {
        case WEBHOOK:
            ObjectKey objectKey = _objectKeyFactory.forWebhookRecord(notificationId);
            try {
                byte[] recordBytes = _objectStore.get(objectKey);
                WebhookRecord webhookRecord = WebhookRecord.fromJsonBytes(recordBytes);
                webhookRecord.setSecret(null);
                return webhookRecord;
            } catch(EntityNotFoundException enfe) {
                return null;
            } catch(IOException ioe) {
                throw(new RuntimeException(ioe));
            }
        case EMAIL:
        case SLACK:
        case HIPCHAT:
        default:
            return null;
        }
    }
}
