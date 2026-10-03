package com.improvedchat.release;

import com.improvedchat.ImprovedChatConfig;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

/**
 * Shows a short in-chat release notice for the current Improved Chat update.
 *
 * The notice is intentionally stateful but not user-configurable: each release id is shown at most
 * twice in total, and never more than once per RuneLite client session. Future releases only need
 * to change CURRENT_NOTICE_ID and NOTICE_TEXT.
 */
@Singleton
public final class ReleaseNoticeModule
{
    static final String CURRENT_NOTICE_ID = "part4-overlay-filters-clan-v1";
    static final int MAX_REMINDERS = 2;

    private static final String NOTICE_ID_KEY = "releaseNoticeId";
    private static final String NOTICE_COUNT_KEY = "releaseNoticeCount";
    private static final String NOTICE_TEXT =
        "Improved Chat has been updated: overlays now support independent chat filters and clan rank/offline "
            + "display, with corrected fixed-width timestamps and offline clan icons.";

    /**
     * External plugins can be toggled without restarting RuneLite. Keep this static so toggling
     * Improved Chat off/on cannot consume both reminders in one client session.
     */
    private static volatile boolean shownThisClientSession;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    private EventBus eventBus;

    private boolean started;

    public synchronized void startUp()
    {
        if (started)
        {
            return;
        }

        started = true;
        eventBus.register(this);
        clientThread.invokeLater(this::maybeShowNotice);
    }

    public synchronized void shutDown()
    {
        if (!started)
        {
            return;
        }

        started = false;
        eventBus.unregister(this);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            maybeShowNotice();
        }
    }

    private void maybeShowNotice()
    {
        if (!started || shownThisClientSession || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        String storedId = configManager.getConfiguration(ImprovedChatConfig.GROUP, NOTICE_ID_KEY);
        String storedCount = configManager.getConfiguration(ImprovedChatConfig.GROUP, NOTICE_COUNT_KEY);
        int reminderIndex = nextReminderIndex(storedId, storedCount);
        if (reminderIndex == 0)
        {
            return;
        }

        chatMessageManager.queue(QueuedMessage.builder()
            .type(ChatMessageType.CONSOLE)
            .value(buildMessage(reminderIndex))
            .build());

        configManager.setConfiguration(ImprovedChatConfig.GROUP, NOTICE_ID_KEY, CURRENT_NOTICE_ID);
        configManager.setConfiguration(
            ImprovedChatConfig.GROUP,
            NOTICE_COUNT_KEY,
            Integer.toString(reminderIndex));

        shownThisClientSession = true;
    }

    static int nextReminderIndex(String storedId, String storedCount)
    {
        if (!CURRENT_NOTICE_ID.equals(storedId))
        {
            return 1;
        }

        int count = 0;
        if (storedCount != null)
        {
            try
            {
                count = Integer.parseInt(storedCount);
            }
            catch (NumberFormatException ignored)
            {
                count = 0;
            }
        }

        if (count < 0)
        {
            count = 0;
        }

        return count >= MAX_REMINDERS ? 0 : count + 1;
    }

    static String buildMessage(int reminderIndex)
    {
        return NOTICE_TEXT + " Reminder " + reminderIndex + "/" + MAX_REMINDERS;
    }
}
