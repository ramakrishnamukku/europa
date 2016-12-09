/*
  $Id: $
  @file WebhookRequest.java
  @brief Contains the WebhookRequest.java class

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
public class WebhookRequest
{
    private String body;
    @Singular
    private Map<String, String> headers;
}
