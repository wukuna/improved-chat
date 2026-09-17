package com.improvedchat.model;

public enum FontSize {
    REGULAR("Regular", 1.0f),
    SMALL("Small", 0.75f),
    LARGE("Large", 1.25f),
    EXTRA_LARGE("Extra Large", 1.50f);

    private final String name;
    private final float scale;

    FontSize(String name, float scale) {
        this.name = name;
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public String toString() {
        return name;
    }
}
