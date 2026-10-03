package com.improvedchat.chatbox.offline;

import com.improvedchat.ImprovedChatConfig;
import java.awt.Color;
import java.util.IdentityHashMap;
import java.util.Map;
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

    private final Map<MessageNode, String> originalNames = new IdentityHashMap<>();
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
        for (MessageNode message : messages)
        {
            changed |= format(message);
        }

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
        originalNames.putIfAbsent(message, currentName);
        String base = originalNames.get(message);
        if (base == null)
        {
            base = "";
        }

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

        if (!result.equals(currentName))
        {
            message.setName(result);
            return true;
        }
        return false;
    }

    private void restoreAll()
    {
        boolean changed = false;
        for (Map.Entry<MessageNode, String> entry : originalNames.entrySet())
        {
            try
            {
                if (!entry.getValue().equals(entry.getKey().getName()))
                {
                    entry.getKey().setName(entry.getValue());
                    changed = true;
                }
            }
            catch (Exception ignored)
            {
                // Message nodes can expire while the plugin is enabled.
            }
        }

        originalNames.clear();
        if (changed)
        {
            client.refreshChat();
        }
    }
}
