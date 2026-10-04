package com.improvedchat.overlay;

import java.awt.Color;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverlayClanDisplayConfigTest
{
    @Test
    public void clanRankIconsAreShownByDefault()
    {
        OverlayConfig config = new OverlayConfig();
        assertTrue(config.isShowClanRankIcons());
    }

    @Test
    public void offlinePresentationIsOptInPerOverlay()
    {
        OverlayConfig config = new OverlayConfig();

        assertFalse(config.isShowOfflineStatus());
        assertTrue(config.isShowOfflineIcon());
        assertTrue(config.isColorOfflineNames());
        assertEquals(Color.DARK_GRAY, config.getOfflineColor());
    }

    @Test
    public void iconTagDetectionMatchesOnlyTheRequestedIcon()
    {
        String sender = "<img=17><img=42>Alice";

        assertTrue(DynamicChatOverlay.containsIconTag(sender, 17));
        assertTrue(DynamicChatOverlay.containsIconTag(sender, 42));
        assertFalse(DynamicChatOverlay.containsIconTag(sender, 9));
        assertFalse(DynamicChatOverlay.containsIconTag(null, 17));
        assertFalse(DynamicChatOverlay.containsIconTag(sender, -1));
    }

    @Test
    public void offlineIconDoesNotSuppressDifferentClanRankIcon()
    {
        String senderAlreadyContainingOfflineMarker = "<img=42>Alice";

        assertTrue(DynamicChatOverlay.containsIconTag(senderAlreadyContainingOfflineMarker, 42));
        assertFalse(DynamicChatOverlay.containsIconTag(senderAlreadyContainingOfflineMarker, 17));
    }

    @Test
    public void overlayOfflineSettingsAreIndependent()
    {
        OverlayConfig first = new OverlayConfig();
        OverlayConfig second = new OverlayConfig();

        first.setShowOfflineStatus(true);
        first.setShowOfflineIcon(false);
        first.setColorOfflineNames(true);
        first.setOfflineColor(Color.GRAY);

        assertTrue(first.isShowOfflineStatus());
        assertFalse(first.isShowOfflineIcon());
        assertEquals(Color.GRAY, first.getOfflineColor());

        assertFalse(second.isShowOfflineStatus());
        assertTrue(second.isShowOfflineIcon());
        assertEquals(Color.DARK_GRAY, second.getOfflineColor());
    }
}
