package com.distelli.europa.db;

import lombok.extern.log4j.Log4j;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import com.distelli.europa.models.ExecutionStatus;
import com.distelli.europa.models.Pipeline;
import com.distelli.europa.models.PipelineComponent;
import com.distelli.europa.models.PCCopyToRepository;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.utils.CompactUUID;
import java.util.stream.Collectors;
import javax.persistence.RollbackException;
import javax.persistence.EntityNotFoundException;
import java.lang.reflect.InvocationTargetException;

@Log4j
@Singleton
public class PipelineDb extends BaseDb
{
    private static final String TABLE_NAME = "pipelines";

    // Force it to sort "first".
    private static final String CID_FOR_PIPELINE = "!";

    private static final ObjectMapper _om = new ObjectMapper();

    private static final List<Class<? extends PipelineComponent>> PIPELINE_COMPONENT_TYPES = Arrays.asList(
        // NOTE: Order matters! Always add new elements to the end!
        PCCopyToRepository.class);

    // The indexes contain pipeline items, since some things
    // in the index are pipeline objects and other things are
    // pipeline components:
    private static class PipelineItem {
        public Pipeline pipeline;
        public PipelineComponent component;
        public Long componentIndex; // Desired index.
        public Long componentIndexPriority; // Higher comes first.

        public PipelineItem(Pipeline pipeline) {
            this.pipeline = pipeline;
        }

        public PipelineItem(Pipeline pipeline, PipelineComponent component, long index, long indexPriority) {
            this.pipeline = pipeline;
            this.component = component;
            this.componentIndex = index;
            this.componentIndexPriority = indexPriority;
        }

        public PipelineItem(Long type) {
            pipeline = new Pipeline();
            if ( null != type && type >= 0 && type < PIPELINE_COMPONENT_TYPES.size() ) {
                try {
                    component = PIPELINE_COMPONENT_TYPES.get(type.intValue()).newInstance();
                } catch ( RuntimeException ex ) {
                    throw ex;
                } catch ( Exception ex ) {
                    throw new RuntimeException(ex);
                }
            }
        }

        public String getDomain() {
            return pipeline.getDomain().toLowerCase();
        }
        public void setDomain(String domain) {
            pipeline.setDomain(domain);
        }
        public String getId() {
            return pipeline.getId();
        }
        public void setId(String id) {
            pipeline.setId(id);
        }
        public Long getType() {
            long index = 0;
            for ( Class type : PIPELINE_COMPONENT_TYPES ) {
                if ( type.isInstance(component) ) return index;
                index++;
            }
            return null;
        }
        public void setType(Long type) {}
        public String getContainerRepoId() {
            // We don't want containers in this index:
            if ( null == component ) {
                return pipeline.getContainerRepoId();
            }
            return null;
        }
        public void setContainerRepoId(String crid) {
            pipeline.setContainerRepoId(crid);
        }
        public String getComponentId() {
            if ( null == component ) return CID_FOR_PIPELINE;
            return component.getId();
        }
        public void setComponentId(String cid) {
            if ( CID_FOR_PIPELINE.equals(cid) ) return;
            if ( null == component ) {
                // no type associated with it...
                component = new PipelineComponent();
            }
            component.setId(cid);
        }
        public <T> T getComponent(String methodName, Class<T> type) {
            if ( null == component ) return null;
            try {
                return (T)component.getClass().getMethod(methodName).invoke(component);
            } catch ( NoSuchMethodException|IllegalAccessException ex ) {
                return null;
            } catch ( InvocationTargetException ex ) {
                if ( ex.getCause() instanceof RuntimeException ) {
                    throw (RuntimeException)ex.getCause();
                }
                throw new RuntimeException(ex.getCause());
            }
        }
        public <T> void setComponent(String methodName, Class<T> type, T value) {
            if ( null == component ) return;
            try {
                component.getClass().getMethod(methodName, type).invoke(component, value);
            } catch ( NoSuchMethodException|IllegalAccessException ex ) {
            } catch ( InvocationTargetException ex ) {
                if ( ex.getCause() instanceof RuntimeException ) {
                    throw (RuntimeException)ex.getCause();
                }
                throw new RuntimeException(ex.getCause());
            }
        }
    }

    @Inject
    private SequenceDb _seq;
    private Index<PipelineItem> _main;
    private Index<PipelineItem> _byContainerRepoId;

    private static final TableDescription TABLE_DESCRIPTION = TableDescription.builder()
        .tableName(TABLE_NAME)
        .indexes(
            Arrays.asList(
                IndexDescription.builder()
                .hashKey(attr("id", AttrType.STR))
                .rangeKey(attr("cid", AttrType.STR))
                .indexType(IndexType.MAIN_INDEX)
                .readCapacity(1L)
                .writeCapacity(1L)
                .build(),
                IndexDescription.builder()
                .hashKey(attr("dom", AttrType.STR))
                .hashKey(attr("crid", AttrType.STR))
                .indexName("dom-crid-index")
                .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                .readCapacity(1L)
                .writeCapacity(1L)
                .build()))
        .build();

    public static TableDescription getTableDescription() {
        return TABLE_DESCRIPTION;
    }

    public TransformModule createTransforms(TransformModule module) {
        module.createTransform(PipelineItem.class)
            .constructor((tree, codec) ->
                         new PipelineItem(
                             codec.treeToValue(tree.get("ty"),
                                               Long.class)))
            // Pipeline fields:
            .put("dom", String.class, "domain")
            .put("id", String.class, "id")
            .put("crid", String.class, "containerRepoId")
            // Component "hidden" fields:
            .put("ty", Long.class, "type")
            .put("idx", Long.class, "componentIndex")
            .put("idxp", Long.class, "componentIndexPriority")
            // Component fields:
            .put("cid", String.class, "componentId")
            // PCCopyToRepository fields:
            .put("dcrdom", String.class,
                 (item) -> item.getComponent("getDestinationContainerRepoDomain", String.class),
                 (item, id) -> item.setComponent("setDestinationContainerRepoDomain", String.class, id))
            .put("dcrid", String.class,
                 (item) -> item.getComponent("getDestinationContainerRepoId", String.class),
                 (item, id) -> item.setComponent("setDestinationContainerRepoId", String.class, id))
            .put("tag", String.class,
                 (item) -> item.getComponent("getTag", String.class),
                 (item, tag) -> item.setComponent("setTag", String.class, tag))
            .put("exT", Long.class,
                 (item) -> item.getComponent("getLastExecutionTime", Long.class),
                 (item, time) -> item.setComponent("setLastExecutionTime", Long.class, time))
            .put("exS", ExecutionStatus.class,
                 (item) -> item.getComponent("getLastExecutionStatus", ExecutionStatus.class),
                 (item, status) -> item.setComponent("setLastExecutionStatus", ExecutionStatus.class, status));
            
        return module;
    }

    @Inject
    protected PipelineDb(Index.Factory indexFactory,
                         ConvertMarker.Factory convertMarkerFactory)
    {
        _om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(PipelineItem.class)
            .withTableDescription(TABLE_DESCRIPTION)
            .withConvertValue(_om::convertValue)
            .build();
    }

    // Populates the id fields.
    public void createPipeline(Pipeline pipeline) {
        pipeline.setId(CompactUUID.randomUUID().toString());
        if ( null != pipeline.getComponents() ) {
            long index = 0;
            for ( PipelineComponent component : pipeline.getComponents() ) {
                component.setId(CompactUUID.randomUUID().toString());
                _main.putItemOrThrow(new PipelineItem(pipeline, component, index++, 0));
            }
        }
        _main.putItemOrThrow(new PipelineItem(pipeline));
    }

    // NOTE: this does NOT include the PipelineComponents!
    public List<Pipeline> listByContainerRepoId(String domain, String containerRepoId, PageIterator iterator) {
        return _byContainerRepoId.queryItems(domain, iterator)
            .eq(containerRepoId)
            .list()
            .stream()
            .map( (item) -> item.pipeline )
            .collect(Collectors.toList());
    }

    // NOTE: this does NOT include the PipelineComponents!
    public List<Pipeline> listByDomain(String domain, PageIterator iterator) {
        return _byContainerRepoId.queryItems(domain, iterator)
            .list()
            .stream()
            .map( (item) -> item.pipeline )
            .collect(Collectors.toList());
    }

    public Pipeline getPipeline(String pipelineId) {
        Pipeline result = null;

        List<PipelineItem> items = new ArrayList<>();
        for ( PageIterator iter : new PageIterator() ) {
            for ( PipelineItem item : _main.queryItems(pipelineId, iter).list() ) {
                if ( null == item.component ) {
                    if ( null == result ) result = item.pipeline;
                } else {
                    items.add(item);
                }
            }
        }
        if ( null == result ) {
            if ( 0 == items.size() ) return null;
            result = new Pipeline();
            result.setId(pipelineId);
        }
        result.setComponents(sortComponents(items));
        return result;
    }

    // afterPipelineComponentId is the id of the pipeline component this is added after.
    // if null, this is added at the "beginning" of the list of pipeline components.
    public void addPipelineComponent(Pipeline pipeline, PipelineComponent component, int toIndex) {
        component.setId(CompactUUID.randomUUID().toString());
        _main.putItemOrThrow(new PipelineItem(pipeline, component, toIndex, _seq.nextPipelineComponentPriority()));
    }

    public void movePipelineComponent(String pipelineId, String componentId, int toIndex) {
        try {
            _main.updateItem(pipelineId, componentId)
                .set("idx", toIndex)
                .set("idxp", _seq.nextPipelineComponentPriority())
                .when((expr) -> expr.exists("id"));
        } catch ( RollbackException ex ) {
            throw new EntityNotFoundException("Invalid pipelineId="+pipelineId+" componentId="+componentId);
        }
    }

    public void removePipelineComponent(String pipelineId, String pipelineComponentId) {
        _main.deleteItem(pipelineId, pipelineComponentId);
    }

    public void removePipeline(String pipelineId) {
        for ( PageIterator it : new PageIterator().backward() ) {
            for ( Map item : _main.queryItems(pipelineId, it).list(Arrays.asList("cid"), Map.class) ) {
                String componentId = (String)item.get("cid");
                _main.deleteItem(pipelineId, componentId);
            }
        }
    }

    private List<PipelineComponent> sortComponents(List<PipelineItem> items) {
        // TODO: Find a better algorithm...
        Collections.sort(items, (a, b) -> {
                int result = 0;
                // [a] Higher component index priorities are first.
                if ( null != a.componentIndexPriority && null != b.componentIndexPriority ) {
                    result = b.componentIndexPriority.compareTo(a.componentIndexPriority);
                    if ( 0 != result ) return result;
                }
                // [b] Lower index priorities are first.
                if ( null != a.componentIndex && null != b.componentIndex ) {
                    result = a.componentIndex.compareTo(b.componentIndex);
                    if ( 0 != result ) return result;
                }
                // [c] Sort by id:
                return a.component.getId().compareTo(b.component.getId());
            });

        PipelineComponent[] result = new PipelineComponent[items.size()];
        for ( PipelineItem item : items ) {
            int index = ( null == item.componentIndex ) ? 0 : item.componentIndex.intValue();
            for ( int i=0; i < result.length; i++ ) {
                // Find the closest index.
                if ( tryIndex(result, index-i, item.component) ) break;
                if ( tryIndex(result, index+i, item.component) ) break;
            }
        }
        return Arrays.asList(result);
    }

    private boolean tryIndex(PipelineComponent[] result, int idx, PipelineComponent comp) {
        if ( idx < 0 ) return false;
        if ( idx >= result.length ) return false;
        if ( null != result[idx] ) return false;
        result[idx] = comp;
        return true;
    }
}
