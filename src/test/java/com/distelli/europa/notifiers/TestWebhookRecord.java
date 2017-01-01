/*
  $Id: $
  @file TestWebhookRecord.java
  @brief Contains the TestWebhookRecord.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.notifiers;

import java.util.Arrays;

import com.distelli.europa.util.Log4JConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import static org.hamcrest.CoreMatchers.*;

import com.distelli.europa.models.*;

import javax.inject.Inject;
import com.google.inject.Guice;
import static org.junit.Assert.*;

public class TestWebhookRecord
{
    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void beforeClass()
    {
        Log4JConfigurator.configure(true);
        Log4JConfigurator.setLogLevel("httpclient.wire", "ERROR");
        Log4JConfigurator.setLogLevel("org.jets3t", "ERROR");
        Log4JConfigurator.setLogLevel("org.apache", "ERROR");
        Log4JConfigurator.setLogLevel("httpclient.wire.content", "ERROR");
        Log4JConfigurator.setLogLevel("com.distelli.auth", "ERROR");
        Log4JConfigurator.setLogLevel("com.distelli.guice", "ERROR");
        Log4JConfigurator.setLogLevel("com.distelli.request", "ERROR");
        Log4JConfigurator.setLogLevel("com.amazonaws.services", "ERROR");
        Log4JConfigurator.setLogLevel("com.amazonaws.auth", "ERROR");
        Log4JConfigurator.setLogLevel("com.amazonaws.http", "ERROR");
        Log4JConfigurator.setLogLevel("com.amazonaws.internal", "ERROR");
        Log4JConfigurator.setLogLevel("com.amazonaws.request", "ERROR");
        Log4JConfigurator.setLogLevel("com.amazonaws.requestId", "ERROR");
        Log4JConfigurator.setLogLevel("com.distelli.endpoint", "ERROR");
        Log4JConfigurator.setLogLevel("org.eclipse", "ERROR");
    }

    @Before
    public void beforeTest()
    {
        String testName = name.getMethodName();
        String prefix = "Running Test: ";
        char[] line = new char[prefix.length()+testName.length()];
        Arrays.fill(line, '=');
        System.out.println();
        System.out.println(line);
        System.out.println(prefix+name.getMethodName());
        System.out.println(line);
    }

    @AfterClass
    public static void afterClass()
    {

    }

    @After
    public void afterTest()
    {

    }

    @Test
    public void testWebhookRecord()
        throws Exception
    {
        WebhookRequest request = WebhookRequest
        .builder()
        .body("{\"hello\":\"world\"}")
        .header("a", "b")
        .header("b", "c")
        .build();

        WebhookResponse response = WebhookResponse
        .builder()
        .httpStatusCode(200)
        .body("{\"some\":\"response\"}")
        .header("1", "2")
        .header("3", "4")
        .build();

        WebhookRecord record = new WebhookRecord();
        record.setRequest(request);
        record.setResponse(response);

        byte[] bytes = record.toJsonBytes();
        WebhookRecord record2 = WebhookRecord.fromJsonBytes(bytes);
        assertThat(record2, equalTo(record));
    }
}
