/*
  $Id: $
  @file DateUtil.java
  @brief Contains the DateUtil.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.log4j.Log4j;

@Log4j
public class DateUtil
{
    private static DateFormat DATE_PARSER_WITH_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    private static DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static DateFormat ISO_8601_DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");

    public static Long parseDate(String ts)
    {
        if ( null == ts || "".equals(ts) ) return null;
        Date date = null;
        try {
           date = DATE_PARSER.parse(ts);
        } catch (Exception e){
            log.error("Error parsing date: " + ts, e);
        }

        if ( null == date ) return null;
        return date.getTime();
    }

    public static Long parseDateWithTimezone(String ts)
    {
        if ( null == ts || "".equals(ts) ) return null;
        Date date = null;
        try {
           date = DATE_PARSER_WITH_TIMEZONE.parse(ts);
        } catch (Exception e){
            log.error("Error parsing DateWithTimezone: " + ts, e);
        }
        if ( null == date ) return null;
        return date.getTime();
    }

    public static Long parseIso8601Date(String ts)
    {
        if ( null == ts || "".equals(ts) ) return null;
        Date date = null;
        try {
           date = ISO_8601_DATE_PARSER.parse(ts);
        } catch (Exception e){
            log.error("Error parsing Iso8601Date: " + ts, e);
        }
        if ( null == date ) return null;
        return date.getTime();
    }
}
