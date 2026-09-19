package com.improvedchat.chatbox.modern;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.ImprovedChatPlugin;
import com.improvedchat.chatbox.collapse.ChatButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Improved Chat's native-widget modernization layer.
 *
 * This intentionally keeps RuneLite's native chat behavior and only supplies visual chrome:
 * a unified tab rail, selected-tab accent, unread dot, framed message surface and input composer.
 */
public class ModernChatOverlay extends Overlay {
    private static final int PANEL_ARC = 10;
    private static final int TAB_ARC = 8;

    private final Client client;
    private final ImprovedChatConfig config;

    public ModernChatOverlay(ImprovedChatPlugin plugin, Client client, ImprovedChatConfig config) {
        super(plugin);
        this.client = client;
        this.config = config;
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public String getName() {
        return "Improved Chat Modernized Chatbox";
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.modernizeChat()) {
            return null;
        }

        Widget background = client.getWidget(InterfaceID.Chatbox.CHAT_BACKGROUND);
        Rectangle chatBounds = background == null || background.isHidden() ? null : background.getBounds();

        if (chatBounds != null) {
            if (usesTransparentChatColors()) {
                graphics.setColor(chatSurface(config.modernBackgroundColor()));
                graphics.fillRoundRect(
                    chatBounds.x + 1, chatBounds.y + 1,
                    Math.max(1, chatBounds.width - 2), Math.max(1, chatBounds.height - 2),
                    PANEL_ARC, PANEL_ARC);
            }

            graphics.setColor(chatSurface(config.modernBorderColor()));
            graphics.drawRoundRect(
                chatBounds.x, chatBounds.y,
                Math.max(1, chatBounds.width - 1), Math.max(1, chatBounds.height - 1),
                PANEL_ARC, PANEL_ARC);
        }

        Rectangle rail = tabRailBounds();
        if (rail != null) {
            Color railColor = buttonSurface(withAlpha(config.modernTabColor(), Math.min(235, Math.max(110, config.modernTabColor().getAlpha()))));
            graphics.setColor(railColor);
            graphics.fillRoundRect(
                rail.x, rail.y + 1,
                rail.width, Math.max(1, rail.height - 2),
                TAB_ARC, TAB_ARC);
            graphics.setColor(buttonSurface(withAlpha(config.modernBorderColor(), Math.min(150, config.modernBorderColor().getAlpha()))));
            graphics.drawRoundRect(
                rail.x, rail.y + 1,
                Math.max(1, rail.width - 1), Math.max(1, rail.height - 3),
                TAB_ARC, TAB_ARC);
        }

        for (ChatButton button : ChatButton.values()) {
            Widget container = client.getWidget(button.containerID);
            Widget graphic = client.getWidget(button.graphicID);
            if (container == null || container.isHidden()) {
                continue;
            }

            boolean collapsedSingleButton = button == ChatButton.ALL && isSingleButtonCollapsed();
            if (collapsedSingleButton && config.collapsedButtonTransparent()) {
                continue;
            }

            Rectangle b = container.getBounds();
            int sprite = graphic == null ? -1 : graphic.getSpriteId();
            boolean selected = sprite == SpriteID.ChatTabButton.SELECTED
                || sprite == SpriteID.ChatTabButton.SELECTED_HOVERED;
            boolean unread = sprite == SpriteID.ChatTabButton.NEW_MESSAGES;

            int insetX = config.modernCompactTabs() ? 3 : 1;
            int insetY = config.modernCompactTabs() ? 3 : 2;
            int x = b.x + insetX;
            int y = b.y + insetY;
            int w = Math.max(1, b.width - insetX * 2);
            int h = Math.max(1, b.height - insetY * 2);

            if (selected) {
                graphics.setColor(buttonSurface(config.modernSelectedTabColor()));
                graphics.fillRoundRect(x, y, w, h, TAB_ARC, TAB_ARC);

                graphics.setColor(buttonSurface(config.modernAccentColor()));
                int underlineW = Math.max(12, w - 14);
                int underlineX = x + (w - underlineW) / 2;
                graphics.fillRoundRect(underlineX, y + h - 3, underlineW, 3, 3, 3);
            } else if (!config.modernCompactTabs()) {
                graphics.setColor(buttonSurface(withAlpha(config.modernTabColor(), Math.min(190, config.modernTabColor().getAlpha()))));
                graphics.fillRoundRect(x, y, w, h, TAB_ARC, TAB_ARC);
            }

            if (unread) {
                int dot = 6;
                graphics.setColor(buttonSurface(config.modernUnreadColor()));
                graphics.fillOval(x + w - dot - 4, y + 4, dot, dot);
            }
        }

        drawInputComposer(graphics);

        return chatBounds == null ? null : chatBounds.getSize();
    }

    private void drawInputComposer(Graphics2D graphics) {
        Widget input = client.getWidget(InterfaceID.Chatbox.INPUT);
        if (input == null || input.isHidden()) {
            return;
        }

        Rectangle b = input.getBounds();
        int x = b.x - 7;
        int y = b.y - 4;
        int w = b.width + 14;
        int h = b.height + 8;

        graphics.setColor(chatSurface(config.modernInputColor()));
        graphics.fillRoundRect(x, y, w, h, 8, 8);

        graphics.setColor(chatSurface(config.modernBorderColor()));
        graphics.drawRoundRect(x, y, Math.max(1, w - 1), Math.max(1, h - 1), 8, 8);

        graphics.setColor(buttonSurface(config.modernAccentColor()));
        graphics.fillRoundRect(x + 2, y + 4, 3, Math.max(4, h - 8), 3, 3);
    }

    private Rectangle tabRailBounds() {
        Rectangle rail = null;
        for (ChatButton button : ChatButton.values()) {
            Widget container = client.getWidget(button.containerID);
            if (container == null || container.isHidden()) {
                continue;
            }

            if (button == ChatButton.ALL && isSingleButtonCollapsed() && config.collapsedButtonTransparent()) {
                continue;
            }

            Rectangle b = container.getBounds();
            rail = rail == null ? new Rectangle(b) : rail.union(b);
        }

        if (rail == null) {
            return null;
        }

        rail.grow(2, 0);
        return rail;
    }

    private boolean usesTransparentChatColors() {
        return client.isResized() && client.getVarbitValue(VarbitID.CHATBOX_TRANSPARENCY) == 1;
    }

    private boolean isSingleButtonCollapsed() {
        for (ChatButton button : ChatButton.values()) {
            if (button == ChatButton.ALL) {
                continue;
            }
            Widget w = client.getWidget(button.containerID);
            if (w != null && !w.isSelfHidden()) {
                return false;
            }
        }
        return true;
    }

    private Color chatSurface(Color color) {
        return opacityAdjusted(color, config.enableChatboxOpacity() ? config.chatboxOpacity() : -1);
    }

    private Color buttonSurface(Color color) {
        return opacityAdjusted(color, config.enableChatboxOpacity() ? config.buttonOpacity() : -1);
    }

    private static Color opacityAdjusted(Color color, int runeLiteOpacity) {
        if (runeLiteOpacity < 0) {
            return color;
        }
        // RuneLite widget opacity is inverted: 0 = opaque, 255 = transparent.
        return withAlpha(color, 255 - Math.max(0, Math.min(255, runeLiteOpacity)));
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }
}
