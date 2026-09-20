package com.improvedchat.chatbox.menu;

import com.improvedchat.ImprovedChatConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

/** Integrated Remove Chat Options behavior. Hold Control to expose the original menu. */
@Singleton
public final class RemoveChatOptionsModule {
    private static final int REPORT_BUTTON_COMPONENT_ID = 10616863;

    private static final Set<Integer> PRESERVED_CHAT_COMPONENTS = Set.of(
        ComponentID.CHATBOX_TAB_PRIVATE,
        ComponentID.CHATBOX_TAB_ALL,
        ComponentID.CHATBOX_TAB_CHANNEL,
        ComponentID.CHATBOX_TAB_CLAN,
        ComponentID.CHATBOX_TAB_GAME,
        ComponentID.CHATBOX_TAB_TRADE,
        ComponentID.CHATBOX_TAB_PUBLIC,
        ComponentID.CHATBOX_GE_SEARCH_RESULTS,
        REPORT_BUTTON_COMPONENT_ID
    );

    private static final Set<String> EXTERNAL_CHAT_OPTIONS = Set.of("Copy to clipboard");

    @Inject private Client client;
    @Inject private ImprovedChatConfig config;
    @Inject private EventBus eventBus;

    private boolean started;
    private boolean chatMenuContext;

    public synchronized void startUp() {
        if (started || !config.enableRemoveChatOptions()) return;
        started = true;
        chatMenuContext = false;
        eventBus.register(this);
    }

    public synchronized void shutDown() {
        if (!started) return;
        started = false;
        chatMenuContext = false;
        eventBus.unregister(this);
    }


    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        boolean wasChatMenu = chatMenuContext;
        chatMenuContext = false;

        if (!started || !wasChatMenu || !config.removeLookupChatOption()
            || client.isKeyPressed(KeyCode.KC_CONTROL)) {
            return;
        }

        MenuEntry[] entries = event.getMenuEntries();
        List<MenuEntry> kept = new LinkedList<>();
        for (MenuEntry entry : entries) {
            if (entry.getType() == MenuAction.RUNELITE && "Lookup".equals(entry.getOption())) {
                continue;
            }
            kept.add(entry);
        }

        MenuEntry[] filtered = kept.toArray(new MenuEntry[0]);
        event.setMenuEntries(filtered);
        client.setMenuEntries(filtered);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!started) return;

        int sourceComponentId = event.getActionParam1();
        int sourceGroupId = WidgetUtil.componentToInterface(sourceComponentId);
        if (sourceGroupId == InterfaceID.PRIVATE_CHAT
            || (sourceGroupId == InterfaceID.CHATBOX && !PRESERVED_CHAT_COMPONENTS.contains(sourceComponentId))) {
            chatMenuContext = true;
        }

        if (client.isKeyPressed(KeyCode.KC_CONTROL)) return;

        MenuEntry[] entries = client.getMenuEntries();
        List<MenuEntry> kept = new LinkedList<>();
        for (MenuEntry entry : entries) {
            int componentId = entry.getParam1();
            int groupId = WidgetUtil.componentToInterface(componentId);
            if ((groupId != InterfaceID.CHATBOX || PRESERVED_CHAT_COMPONENTS.contains(componentId))
                && groupId != InterfaceID.PRIVATE_CHAT
                && !EXTERNAL_CHAT_OPTIONS.contains(entry.getOption())) {
                kept.add(entry);
            }
        }
        client.setMenuEntries(kept.toArray(new MenuEntry[0]));
    }
}
