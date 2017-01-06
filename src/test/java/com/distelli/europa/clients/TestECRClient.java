package com.distelli.europa.clients;

import java.util.*;

import com.distelli.europa.*;
import com.distelli.europa.models.*;
import com.google.inject.Stage;
import com.distelli.utils.Log4JConfigurator;
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
import com.distelli.persistence.PageIterator;
import static org.hamcrest.CoreMatchers.*;

import javax.inject.Inject;
import com.distelli.europa.clients.*;
import com.google.inject.Guice;
import static org.junit.Assert.*;

public class TestECRClient
{
    @Rule
    public TestName name = new TestName();

    @Inject
    private EuropaTestConfig _testConfig;

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
        Guice.createInjector(Stage.PRODUCTION,
                             new EuropaTestModule())
        .injectMembers(this);
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
    public void testListRepositoriesAndImages()
    {
        String credId = UUID.randomUUID().toString();

        RegistryCred cred =  RegistryCred
        .builder()
        .key(_testConfig.getAwsAccessKey())
        .secret(_testConfig.getAwsSecretKey())
        .region(_testConfig.getAwsRegion())
        .id(credId)
        .build();
        ECRClient ecrClient = new ECRClient(cred);
        PageIterator pageIterator = new PageIterator().pageSize(100).marker(null);
        List<ContainerRepo> repos = ecrClient.listRepositories(pageIterator);
        assertThat(repos, is(not(nullValue())));
        for(ContainerRepo repo : repos)
        {
            System.out.println("Repo: "+repo);
            assertThat(repo.getName(), is(not(nullValue())));
            assertThat(repo.getRegion(), is(not(nullValue())));
            assertThat(repo.getProvider(), is(not(nullValue())));
            assertThat(repo.getRegistryId(), is(not(nullValue())));
            assertThat(repo.getCredId(), is(not(nullValue())));
            assertThat(repo.getCredId(), equalTo(credId));
            assertThat(repo.getRepoUri(), is(not(nullValue())));

            PageIterator imageIter = new PageIterator().pageSize(5).marker(null);
            List<DockerImageId> images = ecrClient.listImages(repo, imageIter);
            assertThat(images, is(not(nullValue())));
            for(DockerImageId imageId : images)
            {
                System.out.println("DockerImage: "+imageId);
                assertThat(imageId.getRepoUri(), is(not(nullValue())));
                assertThat(imageId.getRepoUri(), equalTo(repo.getRepoUri()));
                assertThat(imageId.getSha(), is(not(nullValue())));
            }
        }
    }
}
