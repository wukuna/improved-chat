package com.improvedchat.overlay;

import com.improvedchat.model.FontSize;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public final class FontStyler {
    private FontStyler() {}

    public static FontMetrics setupGraphics(Graphics2D graphics, FontSize fontSize, OverlayConfig config) {
        AttentionEngine.beginOverlay(config);
        ChatRenderUtils.setupGraphics(graphics, fontSize);
        Font base = graphics.getFont();
        String family = config == null ? "RuneScape" : config.getFontFamily();
        boolean bold = config != null && config.isBoldText();

        if (family != null && !"RuneScape".equalsIgnoreCase(family)) {
            int style = bold ? Font.BOLD : Font.PLAIN;
            float size = base == null ? 12f : base.getSize2D();
            Font selected = new Font(family, style, Math.max(1, Math.round(size))).deriveFont(style, size);
            graphics.setFont(selected);
        } else if (bold && base != null) {
            graphics.setFont(base.deriveFont(Font.BOLD, base.getSize2D()));
        }

        return graphics.getFontMetrics();
    }
}
