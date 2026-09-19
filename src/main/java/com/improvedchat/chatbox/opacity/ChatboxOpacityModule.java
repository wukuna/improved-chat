package com.improvedchat.chatbox.opacity;

import com.improvedchat.ImprovedChatConfig;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

/**
 * Integrated Chatbox Opacity behavior. Native widget mutation is used only for RuneLite's
 * transparent chatbox; Modernize Chat consumes the same config in its own overlay instead.
 */
@Singleton
public final class ChatboxOpacityModule {
    private static final int CHATBOX_GROUP_ID = 162;
    private static final int BUTTON_BACKGROUND_CHILD = 3;
    private static final int ORIGINAL_BUTTON_TYPE = 5;
    private static final int FILLED_BUTTON_TYPE = 3;

    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ImprovedChatConfig config;
    @Inject private EventBus eventBus;

    private final Map<Widget, Integer> backgroundOpacity = new IdentityHashMap<>();
    private boolean started;
    private int buttonSprite = -1;
    private int buttonOpacity = -1;
    private boolean buttonFilled;
    private int buttonType = ORIGINAL_BUTTON_TYPE;

    public synchronized void startUp() {
        if (started || !config.enableChatboxOpacity()) return;
        started = true;
        eventBus.register(this);
        clientThread.invoke(this::apply);
    }

    public synchronized void shutDown() {
        if (!started) return;
        started = false;
        eventBus.unregister(this);
        clientThread.invoke(() -> {
            restoreBackground();
            restoreButton();
            if (client.getGameState() == GameState.LOGGED_IN) client.runScript(ScriptID.BUILD_CHATBOX);
        });
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() == ScriptID.BUILD_CHATBOX) apply();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!ImprovedChatConfig.GROUP.equals(event.getGroup())) return;
        String key = event.getKey();
        if ("chatboxOpacity".equals(key) || "buttonOpacity".equals(key) || "modernizeChat".equals(key)) {
            clientThread.invokeLater(this::apply);
        }
    }

    public void reapplyAfterChatMutation() {
        if (!started) return;
        apply();
        clientThread.invokeLater(this::apply);
    }

    private void apply() {
        if (!started) return;

        // Modernize Chat owns these surfaces while enabled; it maps the same opacity values to
        // its glass/chat-tab colors so two systems never fight over native widget opacity.
        if (config.modernizeChat()) {
            restoreBackground();
            restoreButton();
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN
            || !client.isResized()
            || client.getVarbitValue(VarbitID.CHATBOX_TRANSPARENCY) == 0) {
            restoreBackground();
            restoreButton();
            return;
        }

        Widget background = client.getWidget(net.runelite.api.gameval.InterfaceID.Chatbox.CHAT_BACKGROUND);
        Widget[] children = background == null ? null : background.getDynamicChildren();
        if (children != null) {
            boolean generationChanged = backgroundOpacity.size() != liveCount(children);
            if (!generationChanged) {
                for (Widget child : children) {
                    if (child != null && !backgroundOpacity.containsKey(child)) {
                        generationChanged = true;
                        break;
                    }
                }
            }
            if (generationChanged) {
                backgroundOpacity.clear();
                for (Widget child : children) if (child != null) backgroundOpacity.put(child, child.getOpacity());
            }

            if (config.chatboxOpacity() == -1) {
                restoreBackground();
            } else {
                for (Widget child : children) if (child != null) child.setOpacity(config.chatboxOpacity());
            }
        }

        Widget button = client.getWidget(CHATBOX_GROUP_ID, BUTTON_BACKGROUND_CHILD);
        if (button == null) return;
        if (config.buttonOpacity() == -1) {
            restoreButton();
        } else {
            if (buttonSprite == -1) {
                buttonSprite = button.getSpriteId();
                buttonOpacity = button.getOpacity();
                buttonFilled = button.isFilled();
                buttonType = button.getType();
            }
            button.setSpriteId(-1);
            button.setType(FILLED_BUTTON_TYPE);
            button.setFilled(true);
            button.setOpacity(config.buttonOpacity());
        }
    }

    private static int liveCount(Widget[] widgets) {
        int count = 0;
        for (Widget widget : widgets) if (widget != null) count++;
        return count;
    }

    private void restoreBackground() {
        for (Map.Entry<Widget, Integer> entry : backgroundOpacity.entrySet()) {
            try {
                entry.getKey().setOpacity(entry.getValue());
            } catch (Exception ignored) {
                // Widget generations are transient; stale instances can safely be dropped.
            }
        }
        backgroundOpacity.clear();
    }

    private void restoreButton() {
        if (buttonSprite == -1) return;
        Widget button = client.getWidget(CHATBOX_GROUP_ID, BUTTON_BACKGROUND_CHILD);
        if (button != null) {
            button.setSpriteId(buttonSprite);
            button.setOpacity(buttonOpacity);
            button.setFilled(buttonFilled);
            button.setType(buttonType);
        }
        buttonSprite = -1;
        buttonOpacity = -1;
    }
}
