package com.improvedchat.compat;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModernChatCompatibilityTest
{
    @Test
    public void modernDefaultsAreConservative()
    {
        assertTrue(ModernChatCompatibility.readBoolean(null, true));
        assertFalse(ModernChatCompatibility.readBoolean(null, false));
        assertTrue(ModernChatCompatibility.readBoolean("true", false));
        assertFalse(ModernChatCompatibility.readBoolean("false", true));
    }

    @Test
    public void ownershipSeparatesRedesignAndToggle()
    {
        ModernChatOwnership inactive = new ModernChatOwnership(false, true, true);
        assertFalse(inactive.blocksGeometry());
        assertFalse(inactive.blocksVisibility());
        assertFalse(inactive.blocksNativePresentation());

        ModernChatOwnership toggleOnly = new ModernChatOwnership(true, false, true);
        assertFalse(toggleOnly.blocksGeometry());
        assertTrue(toggleOnly.blocksVisibility());
        assertFalse(toggleOnly.blocksNativePresentation());

        ModernChatOwnership redesign = new ModernChatOwnership(true, true, false);
        assertTrue(redesign.blocksGeometry());
        assertTrue(redesign.blocksVisibility());
        assertTrue(redesign.blocksNativePresentation());
    }
}
