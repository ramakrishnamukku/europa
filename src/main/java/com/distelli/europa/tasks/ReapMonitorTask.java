package com.distelli.europa.tasks;

import com.distelli.europa.db.TasksDb;
import com.distelli.europa.models.RawTaskEntry;
import com.distelli.europa.models.Monitor;
import com.google.inject.Injector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;

@Log4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReapMonitorTask implements Task {
    private String monitorId;

    public static final String ENTITY_TYPE = "gc:monitor";

    @Override
    public RawTaskEntry toRawTaskEntry() {
        return RawTaskEntry.builder()
            .entityType(ENTITY_TYPE)
            .entityId(monitorId)
            .build();
    }

    public class Run implements Runnable {
        @Inject
        private TasksDb _tasksDb;

        @Inject
        private Monitor _monitor;

        @Override
        public void run() {
            _tasksDb.releaseLocksForMonitorId(monitorId);
        }
    }

    public static class Factory implements TaskFactory {
        @Inject
        private Injector _injector;
        public ReapMonitorTask toTask(RawTaskEntry entry) {
            return ReapMonitorTask.builder()
                .monitorId(entry.getEntityId())
                .build();
        }
        @Override
        public Runnable toRunnable(RawTaskEntry entry) {
            Run run = toTask(entry).new Run();
            _injector.injectMembers(run);
            return run;
        }
    }
}
