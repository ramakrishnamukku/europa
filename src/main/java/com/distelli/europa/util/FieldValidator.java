/*
  $Id: $
  @file FieldValidator.java
  @brief Contains the FieldValidator.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import java.lang.reflect.*;
import com.distelli.webserver.*;
import com.distelli.europa.ajax.*;
import java.util.regex.*;
import lombok.extern.log4j.Log4j;

@Log4j
public class FieldValidator
{
    private static Object getValueForField(Object obj, String field)
    {
        Class<? extends Object> clazz = obj.getClass();
        try {
            String methodName = "get"+field.substring(0, 1).toUpperCase()+field.substring(1);
            Method method = clazz.getMethod(methodName);
            Object value = method.invoke(obj);
            return value;
        } catch(NoSuchMethodException nsme) {
            return null;
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }
    }

    public static void validateNonNull(Object obj, String... fields)
        throws AjaxClientException
    {
        for(String field : fields)
        {
            Object value = getValueForField(obj, field);
            if(value == null)
                throw(new AjaxClientException("Missing Field '"+field+"' in content",
                                              JsonError.Codes.BadContent,
                                              400));
        }
    }

    public static void validateNonNullOrEmpty(Object obj, String... fields)
    {
        for(String field : fields)
        {
            Object value = getValueForField(obj, field);
            if(value == null)
                throw(new AjaxClientException("Missing Field '"+field+"' in content",
                                              JsonError.Codes.BadContent,
                                              400));
            String strVal = (String)value;
            if(strVal.trim().isEmpty())
                throw(new AjaxClientException("Missing Field '"+field+"' in content",
                                              JsonError.Codes.BadContent,
                                              400));
        }
    }

    public static void validateMatch(Object obj, String field, Pattern pattern)
    {
        Object value = getValueForField(obj, field);
        if(value == null || (!(value instanceof String)))
            throw(new AjaxClientException("Invalid value '"+value+"' for field '"+field+"' "+
                                          "in content, does not match pattern: "+pattern.pattern(),
                                          JsonError.Codes.BadContent,
                                          400));
        String strVal = (String)value;
        Matcher m = pattern.matcher(strVal);
        if(m.matches())
            return;
        throw(new AjaxClientException("Invalid value '"+strVal+"' for field '"+field+"' "+
                                      "in content, does not match pattern: "+pattern.pattern(),
                                      JsonError.Codes.BadContent,
                                      400));
    }

    public static void validateEquals(Object obj, String field, Object expectedValue)
    {
        Object value = getValueForField(obj, field);
        if(value == null)
            throw(new AjaxClientException("Missing value '"+value+"' for field '"+field+"' "+
                                          "in content. Value must be: "+expectedValue,
                                          JsonError.Codes.BadContent,
                                          400));
        Class<?> expectedClass = expectedValue.getClass();
        Class<?> actualClass = value.getClass();
        if(!actualClass.equals(expectedClass))
            throw(new AjaxClientException("Invalid value '"+value+"' for field '"+field+"' "+
                                          "in content. Value must be: "+expectedValue,
                                          JsonError.Codes.BadContent,
                                          400));
        if(!value.equals(expectedValue))
            throw(new AjaxClientException("Invalid value '"+value+"' for field '"+field+"' "+
                                          "in content. Value must be: "+expectedValue,
                                          JsonError.Codes.BadContent,
                                          400));
    }
}
