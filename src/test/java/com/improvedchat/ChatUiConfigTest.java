package com.improvedchat;

import com.improvedchat.chatbox.resize.internal.SizeClamps;
import net.runelite.client.config.Keybind;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChatUiConfigTest {
    private final ImprovedChatConfig config = new ImprovedChatConfig() {};

    @Test
    public void consolidatedChatboxFeaturesAreOptIn() {
        assertFalse(config.enableCollapsibleChat());
        assertFalse(config.enableResizableChat());
        assertFalse(config.modernizeChat());
    }

    @Test
    public void collapseDefaultsPreserveSingleButtonBehavior() {
        assertEquals(ImprovedChatConfig.CollapsedButtonContent.STATIC_TEXT, config.collapsedButtonContent());
        assertEquals("-", config.collapsedButtonContentCustomText());
        assertEquals("+", config.collapsedButtonContentCustomTextHovered());
        assertFalse(config.collapsedButtonTransparent());
        assertFalse(config.highlightOnUnreadPublicMessages());
        assertFalse(config.highlightOnUnreadPrivateMessages());
        assertFalse(config.highlightOnUnreadFriendsChatMessages());
        assertFalse(config.highlightOnUnreadClanChatMessages());
        assertFalse(config.highlightOnUnreadTradeMessages());
    }

    @Test
    public void resizeDefaultsRetainSourceFeatureSet() {
        assertEquals(28, config.heightChange());
        assertEquals(80, config.widthChange());
        assertEquals(0, config.fixedHeightChange());
        assertTrue(config.rewrapPrivateChat());
        assertTrue(config.growInterfaces());
        assertTrue(config.fixedTabCollapse());
        assertTrue(config.liveRewrap());
        assertEquals(ImprovedChatConfig.Revert.BOTH, config.revertForDialogs());
        assertEquals(ImprovedChatConfig.Revert.UNGROW, config.revertForModals());
        assertEquals(ImprovedChatConfig.Mode.HOLD, config.secondaryMode());
        assertEquals(Keybind.NOT_SET, config.toggleShowChat());
        assertEquals(Keybind.NOT_SET, config.dragModifier());
        assertEquals(Keybind.NOT_SET, config.secondaryKeybind());
    }

    @Test
    public void resizeClampHonorsDialogAndModalReversion() {
        assertEquals(25, SizeClamps.clamp(25, false, false, false, config));
        assertEquals(0, SizeClamps.clamp(25, false, false, true, config));
        assertEquals(0, SizeClamps.clamp(-25, false, false, true, config));
        assertEquals(0, SizeClamps.clamp(25, false, true, false, config));
        assertEquals(-25, SizeClamps.clamp(-25, false, true, false, config));
    }
}
