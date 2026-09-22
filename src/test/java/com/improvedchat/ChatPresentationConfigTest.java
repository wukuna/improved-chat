package com.improvedchat;

import java.lang.reflect.Method;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChatPresentationConfigTest {
    private final ImprovedChatConfig config = new ImprovedChatConfig() {};

    @Test
    public void configMethodsAreRuneLiteConfigItems() {
        for (Method method : ImprovedChatConfig.class.getDeclaredMethods()) {
            assertTrue(method.isSynthetic() || method.getAnnotation(ConfigItem.class) != null);
        }
    }

    @Test
    public void presentationFeaturesAreOptIn() {
        assertFalse(config.enableCollapsibleChat());
        assertFalse(config.enableChatboxOpacity());
        assertFalse(config.enableDialogueFonts());
    }

    @Test
    public void collapseDefaultsAreStable() {
        assertEquals(ImprovedChatConfig.CollapsedButtonContent.STATIC_TEXT, config.collapsedButtonContent());
        assertEquals("-", config.collapsedButtonContentCustomText());
        assertEquals("+", config.collapsedButtonContentCustomTextHovered());
        assertFalse(config.collapsedButtonTransparent());
    }

    @Test
    public void opacityDefaultsPreserveDialogueBehavior() {
        assertEquals(150, config.chatboxOpacity());
        assertEquals(-1, config.buttonOpacity());
        assertTrue(config.opacityDialogueMenus());
    }

    @Test
    public void dialogueSectionUsesCurrentName() throws Exception {
        ConfigSection section = ImprovedChatConfig.class
            .getField("dialogueFontsSection")
            .getAnnotation(ConfigSection.class);
        assertEquals("Dialogue Text Styling", section.name());
    }
}
