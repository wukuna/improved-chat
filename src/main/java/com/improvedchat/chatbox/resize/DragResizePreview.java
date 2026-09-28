/* Derived from Chat Resizer by shanktank under BSD 2-Clause. See THIRD_PARTY_NOTICES.md. */
package com.improvedchat.chatbox.resize;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.chatbox.resize.internal.Widgets;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Improved Chat resize affordance.
 *
 * The hit targets intentionally remain forgiving along the top/right edges, but the visible UI is
 * a pair of centered grip capsules with a subtle dashed guide rather than Chat Resizer's filled
 * border bands. This keeps the inherited resize mechanics familiar without cloning its chrome.
 */
@Singleton
public final class DragResizePreview extends Overlay {
    private static final int TOP_GRIP_W = 54;
    private static final int TOP_GRIP_H = 8;
    private static final int SIDE_GRIP_W = 8;
    private static final int SIDE_GRIP_H = 54;
    private static final int GRIP_ARC = 8;

    private final Client client;
    private final ImprovedChatConfig config;
    private final DragResizeActuator drag;
    private final TooltipManager tooltipManager;
    private final SecondarySize swapSize;

    private Color base;
    private Color guide;
    private Color grip;
    private Color gripHover;
    private Color edge;
    private Color edgeHover;

    @Inject
    public DragResizePreview(
        Client client, ImprovedChatConfig config,
        DragResizeActuator drag, SecondarySize swapSize,
        TooltipManager tooltipManager
    ) {
        this.client = client;
        this.config = config;
        this.drag = drag;
        this.swapSize = swapSize;
        this.tooltipManager = tooltipManager;
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPosition(OverlayPosition.DYNAMIC);
    }

    private void ensureColors() {
        Color c = config.indicatorColor();
        if (c.equals(base)) {
            return;
        }

        base = c;
        int a = Math.max(48, c.getAlpha());
        guide = ColorUtil.colorWithAlpha(c, Math.max(24, a / 4));
        grip = ColorUtil.colorWithAlpha(c, Math.max(72, a / 2));
        gripHover = ColorUtil.colorWithAlpha(c, a);
        edge = ColorUtil.colorWithAlpha(Color.WHITE, Math.min(170, Math.max(80, a / 2)));
        edgeHover = ColorUtil.colorWithAlpha(Color.WHITE, Math.min(235, Math.max(145, a)));
    }

    @Override
    public Dimension render(Graphics2D g) {
        boolean guides = drag.isHighlightActive();
        boolean readout = drag.isSizeReadoutActive();
        if (!guides && !readout) {
            return null;
        }

        ensureColors();
        boolean fixed = drag.isFixedMode();

        if (readout) {
            String size = fixed
                ? "H " + signed(swapSize.effectiveFixedHeightChange())
                : "W " + signed(swapSize.effectiveWidthChange()) + "  •  H " + signed(swapSize.effectiveHeightChange());
            String set = swapSize.isActive() ? "Secondary" : "Primary";
            tooltipManager.add(new Tooltip("Resize  •  " + size + "  •  " + set));
        }

        Widget slot = Widgets.chatSlot(client);
        if (slot == null) {
            return null;
        }

        Rectangle b = Widgets.liveBounds(slot);
        Point p = drag.getPointer();

        if (guides) {
            int grab = DragResizeActuator.BORDER_GRAB;
            Rectangle topBand = DragResizeActuator.topBand(b, grab, client.getCanvasHeight());
            boolean topActive = drag.isDraggingTop() || (p != null && topBand.contains(p));
            drawTopGrip(g, b, topBand, topActive);

            if (!fixed) {
                Rectangle sideBand = DragResizeActuator.rightBand(b, grab);
                boolean sideActive = drag.isDraggingRight() || (p != null && sideBand.contains(p));
                drawSideGrip(g, b, sideBand, sideActive);
            }
        }

        return null;
    }

    private void drawTopGrip(Graphics2D g, Rectangle chat, Rectangle band, boolean active) {
        int y = band.y + Math.max(0, (band.height - TOP_GRIP_H) / 2);
        int x = chat.x + (chat.width - TOP_GRIP_W) / 2;

        drawDashedGuide(g, chat.x + 8, band.y + band.height / 2, chat.x + chat.width - 9, band.y + band.height / 2);

        g.setColor(active ? gripHover : grip);
        g.fillRoundRect(x, y, TOP_GRIP_W, TOP_GRIP_H, GRIP_ARC, GRIP_ARC);
        g.setColor(active ? edgeHover : edge);
        g.drawRoundRect(x, y, TOP_GRIP_W, TOP_GRIP_H, GRIP_ARC, GRIP_ARC);

        int mid = x + TOP_GRIP_W / 2;
        for (int dx = -7; dx <= 7; dx += 7) {
            g.drawLine(mid + dx, y + 2, mid + dx, y + TOP_GRIP_H - 3);
        }
    }

    private void drawSideGrip(Graphics2D g, Rectangle chat, Rectangle band, boolean active) {
        int x = band.x + Math.max(0, (band.width - SIDE_GRIP_W) / 2);
        int y = chat.y + (chat.height - SIDE_GRIP_H) / 2;

        int guideX = band.x + band.width / 2;
        drawDashedGuide(g, guideX, chat.y + 8, guideX, chat.y + chat.height - 9);

        g.setColor(active ? gripHover : grip);
        g.fillRoundRect(x, y, SIDE_GRIP_W, SIDE_GRIP_H, GRIP_ARC, GRIP_ARC);
        g.setColor(active ? edgeHover : edge);
        g.drawRoundRect(x, y, SIDE_GRIP_W, SIDE_GRIP_H, GRIP_ARC, GRIP_ARC);

        int mid = y + SIDE_GRIP_H / 2;
        for (int dy = -7; dy <= 7; dy += 7) {
            g.drawLine(x + 2, mid + dy, x + SIDE_GRIP_W - 3, mid + dy);
        }
    }

    private void drawDashedGuide(Graphics2D g, int x1, int y1, int x2, int y2) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 5f}, 0f));
        g.setColor(guide);
        g.drawLine(x1, y1, x2, y2);
        g.setStroke(old);
    }

    private String signed(int value) {
        String text = value > 0 ? "+" + value : Integer.toString(value);
        return ColorUtil.wrapWithColorTag(text, base);
    }
}
