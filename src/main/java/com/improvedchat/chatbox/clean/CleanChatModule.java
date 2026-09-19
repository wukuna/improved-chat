package com.improvedchat.chatbox.clean;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.chatbox.clean.overlay.ChatColorBarOverlay;
import com.improvedchat.chatbox.clean.overlay.ChatTimestampOverlay;
import com.improvedchat.chatbox.clean.util.FormatterExtractor;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.timestamp.TimestampPlugin;
import net.runelite.client.ui.overlay.OverlayManager;

/**
 * Integrated lifecycle for Clean Chat. Source behavior is kept in focused helpers so disabling
 * this feature restores native chat without taking down the rest of Improved Chat.
 */
@Singleton
public final class CleanChatModule {
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ImprovedChatConfig config;
    @Inject private ChannelNameManager channelNameManager;
    @Inject private ChatWidgetEditor chatWidgetEditor;
    @Inject private ChatBlocker chatBlocker;
    @Inject private EventBus eventBus;
    @Inject private OverlayManager overlayManager;
    @Inject private PluginManager pluginManager;
    @Inject private ChatColorBarOverlay colorBarOverlay;
    @Inject private ChatTimestampOverlay timestampOverlay;

    private boolean started;
    private boolean timestampPluginEnabled;
    private FormatterExtractor.ExtractionResult timestampTemplate;
    private int timestampTemplateWidth;

    public synchronized void startUp() {
        if (started || !config.enableCleanChat()) {
            return;
        }
        started = true;
        timestampPluginEnabled = findTimestampPluginEnabled();

        eventBus.register(this);
        eventBus.register(chatBlocker);
        eventBus.register(chatWidgetEditor);
        eventBus.register(channelNameManager);
        channelNameManager.startup();

        eventBus.register(timestampOverlay);
        overlayManager.add(timestampOverlay);
        timestampOverlay.startUp();
        overlayManager.add(colorBarOverlay);

        clientThread.invoke(() -> {
            handleScrollbarVisibility(config.hideScrollbar());
            if (client.getGameState() == GameState.LOGGED_IN) {
                client.refreshChat();
            }
        });
    }

    public synchronized void shutDown() {
        if (!started) {
            return;
        }
        started = false;

        eventBus.unregister(chatBlocker);
        eventBus.unregister(chatWidgetEditor);
        eventBus.unregister(channelNameManager);
        eventBus.unregister(timestampOverlay);
        eventBus.unregister(this);

        overlayManager.remove(timestampOverlay);
        overlayManager.remove(colorBarOverlay);
        channelNameManager.shutdown();

        clientThread.invoke(() -> {
            handleScrollbarVisibility(false);
            if (client.getGameState() == GameState.LOGGED_IN) {
                client.refreshChat();
            }
        });
    }

    public boolean isFixedWidthTimestampEnabled() {
        return started && config.isFixedWidthTimestampEnabled() && timestampPluginEnabled;
    }

    public FormatterExtractor.ExtractionResult getTimestampTemplate() {
        return timestampTemplate;
    }

    public void setTimestampTemplate(FormatterExtractor.ExtractionResult timestampTemplate) {
        this.timestampTemplate = timestampTemplate;
    }

    public int getTimestampTemplateWidth() {
        return timestampTemplateWidth;
    }

    public void setTimestampTemplateWidth(int timestampTemplateWidth) {
        this.timestampTemplateWidth = timestampTemplateWidth;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (ImprovedChatConfig.GROUP.equals(event.getGroup())) {
            if (!started || "enableCleanChat".equals(event.getKey())) {
                return;
            }
            clientThread.invokeLater(() -> {
                if (ImprovedChatConfig.HIDE_SCROLLBAR_KEY.equals(event.getKey())) {
                    handleScrollbarVisibility(config.hideScrollbar());
                }
                client.refreshChat();
            });
            return;
        }

        if ("runelite".equals(event.getGroup()) && "timestampplugin".equals(event.getKey())) {
            timestampPluginEnabled = findTimestampPluginEnabled();
            clientThread.invokeLater(client::refreshChat);
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (started && event.getGroupId() == InterfaceID.CHATBOX) {
            handleScrollbarVisibility(config.hideScrollbar());
        }
    }

    private boolean findTimestampPluginEnabled() {
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin instanceof TimestampPlugin) {
                return pluginManager.isPluginEnabled(plugin);
            }
        }
        return false;
    }

    private void handleScrollbarVisibility(boolean hide) {
        Widget scrollArea = client.getWidget(InterfaceID.Chatbox.SCROLLAREA);
        Widget scrollBar = client.getWidget(InterfaceID.Chatbox.CHATSCROLLBAR);
        if (scrollArea == null || scrollBar == null) {
            return;
        }

        scrollBar.setHidden(hide);
        // Width mode is MINUS; 0 consumes the full available parent width.
        scrollArea.setOriginalWidth(hide ? 0 : scrollBar.getWidth());
        scrollArea.revalidate();
    }
}
