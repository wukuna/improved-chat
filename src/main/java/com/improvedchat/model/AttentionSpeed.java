package com.improvedchat.model;

public enum AttentionSpeed {
    SLOW("Slow", 1200L),
    NORMAL("Normal", 700L),
    FAST("Fast", 360L);

    private final String label;
    private final long periodMs;
    AttentionSpeed(String label, long periodMs) { this.label = label; this.periodMs = periodMs; }
    public long getPeriodMs() { return periodMs; }
    @Override public String toString() { return label; }
}
