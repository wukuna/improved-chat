package com.improvedchat.model;

/** Horizontal alignment for text rendered inside an Improved Chat overlay. */
public enum TextAlignment {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right");

    private final String label;

    TextAlignment(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
