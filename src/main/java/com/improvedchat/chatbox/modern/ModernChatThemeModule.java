package com.improvedchat.chatbox.modern;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.ImprovedChatPlugin;
import com.improvedchat.chatbox.collapse.ChatButton;
import com.improvedchat.chatbox.collapse.ChatCollapseModule;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
public class ModernChatThemeModule {
    @Inject private Client client;
    @Inject private ImprovedChatConfig config;
    @Inject private EventBus eventBus;
    @Inject private OverlayManager overlayManager;
    @Inject private ChatCollapseModule collapseModule;

    private final Map<Integer, Integer> originalOpacity = new HashMap<>();
    private final Map<Integer, Integer> originalTextColor = new HashMap<>();
    // CHAT_BACKGROUND is a container. RuneLite rebuilds the visible parchment/gradient as
    // dynamic children, so track those widget instances directly rather than mutating the
    // container and hoping opacity propagates through it.
    private final Map<Widget, Integer> originalBackgroundOpacity = new IdentityHashMap<>();
    private ModernChatOverlay overlay;
    private boolean started;
    private boolean styled;

    public synchronized void startUp(ImprovedChatPlugin plugin) {
        if (started) {
            return;
        }
        started = true;
        overlay = new ModernChatOverlay(plugin, client, config);
        overlayManager.add(overlay);
        eventBus.register(this);
    }

    public synchronized void shutDown() {
        if (!started) {
            return;
        }
        started = false;
        eventBus.unregister(this);
        restoreNativeWidgets();
        if (overlay != null) {
            overlayManager.remove(overlay);
            overlay = null;
        }
    }

    @Subscribe
    public void onBeforeRender(BeforeRender event) {
        if (config.modernizeChat()) {
            applyNativeStyle();
        } else if (styled) {
            restoreNativeWidgets();
        }
    }

    private void applyNativeStyle() {
        styled = true;

        if (usesTransparentChatColors()) {
            hideNativeBackground();
        } else {
            restoreNativeBackground();
        }

        for (ChatButton button : ChatButton.values()) {
            rememberAndSetOpacity(button.graphicID, 255);
            rememberAndSetTextColor(button.textID, config.modernTextColor().getRGB());
        }

        rememberAndSetTextColor(InterfaceID.Chatbox.INPUT, config.modernTextColor().getRGB());
    }

    private void hideNativeBackground() {
        Widget background = client.getWidget(InterfaceID.Chatbox.CHAT_BACKGROUND);
        if (background == null) {
            return;
        }

        Widget[] children = background.getDynamicChildren();
        if (children == null) {
            return;
        }

        int liveChildren = 0;
        boolean sameGeneration = true;
        for (Widget child : children) {
            if (child == null) {
                continue;
            }
            liveChildren++;
            if (!originalBackgroundOpacity.containsKey(child)) {
                sameGeneration = false;
            }
        }
        if (originalBackgroundOpacity.size() != liveChildren) {
            sameGeneration = false;
        }

        // The game rebuilds these dynamic children. Keep only the current generation so a
        // long-running client cannot accumulate references to stale widget instances.
        if (!sameGeneration) {
            originalBackgroundOpacity.clear();
            for (Widget child : children) {
                if (child != null) {
                    originalBackgroundOpacity.put(child, child.getOpacity());
                }
            }
        }

        for (Widget child : children) {
            if (child != null) {
                child.setOpacity(255);
            }
        }
    }

    private void restoreNativeBackground() {
        Widget background = client.getWidget(InterfaceID.Chatbox.CHAT_BACKGROUND);
        if (background != null) {
            Widget[] children = background.getDynamicChildren();
            if (children != null) {
                for (Widget child : children) {
                    if (child == null) {
                        continue;
                    }
                    Integer opacity = originalBackgroundOpacity.get(child);
                    if (opacity != null) {
                        child.setOpacity(opacity);
                    }
                }
            }
        }
        originalBackgroundOpacity.clear();
    }

    private boolean usesTransparentChatColors() {
        return client.isResized() && client.getVarbitValue(VarbitID.CHATBOX_TRANSPARENCY) == 1;
    }

    private void rememberAndSetOpacity(int widgetId, int opacity) {
        Widget w = client.getWidget(widgetId);
        if (w == null) {
            return;
        }
        originalOpacity.putIfAbsent(widgetId, w.getOpacity());
        w.setOpacity(opacity);
    }

    private void rememberAndSetTextColor(int widgetId, int color) {
        Widget w = client.getWidget(widgetId);
        if (w == null) {
            return;
        }
        originalTextColor.putIfAbsent(widgetId, w.getTextColor());
        w.setTextColor(color & 0xFFFFFF);
    }

    private void restoreNativeWidgets() {
        restoreNativeBackground();

        for (Map.Entry<Integer, Integer> e : originalOpacity.entrySet()) {
            Widget w = client.getWidget(e.getKey());
            if (w != null) {
                w.setOpacity(e.getValue());
            }
        }
        for (Map.Entry<Integer, Integer> e : originalTextColor.entrySet()) {
            Widget w = client.getWidget(e.getKey());
            if (w != null) {
                w.setTextColor(e.getValue());
            }
        }
        originalOpacity.clear();
        originalTextColor.clear();
        styled = false;
        collapseModule.refreshAfterExternalStyleChange();
    }
}
