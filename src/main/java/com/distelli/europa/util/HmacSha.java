/*
  $Id: $
  @file HmacSha.java
  @brief Contains the HmacSha.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.log4j.Log4j;

@Log4j
public class HmacSha
{
    public HmacSha()
    {

    }

    public static String hmacSha1(String secretKey, String data)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(data.getBytes("UTF-8"));

            return Base64.getEncoder().encodeToString(digest);
        }
        catch(NoSuchAlgorithmException nsae)
        {
            //cannot happen.
            throw(new RuntimeException(nsae));
        }
        catch(UnsupportedEncodingException usee)
        {
            throw(new RuntimeException(usee));
        }
        catch(InvalidKeyException ike)
        {
            throw(new RuntimeException(ike));
        }
    }

    public static String hmacSha256(String secretKey, String data)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secret);
            byte[] digest = mac.doFinal(data.getBytes("UTF-8"));

            return Base64.getEncoder().encodeToString(digest);
        }
        catch(NoSuchAlgorithmException nsae)
        {
            //cannot happen.
            throw(new RuntimeException(nsae));
        }
        catch(UnsupportedEncodingException usee)
        {
            throw(new RuntimeException(usee));
        }
        catch(InvalidKeyException ike)
        {
            throw(new RuntimeException(ike));
        }
    }
}
