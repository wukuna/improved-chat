package com.improvedchat;

import java.lang.reflect.Method;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Part3ConfigTest
{
    private final ImprovedChatConfig config = new ImprovedChatConfig() {};

    @Test
    public void configMethodsRemainRuneLiteConfigItems()
    {
        for (Method method : ImprovedChatConfig.class.getDeclaredMethods())
        {
            assertTrue(method.isSynthetic() || method.getAnnotation(ConfigItem.class) != null);
        }
    }

    @Test
    public void cleanupAndMenuDefaultsAreSafe()
    {
        assertFalse(config.removeWelcome());
        assertFalse(config.hideScrollbar());
        assertFalse(config.hideSpecs());
        assertFalse(config.isFixedWidthTimestampEnabled());
        assertFalse(config.isColorBarEnabled());
        assertFalse(config.removeClanInstruction());
        assertFalse(config.removeGuestClanInstruction());
        assertFalse(config.removeGroupIronInstruction());
        assertFalse(config.removeFriendsChatStartup());
        assertFalse(config.enableRemoveChatOptions());
        assertFalse(config.removeLookupChatOption());
        assertFalse(config.enableOfflineChatStatus());
    }

    @Test
    public void offlineSubOptionsStayBehindTheMasterToggle()
    {
        assertTrue(config.enableOfflineIcon());
        assertTrue(config.enableOfflineColor());
        assertFalse(config.enableOfflineChatStatus());
    }

    @Test
    public void part2CollapseKeybindPlacementIsPreserved() throws Exception
    {
        ConfigItem item = ImprovedChatConfig.class.getMethod("toggleShowChat").getAnnotation(ConfigItem.class);
        assertEquals(ImprovedChatConfig.collapseChatSection, item.section());
        assertEquals(15, item.position());
    }

    @Test
    public void dialogueStylingStillExistsAfterPart3Sections() throws Exception
    {
        ConfigSection section = ImprovedChatConfig.class.getField("dialogueFontsSection").getAnnotation(ConfigSection.class);
        assertEquals("Dialogue Text Styling", section.name());
        assertEquals(23, section.position());
    }
}
