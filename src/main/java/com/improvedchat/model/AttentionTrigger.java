package com.improvedchat.model;

public enum AttentionTrigger {
    ALL_NEW_MESSAGES("All New Messages"),
    FLASH_RULES_ONLY("::flash Rules Only");

    private final String label;
    AttentionTrigger(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
