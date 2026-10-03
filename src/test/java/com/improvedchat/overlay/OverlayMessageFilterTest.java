package com.improvedchat.overlay;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.model.OverlayMessage;
import net.runelite.api.ChatMessageType;
import net.runelite.client.config.ConfigItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverlayMessageFilterTest
{
    @Test
    public void newOverlayDefaultsDoNotChangeExistingBehavior()
    {
        OverlayConfig config = new OverlayConfig();
        assertFalse(config.isUseGlobalChatFilter());
        assertEquals(OverlayFilterMode.OFF, config.getFilterMode());
        assertEquals("", config.getFilteredWords());
        assertEquals("", config.getFilteredRegex());
        assertEquals("", config.getFilteredNames());
    }

    @Test
    public void globalOverrideTakesPrecedence()
    {
        OverlayConfig config = new OverlayConfig();
        assertFalse(OverlayMessageFilter.usesGlobalChatFilter(false, config));

        config.setUseGlobalChatFilter(true);
        assertTrue(OverlayMessageFilter.usesGlobalChatFilter(false, config));

        config.setUseGlobalChatFilter(false);
        assertTrue(OverlayMessageFilter.usesGlobalChatFilter(true, config));
    }

    @Test
    public void hideMatchesSupportsWordsAndRegex()
    {
        OverlayConfig config = new OverlayConfig();
        config.setFilterMode(OverlayFilterMode.HIDE_MATCHES);
        config.setFilteredWords("buy gold, spam phrase");
        config.setFilteredRegex("^You have .* coins$");

        OverlayMessageFilter filter = new OverlayMessageFilter();
        assertTrue(filter.shouldExclude(config,
            OverlayMessage.gameMessage("BUY GOLD now", 0, ChatMessageType.GAMEMESSAGE, false)));
        assertTrue(filter.shouldExclude(config,
            OverlayMessage.gameMessage("You have 42 coins", 0, ChatMessageType.GAMEMESSAGE, false)));
        assertFalse(filter.shouldExclude(config,
            OverlayMessage.gameMessage("A normal message", 0, ChatMessageType.GAMEMESSAGE, false)));
    }

    @Test
    public void showOnlyMatchesInvertsTheDecision()
    {
        OverlayConfig config = new OverlayConfig();
        config.setFilterMode(OverlayFilterMode.SHOW_ONLY_MATCHES);
        config.setFilteredWords("collection log");

        OverlayMessageFilter filter = new OverlayMessageFilter();
        assertFalse(filter.shouldExclude(config,
            OverlayMessage.gameMessage("Collection log updated", 0, ChatMessageType.GAMEMESSAGE, false)));
        assertTrue(filter.shouldExclude(config,
            OverlayMessage.gameMessage("You eat the shark.", 0, ChatMessageType.GAMEMESSAGE, false)));
    }

    @Test
    public void senderRulesSupportLiteralAndRegexEntries()
    {
        OverlayConfig config = new OverlayConfig();
        config.setFilterMode(OverlayFilterMode.HIDE_MATCHES);
        config.setFilteredNames("Alice\nregex:^Bob[0-9]+$");

        OverlayMessageFilter filter = new OverlayMessageFilter();
        assertTrue(filter.shouldExclude(config,
            OverlayMessage.senderMessage("Alice", null, "hello", 0, ChatMessageType.PUBLICCHAT, false)));
        assertTrue(filter.shouldExclude(config,
            OverlayMessage.senderMessage("Bob42", null, "hello", 0, ChatMessageType.PUBLICCHAT, false)));
        assertFalse(filter.shouldExclude(config,
            OverlayMessage.senderMessage("Charlie", null, "hello", 0, ChatMessageType.PUBLICCHAT, false)));
    }

    @Test
    public void invalidRegexIsIgnoredWithoutBreakingFiltering()
    {
        OverlayConfig config = new OverlayConfig();
        config.setFilterMode(OverlayFilterMode.HIDE_MATCHES);
        config.setFilteredWords("valid");
        config.setFilteredRegex("[invalid");

        OverlayMessageFilter filter = new OverlayMessageFilter();
        assertTrue(filter.shouldExclude(config,
            OverlayMessage.gameMessage("valid message", 0, ChatMessageType.GAMEMESSAGE, false)));
        assertFalse(filter.shouldExclude(config,
            OverlayMessage.gameMessage("other message", 0, ChatMessageType.GAMEMESSAGE, false)));
    }

    @Test
    public void globalConfigKeepsExistingKeyButUsesNewLabel() throws Exception
    {
        ConfigItem item = ImprovedChatConfig.class.getMethod("useChatFilter").getAnnotation(ConfigItem.class);
        assertEquals("useChatFilter", item.keyName());
        assertEquals("Use Chat Filter Globally", item.name());
        assertEquals(
            "When enabled, overrides all overlay chat filters with the RuneLite global chat filter",
            item.description());
    }
}
