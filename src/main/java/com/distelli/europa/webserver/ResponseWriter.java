/*
  $Id: $
  @file ResponseWriter.java
  @brief Contains the ResponseWriter.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.webserver;

import java.io.IOException;
import java.io.OutputStream;

public interface ResponseWriter
{
    public void writeResponse(OutputStream out)
        throws IOException;
}
