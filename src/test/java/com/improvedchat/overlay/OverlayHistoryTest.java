package com.improvedchat.overlay;

import com.improvedchat.model.OverlayMessage;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayHistoryTest {
    private OverlayMessage chat(ChatMessageType type, String channel, boolean outgoing, String body) {
        return OverlayMessage.senderMessage("Alice", channel, body, 123, type, outgoing);
    }

    @Test public void sameTextAcrossChannelsOrDirectionsRemainsSeparate() {
        List<OverlayMessage> messages = Arrays.asList(
                chat(ChatMessageType.PUBLICCHAT, null, false, "hello"),
                chat(ChatMessageType.CLAN_CHAT, "Clan A", false, "hello"),
                chat(ChatMessageType.CLAN_CHAT, "Clan B", false, "hello"),
                chat(ChatMessageType.CLAN_CHAT, "Clan B", true, "hello"));
        assertEquals(4, OverlayHistory.select(messages, 0, 20, true).size());
    }

    @Test public void onlyConsecutiveMessagesCollapseAndSharedHistoryIsUnchanged() {
        OverlayMessage first = chat(ChatMessageType.CLAN_CHAT, "A", false, "<col=ff0000>hello</col>");
        OverlayMessage second = chat(ChatMessageType.CLAN_CHAT, "A", false, "hello");
        OverlayMessage third = chat(ChatMessageType.CLAN_CHAT, "A", false, "different");
        OverlayMessage fourth = chat(ChatMessageType.CLAN_CHAT, "A", false, "hello");
        List<OverlayMessage> history = Arrays.asList(first, second, third, fourth);
        List<OverlayMessage> view = OverlayHistory.select(history, 0, 20, true);
        assertEquals(3, view.size());
        assertEquals(2, view.get(0).getCount());
        assertEquals(second.getSequence(), view.get(0).getSequence());
        assertEquals(4, history.size());
        for (OverlayMessage message : history) assertEquals(1, message.getCount());
    }

    @Test public void clearBoundaryIsLocalAndNewDuplicateStartsAtOne() {
        OverlayMessage before = chat(ChatMessageType.PUBLICCHAT, null, false, "hello");
        long clearedThrough = OverlayMessage.latestSequence();
        OverlayMessage after = chat(ChatMessageType.PUBLICCHAT, null, false, "hello");
        List<OverlayMessage> history = Arrays.asList(before, after);
        List<OverlayMessage> cleared = OverlayHistory.select(history, clearedThrough, 20, true);
        assertEquals(1, cleared.size());
        assertEquals(1, cleared.get(0).getCount());
        assertEquals(2, OverlayHistory.select(history, 0, 20, true).get(0).getCount());
    }

    @Test public void delayedRewriteCannotReappearAfterClear() {
        OverlayMessage command = chat(ChatMessageType.CLAN_CHAT, "A", false, "!kc");
        long clearedThrough = OverlayMessage.latestSequence();
        OverlayMessage rewritten = command.withBody("Kill count: 123");
        assertEquals(command.getSequence(), rewritten.getSequence());
        assertEquals(command.getTimestamp(), rewritten.getTimestamp());
        assertTrue(OverlayHistory.select(Arrays.asList(rewritten), clearedThrough, 20, true).isEmpty());
        assertEquals(1, OverlayHistory.select(Arrays.asList(rewritten), 0, 20, true).size());
    }

    @Test public void delayedFinalBodiesCollapseWithoutLosingRawEntries() {
        OverlayMessage a = chat(ChatMessageType.PUBLICCHAT, null, false, ":)");
        OverlayMessage b = chat(ChatMessageType.PUBLICCHAT, null, false, "<img=42>");
        assertEquals(2, OverlayHistory.select(Arrays.asList(a, b), 0, 20, true).size());
        List<OverlayMessage> view = OverlayHistory.select(Arrays.asList(a.withBody("<img=42>"), b), 0, 20, true);
        assertEquals(1, view.size());
        assertEquals(2, view.get(0).getCount());
    }

    @Test public void limitAppliesAfterCollapsingAndIncludesNotifications() {
        OverlayMessage a = chat(ChatMessageType.PUBLICCHAT, null, false, "hello");
        OverlayMessage b = chat(ChatMessageType.PUBLICCHAT, null, false, "hello");
        OverlayMessage notification = OverlayMessage.loginNotification("Bob", "logged in", 123, 5);
        List<OverlayMessage> view = OverlayHistory.select(Arrays.asList(a, b, notification), 0, 2, true);
        assertEquals(2, view.size());
        assertEquals(2, view.get(0).getCount());
        assertSame(notification, view.get(1));
        assertEquals(2, OverlayHistory.select(Arrays.asList(notification, notification), 0, 20, true).size());
    }
}
