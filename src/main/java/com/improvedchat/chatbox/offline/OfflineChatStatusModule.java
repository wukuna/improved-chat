package com.improvedchat.chatbox.offline;

import com.improvedchat.ImprovedChatConfig;
import java.awt.Color;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.IterableHashTable;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ClanMemberJoined;
import net.runelite.api.events.ClanMemberLeft;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

/**
 * Native-chat offline clan presentation. Icon registration/status resolution are shared with
 * Improved Chat overlays through {@link ClanChatPresentationService}.
 */
@Singleton
public final class OfflineChatStatusModule
{
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ImprovedChatConfig config;
    @Inject private EventBus eventBus;
    @Inject private ClanChatPresentationService clanPresentation;

    private final Map<MessageNode, ClanNameState> nameStates = new IdentityHashMap<>();
    private boolean started;

    public synchronized void startUp()
    {
        if (started || !config.enableOfflineChatStatus())
        {
            return;
        }

        started = true;
        eventBus.register(this);

        // ChatIconManager assigns registered icon indices asynchronously. Two deferred passes let
        // the shared offline marker finish registration before names are rebuilt.
        clientThread.invokeLater(() -> clientThread.invokeLater(this::formatAll));
    }

    public synchronized void shutDown()
    {
        if (!started)
        {
            return;
        }

        started = false;
        eventBus.unregister(this);
        clientThread.invoke(this::restoreAll);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!ImprovedChatConfig.GROUP.equals(event.getGroup()))
        {
            return;
        }

        if ("enableOfflineIcon".equals(event.getKey())
            || "enableOfflineColor".equals(event.getKey())
            || "offlineColor".equals(event.getKey()))
        {
            clientThread.invokeLater(this::formatAll);
        }
    }

    @Subscribe
    public void onClanMemberJoined(ClanMemberJoined event)
    {
        clientThread.invokeLater(this::formatAll);
    }

    @Subscribe
    public void onClanMemberLeft(ClanMemberLeft event)
    {
        clientThread.invokeLater(this::formatAll);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            clientThread.invokeLater(() -> clientThread.invokeLater(this::formatAll));
        }
    }

    private void formatAll()
    {
        if (!started)
        {
            return;
        }

        IterableHashTable<MessageNode> messages = client.getMessages();
        if (messages == null)
        {
            return;
        }

        boolean changed = false;
        Set<MessageNode> liveNodes = Collections.newSetFromMap(new IdentityHashMap<>());
        for (MessageNode message : messages)
        {
            liveNodes.add(message);
            changed |= format(message);
        }

        // Do not retain strong references to chat nodes that have left RuneLite's active message
        // table. This also prevents stale decoration state from surviving longer than the message.
        nameStates.keySet().removeIf(node -> !liveNodes.contains(node));

        if (changed)
        {
            client.refreshChat();
        }
    }

    private boolean format(MessageNode message)
    {
        ChatMessageType type = message.getType();
        if (type != ChatMessageType.CLAN_CHAT
            && type != ChatMessageType.CLAN_GUEST_CHAT
            && type != ChatMessageType.CLAN_GIM_CHAT)
        {
            return false;
        }

        String currentName = message.getName();
        ClanNameState state = nameStates.get(message);
        if (state == null || !state.matches(message))
        {
            // RuneLite recycles MessageNode objects as the chat buffer advances. A recycled node
            // must start with the new message's own sender name, never the cached name from the
            // older message which previously occupied this object.
            state = ClanNameState.capture(message);
            nameStates.put(message, state);
        }

        String base = state.baseName(currentName);

        ClanChatPresentationService.Presentation presentation =
            clanPresentation.resolve(base, type);

        String result = base;
        if (presentation.isOffline())
        {
            if (config.enableOfflineColor())
            {
                Color color = config.offlineColor();
                result = String.format("<col=%02x%02x%02x>%s</col>",
                    color.getRed(), color.getGreen(), color.getBlue(), result);
            }

            if (config.enableOfflineIcon() && presentation.getOfflineIconId() >= 0)
            {
                result = "<img=" + presentation.getOfflineIconId() + ">" + result;
            }
        }

        boolean changed = !result.equals(currentName);
        if (changed)
        {
            message.setName(result);
        }
        state.recordApplied(result);
        return changed;
    }

    private void restoreAll()
    {
        boolean changed = false;
        for (Map.Entry<MessageNode, ClanNameState> entry : nameStates.entrySet())
        {
            try
            {
                MessageNode node = entry.getKey();
                ClanNameState state = entry.getValue();

                // Never restore onto a recycled node, and never overwrite a name another plugin
                // changed after our last decoration pass.
                if (state.shouldRestore(node))
                {
                    node.setName(state.getOriginalName());
                    changed = true;
                }
            }
            catch (Exception ignored)
            {
                // Message nodes can expire while the plugin is enabled.
            }
        }

        nameStates.clear();
        if (changed)
        {
            client.refreshChat();
        }
    }
}
