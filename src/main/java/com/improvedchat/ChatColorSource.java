package com.improvedchat;

/** Selects which RuneLite Chat Color palette supplies an overlay's baseline colors. */
public enum ChatColorSource {
    AUTOMATIC("RuneLite (automatic)"),
    TRANSPARENT("RuneLite transparent"),
    OPAQUE("RuneLite opaque"),
    WIDGET("Overlay palette");

    private final String label;

    ChatColorSource(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
