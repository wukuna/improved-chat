package com.improvedchat.dialogue;

import com.improvedchat.ImprovedChatConfig;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

/**
 * Lifecycle wrapper for the integrated Dialogue Fonts feature.
 */
@Singleton
public final class DialogueFontsModule {
    @Inject private ImprovedChatConfig config;
    @Inject private EventBus eventBus;
    @Inject private OverlayManager overlayManager;
    @Inject private DialogueFontsOverlay overlay;
    @Inject private DialogueWidgetManager widgetManager;

    private boolean started;

    public synchronized void startUp() {
        if (started || !config.enableDialogueFonts()) {
            return;
        }
        started = true;
        overlayManager.add(overlay);
        eventBus.register(this);
    }

    public synchronized void shutDown() {
        if (!started) {
            return;
        }
        started = false;
        eventBus.unregister(this);
        overlayManager.remove(overlay);
        widgetManager.restoreAll();
        overlay.setState(null);
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (!started) {
            return;
        }
        overlay.setState(widgetManager.getCurrentDialogue());
    }
}
