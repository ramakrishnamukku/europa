package com.distelli.europa.models;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Monitor {
    public static long HEARTBEAT_INTERVAL_MS = 10000;
    public static long REAP_INTERVAL = 6;
    private static long NANOS_TO_MS = 1000000;

    private String id; // Compact UUID.
    private String nodeName; // ManagementFactory.getRuntimeMXBean().getName()
    private long heartbeat; // incremented HEARTBEAT_INTERVAL_MS
    private boolean failHeartbeat = false;

    public synchronized void setFailHeartbeat(String monitorId) {
        if ( null == monitorId ) return;
        if ( monitorId.equals(this.id) ) {
            failHeartbeat = true;
        }
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<Thread, AtomicInteger> running = new HashMap<>();
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long lastHeartbeatNanos = System.nanoTime();

    public static class MonitorBuilder {
        private Map<Thread, AtomicInteger> running = new HashMap<>();
        private long lastHeartbeatNanos = System.nanoTime();
        private boolean failHeartbeat = false;
    }

    public class Lock implements AutoCloseable {
        private String id;
        private AtomicInteger depth;
        private Thread cur;
        private Lock(String id, AtomicInteger depth, Thread cur) {
            this.id = id;
            this.depth = depth;
            this.cur = cur;
        }
        public String getId() {
            return this.id;
        }
        public void close() throws Exception {
            if ( null == depth ) return;
            synchronized ( running ) {
                if ( 0 == depth.decrementAndGet() ) {
                    running.remove(cur);
                    running.notifyAll();
                }
            }
            depth = null;
        }
    }

    // All monitored tasks must use this construct:
    //  try ( Object _ = monitor.monitor() ) {
    //      ...critical section.
    //  }
    //
    // The critical section may be interrupted if the monitor is ever
    // deleted.
    public Lock monitor() {
        String id = this.id;
        synchronized ( this ) {
            if ( null == id ) {
                throw new IllegalStateException("Attempt to monitor during monitor shutdown.");
            }
        }
        Thread cur = Thread.currentThread();
        AtomicInteger depth;
        synchronized ( running ) {
            depth = running.get(cur);
            if ( null == depth ) {
                depth = new AtomicInteger(1);
                running.put(cur, depth);
            } else {
                depth.getAndIncrement();
            }
        }
        return new Lock(id, depth, cur);
    }

    public synchronized void incrementHeartbeat() {
        heartbeat++;
        lastHeartbeatNanos = System.nanoTime();
    }

    public void forceAllMonitorsToHalt(Monitor newMonitor) {
        Map<Thread, AtomicInteger> old = running;
        synchronized ( old ) {
            long t0;
            synchronized ( this ) {
                t0 = lastHeartbeatNanos;
                if ( running == old ) {
                    if ( null == newMonitor ) {
                        id = null;
                        nodeName = null;
                        heartbeat = 0;
                    } else {
                        id = newMonitor.id;
                        nodeName = newMonitor.nodeName;
                        heartbeat = newMonitor.heartbeat;
                        failHeartbeat = false;
                    }
                    running = new HashMap<>();
                }
            }
            // Interrupt all threads:
            for ( Thread thread : old.keySet() ) {
                thread.interrupt();
            }

            // Wait for them to reap, wait at most the reap interval:
            long intervalMs = HEARTBEAT_INTERVAL_MS*REAP_INTERVAL;
            long endTime = t0 + NANOS_TO_MS*intervalMs;

            while ( true ) {
                try {
                    long waitTime = (endTime - System.nanoTime()) / NANOS_TO_MS;
                    // Must do size check after nanoTime() acquired:
                    if ( old.size() <= 0 ) break;
                    if ( waitTime <= 0 ) {
                        String msg = "Failed to halt monitor threads after "+
                            (intervalMs/1000.0)+" seconds since last heartbeat."+
                            " Calling System.exit(-1) to avoid duplicate tasks from running.";
                        log.fatal(msg);
                        System.err.println(msg);
                        System.exit(-1);
                    }
                    old.wait(waitTime);
                } catch ( InterruptedException ex ) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
