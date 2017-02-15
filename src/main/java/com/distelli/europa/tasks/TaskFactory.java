package com.distelli.europa.tasks;

import com.distelli.europa.models.RawTaskEntry;

public interface TaskFactory {//<T extends Task> {
//    public T toTask(RawTaskEntry entry);
    public Runnable toRunnable(RawTaskEntry entry);
}
