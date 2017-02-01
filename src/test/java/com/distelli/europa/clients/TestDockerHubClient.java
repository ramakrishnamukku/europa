package com.distelli.europa.clients;

import org.junit.Test;
import org.junit.Before;
import javax.inject.Inject;
import com.google.inject.Guice;
import com.distelli.europa.EuropaTestModule;
import com.distelli.europa.EuropaTestConfig;
import com.distelli.persistence.PageIterator;
import com.distelli.europa.models.DockerHubRepository;

public class TestDockerHubClient {
    @Inject
    private EuropaTestConfig testConfig;

    @Before
    public void before() {
        Guice.createInjector(new EuropaTestModule())
            .injectMembers(this);
    }
    @Test
    public void testListRepositories() throws Exception {
        DockerHubClient client = DockerHubClient.builder()
            .credentials(
                testConfig.getDockerHubUsername(),
                testConfig.getDockerHubPassword())
            .build();

        int count = 0;
        System.out.println("Repositories:");
        for ( PageIterator iter : new PageIterator().pageSize(3) ) {
            for ( DockerHubRepository repo : client.listRepositories(testConfig.getDockerHubUsername(), iter) ) {
                System.out.println("\t- "+repo);
            }
            if ( ++count >= 3 ) break;
        }
    }
}
