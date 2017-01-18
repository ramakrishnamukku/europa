package com.distelli.europa.models;


public enum ExecutionStatus
{
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILURE;
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILURE;
    }
}
