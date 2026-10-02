package com.improvedchat.chatbox.clean;

import com.improvedchat.ImprovedChatConfig;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CleanChatModuleConfigTest
{
    @Test
    public void allDefaultsLeaveCleanupInactive()
    {
        ImprovedChatConfig config = new ImprovedChatConfig() {};
        assertFalse(CleanChatModule.hasActiveCleanupSettings(config));
    }

    @Test
    public void oneCleanupSettingActivatesCleanup()
    {
        ImprovedChatConfig config = new ImprovedChatConfig()
        {
            @Override
            public boolean removeWelcome()
            {
                return true;
            }
        };
        assertTrue(CleanChatModule.hasActiveCleanupSettings(config));
    }
}
