/*
  $Id: $
  @file WebhookResponse.java
  @brief Contains the WebhookResponse.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.util.Map;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse
{
    private int httpStatusCode;
    private String body;
    @Singular
    private Map<String, String> headers;
}
