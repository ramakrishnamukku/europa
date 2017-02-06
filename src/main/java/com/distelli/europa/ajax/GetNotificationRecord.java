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

import com.distelli.europa.util.PermissionCheck;
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
import javax.inject.Provider;

@Log4j
@Singleton
public class GetNotificationRecord extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    protected Provider<ObjectStore> _objectStoreProvider;
    @Inject
    protected Provider<ObjectKeyFactory> _objectKeyFactoryProvider;
    @Inject
    protected PermissionCheck _permissionCheck;

    public GetNotificationRecord()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String id = ajaxRequest.getParam("notificationId",
                                         true); //throw if missing
        String repoId = ajaxRequest.getParam("repoId", true);
        String domain = requestContext.getOwnerDomain();
        _permissionCheck.check(ajaxRequest, requestContext, repoId);

        NotificationId notificationId = NotificationId.fromCanonicalId(id);
        NotificationType type = notificationId.getType();
        switch(type)
        {
        case WEBHOOK:
            ObjectKeyFactory objectKeyFactory = _objectKeyFactoryProvider.get();
            ObjectKey objectKey = objectKeyFactory.forWebhookRecord(notificationId);
            try {
                ObjectStore objectStore = _objectStoreProvider.get();
                byte[] recordBytes = objectStore.get(objectKey);
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
