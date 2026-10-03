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
