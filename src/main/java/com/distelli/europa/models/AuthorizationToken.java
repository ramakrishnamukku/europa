/*
  $Id: $
  @file AuthorizationToken.java
  @brief Contains the AuthorizationToken.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationToken
{
    protected String token;
    protected URI endpoint;
}
