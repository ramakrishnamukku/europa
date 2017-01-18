package com.distelli.europa.db;

import lombok.extern.log4j.Log4j;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.TreeSet;
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
import com.fasterxml.jackson.databind.DeserializationFeature;

@Log4j
@Singleton
public class PipelineDb extends BaseDb
{
    private static final long SPACING = 10;

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

        public PipelineItem(Pipeline pipeline) {
            this.pipeline = pipeline;
        }

        public PipelineItem(Pipeline pipeline, PipelineComponent component, long index) {
            this.pipeline = pipeline;
            this.component = component;
            this.componentIndex = index;
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
            // We don't want containers in this index:
            if ( null != component ) return null;
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
            if ( null != component ) return null;
            return pipeline.getContainerRepoId();
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
        _om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
            long idx = SPACING;
            for ( PipelineComponent component : pipeline.getComponents() ) {
                component.setId(CompactUUID.randomUUID().toString());
                _main.putItemOrThrow(new PipelineItem(pipeline, component, idx));
                idx += SPACING;
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
            .map((item) -> item.pipeline)
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

    // Add new pipeline component so it is before the specified componentId. Set to null
    // to make this component come first.
    public void addPipelineComponent(String pipelineId, PipelineComponent component, String beforeComponentId) {
        if ( null == pipelineId ) throw new NullPointerException("pipelineId must not be null");
        Pipeline pipeline = new Pipeline();
        pipeline.setId(pipelineId);
        long idx = allocateIdxBefore(pipelineId, beforeComponentId);
        component.setId(CompactUUID.randomUUID().toString());
        _main.putItemOrThrow(new PipelineItem(pipeline, component, idx));
    }

    public void movePipelineComponent(String pipelineId, String componentId, String beforeComponentId) {
        long idx = allocateIdxBefore(pipelineId, beforeComponentId);
        try {
            _main.updateItem(pipelineId, componentId)
                .set("idx", idx)
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

    protected static class PKIdx implements Comparable<PKIdx> {
        public String id;
        public String cid;
        public Long idx;
        @Override
        public int compareTo(PKIdx other) {
            // Sort low indexes first:
            int result = idx.compareTo(other.idx);
            if ( 0 != result ) return result;
            return cid.compareTo(other.cid);
        }
        @Override
        public String toString() {
            return "{cid="+cid+",idx="+idx+"}";
        }
    }
    private long allocateIdxBefore(String pipelineId, String beforeComponentId) {
        // [1] Collect the set of all components, finding beforeComponent:
        TreeSet<PKIdx> components = new TreeSet<>();
        PKIdx beforeComponent = null;
        for ( PageIterator it : new PageIterator() ) {
            for ( PKIdx item : _main.queryItems(pipelineId, it)
                      .list(Arrays.asList("id", "cid", "idx"), PKIdx.class) )
            {
                if ( null == item.idx || null == item.id || null == item.cid ) {
                    continue;
                }
                components.add(item);
                if ( item.cid.equals(beforeComponentId) ) {
                    beforeComponent = item;
                }
            }
        }
        // [2] Make sure beforeComponentId was found:
        if ( null != beforeComponentId && null == beforeComponent ) {
            throw new EntityNotFoundException(
                "Invalid pipelineId="+pipelineId+" beforeComponentId="+beforeComponentId);
        }
        // [3] No components, assign "first index":
        if ( 0 == components.size() ) return SPACING;

        // [4] Find a space:
        PKIdx left = ( null == beforeComponent )
            ? components.last()
            : components.lower(beforeComponent);
        PKIdx right = beforeComponent;

        // [5] Space already exists:
        if ( doesSpaceExist(left, right) ) {
            if ( null == left ) {
                return right.idx - 1;
            }
            return left.idx + 1;
        }

        PKIdx high = right;
        PKIdx low = left;

        long increment = 0;
        while ( high != null || low != null ) {
            if ( null != low ) {
                PKIdx next = components.lower(low);
                if ( doesSpaceExist(next, low) ) {
                    high = left;
                    increment = -1L;
                    break;
                }
                low = next;
            }
            if ( null != high ) {
                PKIdx next = components.higher(high);
                if ( doesSpaceExist(high, next) ) {
                    // Found a space:
                    low = right;
                    increment = 1L;
                    break;
                }
                high = next;
            }
        }
        if ( 0 == increment || null == high || null == low ) {
            throw new IllegalStateException("unreachable");
        }
        if ( increment < 0 ) {
            // [5.a] Shift down:
            while ( true ) {
                incrementIdx(low, increment);
                if ( low.compareTo(high) >= 0 ) break;
                low = components.higher(low);
            }
        } else {
            // [5.b] Shift up:
            while ( true ) {
                incrementIdx(high, increment);
                if ( low.compareTo(high) >= 0 ) break;
                high = components.lower(high);
            }
        }
        return high.idx;
    }
    private boolean doesSpaceExist(PKIdx low, PKIdx high) {
        if ( null == low ) {
            return high.idx > 0;
        }
        if ( null == high ) {
            return low.idx < Long.MAX_VALUE - 100;
        }
        return high.idx - low.idx > 2;
    }
    private void incrementIdx(PKIdx item, long amount) {
        try {
            _main.updateItem(item.id, item.cid)
                .increment("idx", amount)
                .when((expr) -> expr.exists("idx"));
        } catch ( RollbackException ex ){}
    }

    private List<PipelineComponent> sortComponents(List<PipelineItem> items) {
        Collections.sort(items, (a, b) -> {
                int result = 0;
                // [a] Sort by index:
                if ( null != a.componentIndex && null != b.componentIndex ) {
                    result = a.componentIndex.compareTo(b.componentIndex);
                    if ( 0 != result ) return result;
                }
                // [b] Sort by id:
                return a.component.getId().compareTo(b.component.getId());
            });
        return items.stream()
            .map((item) -> item.component)
            .collect(Collectors.toList());
    }
}
