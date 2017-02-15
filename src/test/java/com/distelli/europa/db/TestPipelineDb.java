package com.distelli.europa.db;

import com.distelli.europa.EuropaConfiguration;
import com.distelli.europa.models.Pipeline;
import com.distelli.europa.models.ExecutionStatus;
import com.distelli.europa.models.PipelineComponent;
import java.io.File;
import java.util.Arrays;
import javax.inject.Inject;
import com.distelli.europa.guice.EuropaInjectorModule;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.impl.PersistenceModule;
import com.distelli.objectStore.impl.ObjectStoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.UUID;
import java.util.List;
import com.distelli.europa.models.PCCopyToRepository;
import com.distelli.utils.Log4JConfigurator;
import static org.junit.Assert.*;

public class TestPipelineDb {
    private static Injector INJECTOR = createInjector();

    private static Injector createInjector() {
        String path = System.getenv("EUROPA_CONFIG");
        EuropaConfiguration config = null;
        if(path != null) {
            File file = new File(path);
            if(!file.exists())
                throw(new IllegalStateException("Invalid value for EUROPA_CONFIG env var: "+path));
            config = EuropaConfiguration.fromFile(file);
        } else {
            config = EuropaConfiguration.fromEnvironment();
        }
        return Guice.createInjector(
            new PersistenceModule(),
            new ObjectStoreModule(),
            new EuropaInjectorModule(
                config));
    }

    @Inject
    private PipelineDb pipelineDb;

    @Before
    public void before() throws Exception {
        if ( null == INJECTOR ) {
            throw new RuntimeException("EUROPA_CONFIG environment variable must point to valid file");
        }
        INJECTOR.injectMembers(this);
    }

    @BeforeClass
    public static void beforeClass()
    {
        Log4JConfigurator.configure(true);
        Log4JConfigurator.setLogLevel("com.zaxxer.hikari", "INFO");
    }

    @Test
    public void test() throws Exception {
        String domain = UUID.randomUUID().toString();
        Pipeline pipeline = Pipeline.builder()
            .name("super-pipes")
            .domain(domain)
            .components(
                Arrays.asList(
                    PCCopyToRepository.builder()
                    .destinationContainerRepoDomain(domain)
                    .destinationContainerRepoId("A")
                    .tag("tag")
                    .lastExecutionTime(1000L)
                    .lastExecutionStatus(ExecutionStatus.QUEUED)
                    .build(),
                    PCCopyToRepository.builder()
                    .destinationContainerRepoId("B")
                    .build(),
                    PCCopyToRepository.builder()
                    .destinationContainerRepoId("C")
                    .build(),
                    PCCopyToRepository.builder()
                    .destinationContainerRepoId("D")
                    .build(),
                    PCCopyToRepository.builder()
                    .destinationContainerRepoId("E")
                    .build()))
            .build();
        try {
            pipelineDb.createPipeline(pipeline);

            pipelineDb.setContainerRepo(pipeline.getId(), domain, "Y");

            List<Pipeline> list = pipelineDb.listByContainerRepoId(domain, "Y", new PageIterator());
            assertEquals(list.size(), 1);
            assertEquals(list.get(0).getId(), pipeline.getId());

            Pipeline got = pipelineDb.getPipeline(pipeline.getId());
            assertEquals(got.getName(), "super-pipes");
            assertEquals(got.getDomain(), domain);
            assertEquals(got.getContainerRepoId(), "Y");
            assertEqualComponents(got.getComponents(), pipeline.getComponents());

            PipelineComponent pcA = pipeline.getComponents().get(0);
            PipelineComponent pcB = pipeline.getComponents().get(1);
            PipelineComponent pcC = pipeline.getComponents().get(2);
            PipelineComponent pcD = pipeline.getComponents().get(3);
            PipelineComponent pcE = pipeline.getComponents().get(4);

            // A 0 B C D E:
            PipelineComponent pc0 = PCCopyToRepository.builder()
                .destinationContainerRepoId("0")
                .build();
            pipelineDb.addPipelineComponent(pipeline.getId(), pc0, pcB.getId());

            // A E 0 B C D:
            pipelineDb.movePipelineComponent(pipeline.getId(),
                                             pcE.getId(),
                                             pc0.getId());

            assertEquals(getDestinationContainerRepoIds(pipelineDb.getPipeline(pipeline.getId()).getComponents()),
                         new String[]{"A", "E", "0", "B", "C", "D"});
            // E 0 B C D:
            pipelineDb.removePipelineComponent(pipeline.getId(), pcA.getId());
            assertEquals(getDestinationContainerRepoIds(pipelineDb.getPipeline(pipeline.getId()).getComponents()),
                         new String[]{"E", "0", "B", "C", "D"});
        } finally {
            if ( null != pipeline.getId() ) {
                //pipelineDb.removePipeline(pipeline.getId());
            }
        }
    }

    private String[] getDestinationContainerRepoIds(List<PipelineComponent> components) {
        String[] result = new String[components.size()];
        int idx = -1;
        for ( PipelineComponent comp : components ) {
            idx++;
            if ( ! (comp instanceof PCCopyToRepository) ) continue;
            PCCopyToRepository pc = (PCCopyToRepository)comp;
            result[idx] = pc.getDestinationContainerRepoId();
        }
        return result;
    }

    private void assertEqualComponents(List<PipelineComponent> a, List<PipelineComponent> b) {
        assertEquals(a.size(), b.size());
        for ( int i=0; i < a.size(); i++ ) {
            assertEqualComponents(a.get(i), b.get(i));
        }
    }
    private void assertEqualComponents(PipelineComponent a, PipelineComponent b) {
        assertEquals(a.getId(), b.getId());
        assertEquals(a.getClass(), b.getClass());
        if ( a instanceof PCCopyToRepository ) {
            assertEqualComponents((PCCopyToRepository)a, (PCCopyToRepository)b);
        } else {
            throw new UnsupportedOperationException("Class="+a.getClass()+" not implemented");
        }
    }
    private void assertEqualComponents(PCCopyToRepository a, PCCopyToRepository b) {
        assertEquals(a.getDestinationContainerRepoDomain(), b.getDestinationContainerRepoDomain());
        assertEquals(a.getDestinationContainerRepoId(), b.getDestinationContainerRepoId());
        assertEquals(a.getTag(), b.getTag());
        assertEquals(a.getLastExecutionTime(), b.getLastExecutionTime());
        assertEquals(a.getLastExecutionStatus(), b.getLastExecutionStatus());
    }
}
