package com.improvedchat.model;

public enum AttentionStopMode {
    TIMER_OR_REFOCUS("Timer or Refocus"),
    TIMER_ONLY("Timer Only"),
    REFOCUS_ONLY("Refocus Only");

    private final String label;
    AttentionStopMode(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
