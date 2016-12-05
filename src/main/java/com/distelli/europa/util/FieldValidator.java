/*
  $Id: $
  @file FieldValidator.java
  @brief Contains the FieldValidator.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import org.apache.log4j.Logger;
import java.lang.reflect.*;
import com.distelli.europa.webserver.*;
import com.distelli.europa.ajax.*;

public class FieldValidator
{
    private static final Logger log = Logger.getLogger(FieldValidator.class);

    public static void validateNonNull(Object obj, String... fields)
        throws AjaxClientException
    {
        Class<? extends Object> clazz = obj.getClass();
        for(String field : fields)
        {
            try {
                String methodName = "get"+field.substring(0, 1).toUpperCase()+field.substring(1);
                Method method = clazz.getMethod(methodName);
                Object value = method.invoke(obj);
                if(value == null)
                    throw(new AjaxClientException("Missing Field '"+field+"' in content",
                                                  JsonError.Codes.BadContent,
                                                  400));
            } catch(AjaxClientException ace) {
                throw(ace);
            } catch(NoSuchMethodException nsme) {
                    throw(new AjaxClientException("Missing Field '"+field+"' in content",
                                                  JsonError.Codes.BadContent,
                                                  400));
            } catch(Throwable t) {
                throw(new RuntimeException(t));
            }
        }
    }
}
