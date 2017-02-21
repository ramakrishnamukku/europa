/*
  $Id: $
  @file CompositeKey.java
  @brief Contains the CompositeKey.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import java.util.StringJoiner;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class CompositeKey
{
    public static final String DELIM = "\u001e";

    public CompositeKey()
    {

    }

    public String build(String... parts)
    {
        StringJoiner joiner = new StringJoiner(DELIM);
        for(String part : parts)
        {
            if(part == null)
                part = "";
            joiner.add(part);
        }
        return joiner.toString();
    }

    public String buildPrefix(String... parts)
    {
        return build(parts)+DELIM;
    }

    public String[] split(String key)
    {
        return key.split(DELIM);
    }

    public String[] split(String key, int limit)
    {
        return key.split(DELIM, limit);
    }
}
