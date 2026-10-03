package com.improvedchat.overlay;

/** Content filtering mode for an individual Improved Chat overlay. */
public enum OverlayFilterMode
{
    OFF("Off"),
    HIDE_MATCHES("Hide Matches"),
    SHOW_ONLY_MATCHES("Show Only Matches");

    private final String label;

    OverlayFilterMode(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
