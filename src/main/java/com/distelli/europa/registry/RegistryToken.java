/*
  $Id: $
  @file RegistryToken.java
  @brief Contains the RegistryToken.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.registry;

import java.util.Base64;

import lombok.Getter;
import lombok.ToString;

@ToString
public class RegistryToken
{
    @Getter
    private String token;

    private static final String PUBLIC_TOKEN_STRING = "PUBLIC";
    public static RegistryToken PUBLIC_TOKEN = new RegistryToken(PUBLIC_TOKEN_STRING);

    protected RegistryToken(String token)
    {
        this.token = Base64.getEncoder().encodeToString(token.getBytes());
    }

    public static RegistryToken fromString(String token)
    {
        return new RegistryToken(token);
    }

    public static boolean isPublicToken(String token)
    {
        return PUBLIC_TOKEN_STRING.equalsIgnoreCase(token);
    }
}
