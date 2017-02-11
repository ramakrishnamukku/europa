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
    public static long HEARTBEAT_INTERVAL_MS = 30000;
    private static long NANOS_TO_MS = 1000000;

    private String id; // Compact UUID.
    private String nodeName; // ManagementFactory.getRuntimeMXBean().getName()
    private long heartbeat; // incremented HEARTBEAT_INTERVAL_MS

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<Thread, AtomicInteger> running = new HashMap<>();

    public static class MonitorBuilder {
        private Map<Thread, AtomicInteger> running = new HashMap<>();
    }

    // All monitored tasks must use this construct:
    //  try ( Object _ = monitor.monitor() ) {
    //      ...critical section.
    //  }
    //
    // The critical section may be interrupted if the monitor is ever
    // deleted.
    public AutoCloseable monitor() {
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
        AtomicInteger finalDepth = depth;
        return () -> {
            synchronized ( running ) {
                if ( 0 == finalDepth.decrementAndGet() ) {
                    running.remove(cur);
                    running.notifyAll();
                }
            }
        };
    }

    public synchronized void incrementHeartbeat() {
        heartbeat++;
    }

    public void forceAllMonitorsToHalt(Monitor newMonitor) {
        Map<Thread, AtomicInteger> old = running;
        synchronized ( old ) {
            if ( running == old ) {
                synchronized ( this ) {
                    if ( null == newMonitor ) {
                        id = null;
                        nodeName = null;
                        heartbeat = 0;
                    } else {
                        id = newMonitor.id;
                        nodeName = newMonitor.nodeName;
                        heartbeat = newMonitor.heartbeat;
                    }
                    running = new HashMap<>();
                }
            }
            // Interrupt all threads:
            for ( Thread thread : old.keySet() ) {
                thread.interrupt();
            }

            // Wait for them to reap:
            long endTime = System.nanoTime() + NANOS_TO_MS*(HEARTBEAT_INTERVAL_MS/2);

            while ( old.size() > 0 ) {
                try {
                    long waitTime = (endTime - System.nanoTime()) / NANOS_TO_MS;
                    if ( waitTime <= 0 ) {
                        // Serious problem can only be solved by halting the VM!
                        String msg = "Monitor.forceAllMonitorsToHalt() failed to halt all threads!";
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
