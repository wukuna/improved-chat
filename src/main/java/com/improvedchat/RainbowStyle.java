package com.improvedchat;

public enum RainbowStyle {
    PER_WORD("Per Word"),
    PER_LETTER("Per Letter");

    private final String name;

    RainbowStyle(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
