package com.improvedchat.chatbox.resize;

import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RuneLiteHudAnchorsTest {
    @Test
    public void activeHudFollowsActualTopLevelInterface() {
        assertEquals(
            InterfaceID.ToplevelPreEoc.HUD_CONTAINER_FRONT,
            RuneLiteHudAnchors.frontIdForTopLevel(InterfaceID.TOPLEVEL_PRE_EOC)
        );
        assertEquals(
            InterfaceID.ToplevelOsrsStretch.HUD_CONTAINER_FRONT,
            RuneLiteHudAnchors.frontIdForTopLevel(InterfaceID.TOPLEVEL_OSRS_STRETCH)
        );
    }
}
