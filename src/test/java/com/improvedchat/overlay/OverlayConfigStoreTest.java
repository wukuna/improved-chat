package com.improvedchat.overlay;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayConfigStoreTest {
    private final Gson gson = new Gson();

    @Test public void deletedOverlaysStayDeletedAfterSavingAndReloading() {
        assertTrue(OverlayConfigStore.read(gson, "[]").isEmpty());
        assertTrue(OverlayConfigStore.read(gson, gson.toJson(OverlayConfigStore.read(gson, "[]"))).isEmpty());
        assertEquals(2, OverlayConfigStore.read(gson, null).size());
    }

    @Test(expected = JsonParseException.class) public void invalidJsonRequiresRecovery() {
        OverlayConfigStore.read(gson, "not json");
    }

    @Test(expected = JsonParseException.class) public void nullListRequiresRecovery() {
        OverlayConfigStore.read(gson, "null");
    }

    @Test(expected = JsonParseException.class) public void duplicateIdsRequireRecovery() {
        OverlayConfigStore.read(gson, "[{\"id\":\"same\"},{\"id\":\"same\"}]");
    }

    @Test public void persistedNumbersAreValidatedAndSelectionsPreserved() {
        OverlayConfig config = OverlayConfigStore.read(gson,
                "[{\"maxMessages\":-1,\"widgetWidth\":99999,\"name\":\"Saved\",\"messageTypes\":[\"PUBLICCHAT\"]}]").get(0);
        assertEquals(1, config.getMaxMessages());
        assertEquals(1024, config.getWidgetWidth());
        assertEquals("Saved", config.getName());
        assertTrue(config.getMessageTypes().contains(ChatMessageType.PUBLICCHAT));
    }

    @Test public void gamePresetsAgreeAndExcludeOrdinaryPlayerChat() {
        OverlayConfig initial = OverlayConfig.defaultAllOverlay();
        assertEquals(OverlayConfig.preset("Game Alerts", "game").getMessageTypes(), initial.getMessageTypes());
        assertTrue(initial.getMessageTypes().contains(ChatMessageType.GAMEMESSAGE));
        assertFalse(initial.getMessageTypes().contains(ChatMessageType.PUBLICCHAT));
        assertFalse(initial.getMessageTypes().contains(ChatMessageType.CLAN_CHAT));
    }
}
