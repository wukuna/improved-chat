package com.improvedchat.overlay;

import java.util.List;

public final class RenderLine {
    public final List<TextSegment> segments;
    public final int alpha;

    public RenderLine(List<TextSegment> segments, int alpha) {
        this.segments = segments;
        this.alpha = alpha;
    }
}
