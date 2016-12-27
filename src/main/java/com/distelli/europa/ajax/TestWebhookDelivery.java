/*
  $Id: $
  @file TestWebhookDelivery.java
  @brief Contains the TestWebhookDelivery.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import org.apache.log4j.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import com.distelli.europa.models.*;
import com.distelli.ventura.*;
import com.distelli.europa.util.*;
import javax.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class TestWebhookDelivery implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(TestWebhookDelivery.class);

    public TestWebhookDelivery()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        Notification notification = ajaxRequest.convertContent("/notification", Notification.class,
                                                               true);
        FieldValidator.validateNonNull(notification, "type", "target");
        FieldValidator.validateEquals(notification, "type", NotificationType.WEBHOOK);
        Random rand = new Random(System.currentTimeMillis());
        int random = rand.nextInt(10);
        int httpStatusCode = 400;
        if(random % 2 == 0)
            httpStatusCode = 200;

        WebhookRequest whRequest = WebhookRequest.builder()
        .body("{ \"some\":\"webhook\", \"body\":\"yo!\"}")
        .header("Content-Type", "application/json")
        .header("Server", "DistelliCallisto")
        .build();

        WebhookResponse whResponse = WebhookResponse.builder()
        .body("{ \"some\":\"webhook\", \"response\":\"yo!\"}")
        .header("Content-Type", "application/json")
        .header("Server", "SomeServer")
        .httpStatusCode(httpStatusCode)
        .build();

        Map<String, Object> webhookData = new HashMap<String, Object>();
        webhookData.put("request", whRequest);
        webhookData.put("response", whResponse);
        return webhookData;
    }
}
