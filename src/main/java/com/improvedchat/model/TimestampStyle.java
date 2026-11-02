package com.improvedchat.model;

/** How an overlay labels a message's arrival time. */
public enum TimestampStyle {
    CLOCK_TIME("Clock time"),
    TIME_AGO("Time ago (20m)");

    private final String label;
    TimestampStyle(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
