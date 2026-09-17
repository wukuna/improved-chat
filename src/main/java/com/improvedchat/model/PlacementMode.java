package com.improvedchat.model;

public enum PlacementMode {
    FREE("Free / Drag Anywhere"),
    BELOW_PLAYER("Below Player"),
    ABOVE_PLAYER("Above Player");

    private final String label;

    PlacementMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
