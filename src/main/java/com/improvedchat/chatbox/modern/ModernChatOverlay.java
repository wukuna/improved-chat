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
 * Modern chrome for RuneLite's native chatbox.
 *
 * Unlike Modern Chat's replacement overlay, this deliberately leaves RuneLite's native message,
 * input, tab click, menu and collapse behavior in place. The visual language is Improved Chat's:
 * a restrained glass surface, thin accent rail, segmented tab dock, selected-tab accent and
 * compact unread dot.
 */
public class ModernChatOverlay extends Overlay {
    private static final int PANEL_ARC = 12;
    private static final int TAB_ARC = 7;

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
        Rectangle chatBounds = background != null && !background.isHidden() ? background.getBounds() : null;

        if (chatBounds != null && usesTransparentChatColors()) {
            drawMessageSurface(graphics, chatBounds);
        }

        Rectangle tabRail = visibleTabRail();
        if (tabRail != null) {
            drawTabRail(graphics, tabRail);
            drawTabs(graphics);
        }

        Widget input = client.getWidget(InterfaceID.Chatbox.INPUT);
        if (input != null && !input.isHidden()) {
            drawInputDock(graphics, input.getBounds(), chatBounds);
        }

        return chatBounds == null ? null : chatBounds.getSize();
    }

    private void drawMessageSurface(Graphics2D g, Rectangle b) {
        Color background = applyOpacity(config.modernBackgroundColor(), config.chatboxOpacity());
        Color accent = config.modernAccentColor();

        g.setColor(background);
        g.fillRoundRect(b.x, b.y, b.width, b.height, PANEL_ARC, PANEL_ARC);

        // Fine outline plus a short top rail gives the surface definition without a heavy neon box.
        g.setColor(alpha(accent, Math.min(130, Math.max(55, accent.getAlpha() / 2))));
        g.drawRoundRect(b.x, b.y, Math.max(0, b.width - 1), Math.max(0, b.height - 1), PANEL_ARC, PANEL_ARC);

        int railW = Math.max(18, b.width / 5);
        int railX = b.x + (b.width - railW) / 2;
        g.setColor(alpha(accent, Math.max(120, accent.getAlpha())));
        g.fillRoundRect(railX, b.y + 2, railW, 2, 2, 2);
    }

    private Rectangle visibleTabRail() {
        Rectangle rail = null;
        boolean singleCollapsed = isSingleButtonCollapsed();

        for (ChatButton button : ChatButton.values()) {
            Widget container = client.getWidget(button.containerID);
            if (container == null || container.isHidden()) {
                continue;
            }
            if (singleCollapsed && button == ChatButton.ALL && config.collapsedButtonTransparent()) {
                continue;
            }

            Rectangle b = container.getBounds();
            if (b.width <= 0 || b.height <= 0) {
                continue;
            }
            rail = rail == null ? new Rectangle(b) : rail.union(b);
        }
        return rail;
    }

    private void drawTabRail(Graphics2D g, Rectangle rail) {
        Color tab = applyOpacity(config.modernTabColor(), config.buttonOpacity());
        int inset = config.modernCompactTabs() ? 1 : 0;
        Rectangle r = new Rectangle(
            rail.x + inset,
            rail.y + inset,
            Math.max(1, rail.width - inset * 2),
            Math.max(1, rail.height - inset * 2)
        );

        g.setColor(alpha(tab, Math.max(85, Math.min(190, tab.getAlpha() / 2 + 45))));
        g.fillRoundRect(r.x, r.y, r.width, r.height, TAB_ARC + 2, TAB_ARC + 2);

        Color accent = config.modernAccentColor();
        g.setColor(alpha(accent, Math.min(105, Math.max(45, accent.getAlpha() / 3))));
        g.drawRoundRect(r.x, r.y, Math.max(0, r.width - 1), Math.max(0, r.height - 1), TAB_ARC + 2, TAB_ARC + 2);
    }

    private void drawTabs(Graphics2D g) {
        boolean singleCollapsed = isSingleButtonCollapsed();

        for (ChatButton button : ChatButton.values()) {
            Widget container = client.getWidget(button.containerID);
            Widget graphic = client.getWidget(button.graphicID);
            if (container == null || container.isHidden()) {
                continue;
            }

            if (singleCollapsed && button == ChatButton.ALL && config.collapsedButtonTransparent()) {
                continue;
            }

            Rectangle b = container.getBounds();
            if (b.width <= 0 || b.height <= 0) {
                continue;
            }

            int sprite = graphic == null ? -1 : graphic.getSpriteId();
            boolean selected = sprite == SpriteID.ChatTabButton.SELECTED
                || sprite == SpriteID.ChatTabButton.SELECTED_HOVERED;
            boolean unread = sprite == SpriteID.ChatTabButton.NEW_MESSAGES;

            int padX = config.modernCompactTabs() ? 3 : 1;
            int padY = config.modernCompactTabs() ? 3 : 1;
            Rectangle face = new Rectangle(
                b.x + padX,
                b.y + padY,
                Math.max(1, b.width - padX * 2),
                Math.max(1, b.height - padY * 2)
            );

            Color fill = selected ? config.modernSelectedTabColor() : config.modernTabColor();
            fill = applyOpacity(fill, config.buttonOpacity());
            g.setColor(fill);
            g.fillRoundRect(face.x, face.y, face.width, face.height, TAB_ARC, TAB_ARC);

            if (selected) {
                Color accent = config.modernAccentColor();
                int lineW = Math.max(10, face.width - 12);
                int lineX = face.x + (face.width - lineW) / 2;
                g.setColor(alpha(accent, Math.max(145, accent.getAlpha())));
                g.fillRoundRect(lineX, face.y + 1, lineW, 2, 2, 2);
            }

            if (unread) {
                Color unreadColor = config.modernUnreadColor();
                int dot = Math.max(5, Math.min(7, face.height / 4));
                g.setColor(unreadColor);
                g.fillOval(face.x + face.width - dot - 5, face.y + 4, dot, dot);
            }
        }
    }

    private void drawInputDock(Graphics2D g, Rectangle input, Rectangle chatBounds) {
        Color tab = applyOpacity(config.modernTabColor(), config.buttonOpacity());
        Color accent = config.modernAccentColor();

        int x = chatBounds == null ? input.x - 5 : chatBounds.x + 8;
        int width = chatBounds == null ? input.width + 10 : Math.max(1, chatBounds.width - 16);
        int y = input.y - 3;
        int height = Math.max(18, input.height + 6);

        g.setColor(alpha(tab, Math.max(130, tab.getAlpha())));
        g.fillRoundRect(x, y, width, height, 8, 8);

        g.setColor(alpha(accent, Math.min(115, Math.max(55, accent.getAlpha() / 2))));
        g.drawRoundRect(x, y, Math.max(0, width - 1), Math.max(0, height - 1), 8, 8);

        // Short left prompt marker instead of copying Modern Chat's boxed input chrome.
        g.setColor(alpha(accent, Math.max(145, accent.getAlpha())));
        g.fillRoundRect(x + 3, y + 4, 3, Math.max(6, height - 8), 3, 3);
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

    private Color applyOpacity(Color color, int widgetOpacity) {
        if (!config.enableChatboxOpacity() || widgetOpacity < 0) {
            return color;
        }
        // RuneLite widget opacity is inverted: 0 = opaque, 255 = transparent.
        // Preserve the user's chosen modern-color alpha and apply the opacity control as
        // an additional transparency factor rather than letting the two settings fight.
        int visible = 255 - Math.max(0, Math.min(255, widgetOpacity));
        int combinedAlpha = color.getAlpha() * visible / 255;
        return alpha(color, combinedAlpha);
    }

    private static Color alpha(Color color, int alpha) {
        return new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            Math.max(0, Math.min(255, alpha))
        );
    }
}
