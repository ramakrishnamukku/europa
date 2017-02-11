package com.distelli.europa.db;

import com.distelli.europa.models.Monitor;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.distelli.utils.CompactUUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.lang.management.ManagementFactory;
import javax.persistence.RollbackException;

@Log4j
@Singleton
public class MonitorDb extends BaseDb
{
    @Inject
    private ScheduledExecutorService _executor;
    private Index<Monitor> _main;
    private final ObjectMapper _om = new ObjectMapper();

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName("monitors")
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("id", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(Monitor.class)
            .put("id", String.class, "id")
            .put("nam", String.class, "nodeName")
            .put("hb", Long.class, "heartbeat");
        return module;
    }

    @Inject
    public MonitorDb(Index.Factory indexFactory) {
        _om.registerModule(createTransforms(new TransformModule()));

        _main = indexFactory.create(Monitor.class)
            .withNoEncrypt("hb")
            .withTableDescription(getTableDescription())
            .withConvertValue(_om::convertValue)
            .build();
    }

    // Should only be ran once in the VM:
    public Monitor startMonitor() {
        Monitor monitor = createMonitor();
        _executor.scheduleAtFixedRate(
            () -> heartbeat(monitor),
            Monitor.HEARTBEAT_INTERVAL_MS,
            Monitor.HEARTBEAT_INTERVAL_MS,
            TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        String monitorId = monitor.getId();
                        monitor.forceAllMonitorsToHalt(null);
                        _main.deleteItem(monitorId, null);
                    } catch ( Throwable ex ) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            });
        return monitor;
    }

    public List<Monitor> listMonitors(PageIterator iter) {
        return _main.scanItems(iter);
    }

    private Monitor createMonitor() {
        Monitor monitor = Monitor.builder()
            .id(CompactUUID.randomUUID().toString())
            .nodeName(ManagementFactory.getRuntimeMXBean().getName())
            .heartbeat(1)
            .build();
        _main.putItem(monitor);
        return monitor;
    }

    private void heartbeat(Monitor monitor) {
        // We are already shutting down:
        String monitorId = monitor.getId();
        if ( null == monitorId ) return;
        try {
            monitor.incrementHeartbeat();
            _main.updateItem(monitor.getId(), null)
                .increment("hb", 1)
                .when((expr) -> expr.exists("id"));
            return;
        } catch ( Throwable ex ) {
            // Monitor was shutdown during DB update:
            if ( null == monitor.getId() ) return;

            if ( ex instanceof RollbackException ) {
                // This could happen if the computer is put to sleep:
                log.warn("Detected monitor deletion, forcing all tasks to stop (perhaps computer sleeped).");
            } else {
                log.error(ex.getMessage(), ex);
            }
        }
        try {
            monitor.forceAllMonitorsToHalt(createMonitor());
            _main.deleteItem(monitorId, null);
        } catch ( Throwable ex ) {
            log.error(ex.getMessage(), ex);
        }
    }
}
