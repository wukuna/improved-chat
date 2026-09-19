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

public class ModernChatOverlay extends Overlay {
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
        Rectangle chatBounds = null;
        if (background != null && !background.isHidden()) {
            chatBounds = background.getBounds();
            // RuneLite uses dark text on its opaque parchment and light text on its transparent
            // resizable chat. Only replace the message background when the active color set is
            // already intended for a transparent/dark surface.
            if (usesTransparentChatColors()) {
                Color bg = config.modernBackgroundColor();
                graphics.setColor(bg);
                graphics.fillRoundRect(chatBounds.x, chatBounds.y, chatBounds.width, chatBounds.height, 10, 10);
            }
        }

        for (ChatButton button : ChatButton.values()) {
            Widget container = client.getWidget(button.containerID);
            Widget graphic = client.getWidget(button.graphicID);
            if (container == null || container.isHidden()) {
                continue;
            }
            Rectangle b = container.getBounds();
            Color fill = config.modernTabColor();
            if (graphic != null) {
                int sprite = graphic.getSpriteId();
                if (sprite == SpriteID.ChatTabButton.NEW_MESSAGES) {
                    fill = config.modernUnreadColor();
                } else if (sprite == SpriteID.ChatTabButton.SELECTED
                        || sprite == SpriteID.ChatTabButton.SELECTED_HOVERED) {
                    fill = config.modernSelectedTabColor();
                }
            }

            boolean collapsedSingleButton = button == ChatButton.ALL && isSingleButtonCollapsed();
            if (collapsedSingleButton && config.collapsedButtonTransparent()) {
                continue;
            }

            graphics.setColor(fill);
            if (config.modernCompactTabs()) {
                graphics.fillRoundRect(b.x + 1, b.y + 2, Math.max(1, b.width - 2), Math.max(1, b.height - 4), 8, 8);
            } else {
                graphics.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
            }
        }

        Widget input = client.getWidget(InterfaceID.Chatbox.INPUT);
        if (input != null && !input.isHidden()) {
            Rectangle b = input.getBounds();
            graphics.setColor(config.modernTabColor());
            graphics.fillRoundRect(b.x - 4, b.y - 2, b.width + 8, b.height + 4, 8, 8);
        }

        return chatBounds == null ? null : chatBounds.getSize();
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
}
