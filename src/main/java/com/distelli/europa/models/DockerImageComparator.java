/*
  $Id: $
  @file DockerImageComparator.java
  @brief Contains the DockerImageComparator.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.models;

import java.util.Comparator;

import org.apache.log4j.Logger;

public class DockerImageComparator implements Comparator<DockerImage>
{
    private static final Logger log = Logger.getLogger(DockerImageComparator.class);

    public DockerImageComparator()
    {

    }

    public int compare(DockerImage i1, DockerImage i2) {
        long i1PushTime = getPushTime(i1);
        long i2PushTime = getPushTime(i2);
        if(i1PushTime > i2PushTime)
            return 1;
        if(i1PushTime < i2PushTime)
            return -1;
        return 0;
    }

    private long getPushTime(DockerImage dockerImage)
    {
        Long pushTime = dockerImage.getPushTime();
        if(pushTime == null)
            return 0;
        return pushTime.longValue();
    }

}
