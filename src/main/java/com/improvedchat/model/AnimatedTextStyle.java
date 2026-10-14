package com.improvedchat.model;

public enum AnimatedTextStyle {
    NORMAL("Normal"), RGB_CYCLE("RGB Cycle (Beta)"), RGB_WAVE("RGB Wave (Beta)");

    private final String label;

    AnimatedTextStyle(String label) { this.label = label; }

    @Override public String toString() { return label; }
}
