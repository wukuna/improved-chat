/* Improved Chat native chat collapse module. See THIRD_PARTY_NOTICES.md for required attribution. */
package com.improvedchat.chatbox.collapse;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.chatbox.opacity.ChatboxOpacityModule;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;


@Slf4j
@Singleton
public class ChatCollapseModule {
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ChatWidgetManager widgetManager;

    private final ChatState state = new ChatState();

    @Inject
    private ImprovedChatConfig config;
    @Inject
    private EventBus eventBus;
    @Inject
    private ChatboxOpacityModule chatboxOpacityModule;

    private boolean started;

    public synchronized void startUp() {
        if (started || !config.enableCollapsibleChat()) {
            return;
        }
        started = true;
        state.reset();
        eventBus.register(this);
        clientThread.invokeLater(this::refreshAll);
    }

    public synchronized void shutDown() {
        if (!started) {
            return;
        }
        started = false;
        eventBus.unregister(this);
        state.reset();
        clientThread.invokeLater(() -> {
            refreshChatWidgets();
            client.runScript(113);
            chatboxOpacityModule.reapplyAfterChatMutation();
        });
    }

    private void refreshAll() {
        widgetManager.setupMouseListeners(state, this::refreshChatWidgets);
        updateChatState();
        refreshChatWidgets();
    }

    private void refreshChatWidgets() {
        widgetManager.updateChatWidgets(state);
        chatboxOpacityModule.reapplyAfterChatMutation();
    }

    @Subscribe
    void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() == ScriptID.CHAT_PROMPT_INIT) {
            refreshAll();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!"Switch tab".equals(event.getMenuOption())) return;

        clientThread.invokeLater(() -> {
            updateChatState();
            refreshChatWidgets();
        });
    }

    @Subscribe
    void onVarClientIntChanged(VarClientIntChanged event) {
        if (event.getIndex() == VarClientID.CHAT_LASTREBUILD) {
            refreshAll();
        }
    }

    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (ImprovedChatConfig.GROUP.equals(event.getGroup())) {
            clientThread.invokeLater(this::refreshChatWidgets);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (state.isCollapsed()) {
            if (isSubscribedToChatMessageType(event.getType())) {
                state.hasUnseenMessages = true;
            }
        }
        clientThread.invokeLater(this::refreshChatWidgets);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (config.collapsedButtonContent() == ImprovedChatConfig.CollapsedButtonContent.REPORT_BUTTON_TEXT) {
            clientThread.invokeLater(this::refreshChatWidgets);
        }
    }

    private boolean isUsingSplitPrivateChat() {
        return client.getVarpValue(VarPlayerID.OPTION_PM) == 1;
    }

    private boolean isPrivateChatHiddenWithChat() {
        return client.getVarbitValue(VarbitID.HIDE_PM_ALONGSIDE_CHATBOX) == 1;
    }

    private boolean arePrivateMessagesHiddenOnCollapse() {
        return !isUsingSplitPrivateChat() || isPrivateChatHiddenWithChat();
    }

    private boolean isSubscribedToChatMessageType(ChatMessageType messageType) {
        switch (messageType) {
            case PUBLICCHAT:
                return config.highlightOnUnreadPublicMessages();
            case PRIVATECHAT:
                return config.highlightOnUnreadPrivateMessages() && arePrivateMessagesHiddenOnCollapse();
            case CLAN_CHAT:
                return config.highlightOnUnreadClanChatMessages();
            case TRADE:
                return config.highlightOnUnreadTradeMessages();
            case FRIENDSCHAT:
                return config.highlightOnUnreadFriendsChatMessages();
            default:
                return false;
        }
    }

    private void updateChatState() {
        state.selectedChatButton = widgetManager.getSelectedChatButton();
        if (state.selectedChatButton == null) {
            state.collapseState = ChatCollapseState.COLLAPSED;
        } else {
            state.collapseState = ChatCollapseState.EXPANDED;
            state.hasUnseenMessages = false;
        }
    }
}
