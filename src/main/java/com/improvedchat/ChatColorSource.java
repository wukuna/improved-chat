package com.improvedchat;

/** Selects which RuneLite Chat Color palette supplies an overlay's baseline colors. */
public enum ChatColorSource {
    AUTOMATIC("Automatic"),
    TRANSPARENT("Transparent"),
    OPAQUE("Opaque"),
    WIDGET("Improved Chat fallback");

    private final String label;

    ChatColorSource(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
