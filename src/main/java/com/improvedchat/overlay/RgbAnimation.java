package com.improvedchat.overlay;

import com.improvedchat.model.AnimatedTextStyle;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.geom.Point2D;

/** Cosmetic animation on the existing render clock; no timers or retained runtime state. */
public final class RgbAnimation {
    private static final float[] STOPS = {0f, 1f / 6, 2f / 6, 3f / 6, 4f / 6, 5f / 6, 1f};

    private RgbAnimation() { }

    static double phase(long now, int seconds) {
        long period = Math.max(2, Math.min(30, seconds)) * 1000L;
        return Math.floorMod(now, period) / (double) period;
    }

    private static Color hue(double value, int alpha) {
        Color c = Color.getHSBColor((float) (value - Math.floor(value)), 1f, 1f);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    private static Paint gradient(double x, double y, double dx, double dy,
            double length, double distance, double phase, int alpha) {
        Color[] colors = new Color[STOPS.length];
        for (int i = 0; i < colors.length; i++) colors[i] = hue(STOPS[i], alpha);
        double offset = distance - phase * length;
        Point2D start = new Point2D.Double(x - dx * offset, y - dy * offset);
        Point2D end = new Point2D.Double(start.getX() + dx * length, start.getY() + dy * length);
        return new LinearGradientPaint(start, end, STOPS, colors, CycleMethod.REPEAT);
    }

    static void drawBorder(Graphics2D g, int inset, int width, int height, int alpha, long now, int seconds) {
        if (width <= 0 || height <= 0) return;
        double perimeter = 2.0 * (width + height);
        double phase = phase(now, seconds);
        int right = inset + width, bottom = inset + height;
        g.setPaint(gradient(inset, inset, 1, 0, perimeter, 0, phase, alpha));
        g.drawLine(inset, inset, right, inset);
        g.setPaint(gradient(right, inset, 0, 1, perimeter, width, phase, alpha));
        g.drawLine(right, inset, right, bottom);
        g.setPaint(gradient(right, bottom, -1, 0, perimeter, width + height, phase, alpha));
        g.drawLine(right, bottom, inset, bottom);
        g.setPaint(gradient(inset, bottom, 0, -1, perimeter, 2.0 * width + height, phase, alpha));
        g.drawLine(inset, bottom, inset, inset);
    }

    static int drawText(Graphics2D g, String text, int alpha, int x, int y,
            boolean shadow, FontMetrics metrics, OverlayConfig cfg, long now, int originX) {
        Paint old = g.getPaint();
        try {
            if (shadow) {
                g.setColor(new Color(0, 0, 0, Math.max(0, Math.min(255, alpha))));
                g.drawString(text, x + 2, y + 1);
            }
            double phase = phase(now, cfg.getRgbCycleSeconds());
            g.setPaint(cfg.getAnimatedTextStyle() == AnimatedTextStyle.RGB_CYCLE
                    ? hue(-phase, alpha)
                    : gradient(originX, y, 1, 0, 240, 0, phase, alpha));
            g.drawString(text, x + 1, y);
            return metrics.stringWidth(text);
        } finally {
            g.setPaint(old);
        }
    }
}
