package com.distelli.europa.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawTaskEntry {
    // Unique id of individual task (RK end):
    private long taskId;

    // What type of entity does this task belong to (HK)?
    private String entityType;
    // What unique id of this entity does it belong to (RK start)?
    private String entityId;
 
    // What lock ids does this task need to acquire?
    private Set<String> lockIds;
    // The tasks monitor that is currently running this task or
    // null if this has ran (endTime is non-null) or this has
    // not ran yet.
    private String monitorId;

    // Information about this task.
    private byte[] privateTaskState;
    // If it fails, here is the error message.
    private String errorMessage;
    // When it starts, this is set:
    private Long startTime; // System.currentTimeMillis()
    // When it ends, this is set:
    private Long endTime;
}
