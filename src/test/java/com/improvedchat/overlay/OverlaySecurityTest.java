package com.improvedchat.overlay;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.improvedchat.BoundedPattern;
import com.improvedchat.model.OverlayMessage;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlaySecurityTest
{
    @Test(timeout=3000) public void costlyShowOnlyRuleDoesNotBlankTheOverlay()
    {
        OverlayConfig config = new OverlayConfig();
        config.setFilterMode(OverlayFilterMode.SHOW_ONLY_MATCHES);
        config.setFilteredRegex("((a|aa)+)+$");
        OverlayMessageFilter filter = new OverlayMessageFilter();
        assertFalse(filter.shouldExclude(config, OverlayMessage.gameMessage(
            "a".repeat(32) + "!", 0, ChatMessageType.GAMEMESSAGE, false)));
        assertFalse(filter.shouldExclude(config, OverlayMessage.gameMessage(
            "another message", 0, ChatMessageType.GAMEMESSAGE, false)));
        assertFalse(filter.validationMessages(config).isEmpty());
        assertEquals("((a|aa)+)+$", config.getFilteredRegex());
        config.setFilteredRegex("^allowed$");
        assertTrue(filter.shouldExclude(config, OverlayMessage.gameMessage(
            "not allowed", 0, ChatMessageType.GAMEMESSAGE, false)));
    }

    @Test public void oversizedRuleSourceLeavesMessagesVisibleAndSettingsUntouched()
    {
        OverlayConfig config = new OverlayConfig();
        config.setFilterMode(OverlayFilterMode.SHOW_ONLY_MATCHES);
        String source = "a".repeat(BoundedPattern.MAX_SOURCE+1);
        config.setFilteredRegex(source);
        assertFalse(new OverlayMessageFilter().shouldExclude(config, OverlayMessage.gameMessage(
            "message", 0, ChatMessageType.GAMEMESSAGE, false)));
        assertEquals(source, config.getFilteredRegex());
    }

    @Test(expected=JsonParseException.class) public void rejectsOversizedConfigurationBeforeParsing()
    { OverlayConfigStore.read(new Gson(), " ".repeat(OverlayConfigStore.MAX_JSON+1)); }

    @Test(expected=JsonParseException.class) public void rejectsExcessiveOverlayCount()
    { OverlayConfigStore.read(new Gson(), "[" + "{},".repeat(64) + "{}]"); }

    @Test(expected=JsonParseException.class) public void rejectsDeepUnknownFields()
    { OverlayConfigStore.read(new Gson(), "[{\"unused\":" + "[".repeat(33) + "0" + "]".repeat(33) + "}]"); }

    @Test public void quotedBracesAndEscapesDoNotCountAsNesting()
    {
        OverlayConfig config = new OverlayConfig();
        config.setName("[{}]\\\"".repeat(40));
        Gson gson = new Gson();
        assertEquals(config.getName(), OverlayConfigStore.read(gson, gson.toJson(
            java.util.Collections.singletonList(config))).get(0).getName());
    }
}
