package com.improvedchat;

import com.improvedchat.chatbox.resize.internal.SizeClamps;
import com.improvedchat.model.MessageCategory;
import com.improvedchat.overlay.OverlayConfig;
import java.lang.reflect.Method;
import net.runelite.api.ChatMessageType;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChatUiConfigTest {
    private final ImprovedChatConfig config = new ImprovedChatConfig() {};

    @Test
    public void configInterfaceContainsOnlyConfigItems() {
        for (Method method : ImprovedChatConfig.class.getDeclaredMethods()) {
            assertTrue(
                "ImprovedChatConfig method must be annotated @ConfigItem: " + method.getName(),
                method.isSynthetic() || method.getAnnotation(ConfigItem.class) != null
            );
        }
    }

    @Test
    public void consolidatedChatboxFeaturesAreOptIn() {
        assertFalse(config.enableCollapsibleChat());
        assertFalse(config.enableResizableChat());
        assertFalse(config.enableChatboxOpacity());
        assertFalse(config.enableRemoveChatOptions());
        assertFalse(config.removeLookupChatOption());
        assertFalse(config.enableOfflineChatStatus());
        assertFalse(config.enableDialogueFonts());
    }

    @Test
    public void defaultPrivateOverlayStartsDisabledAndContainsNoGameTypes() {
        OverlayConfig privateOverlay = OverlayConfig.defaultPrivateOverlay();

        assertFalse(privateOverlay.isShow());
        assertEquals(
            new java.util.HashSet<>(MessageCategory.PRIVATE.getTypes()),
            new java.util.HashSet<>(privateOverlay.getMessageTypes())
        );
        for (ChatMessageType type : MessageCategory.GAME.getTypes()) {
            assertFalse("Private default must not select GAME type " + type, privateOverlay.getMessageTypes().contains(type));
        }
        for (ChatMessageType type : MessageCategory.GAME_CLAN.getTypes()) {
            assertFalse("Private default must not select GAME (Clan) type " + type, privateOverlay.getMessageTypes().contains(type));
        }
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
    public void showHideKeybindIsInResizableChatSection() throws Exception {
        ConfigItem item = ImprovedChatConfig.class
            .getMethod("toggleShowChat")
            .getAnnotation(ConfigItem.class);

        assertEquals(ImprovedChatConfig.resizeChatSection, item.section());
        assertEquals(6, item.position());
    }

    @Test
    public void companionFeatureDefaultsAreSafe() {
        assertFalse(config.removeWelcome());
        assertFalse(config.hideSpecs());
        assertFalse(config.removeClanInstruction());
        assertFalse(config.removeGuestClanInstruction());
        assertFalse(config.removeGroupIronInstruction());
        assertFalse(config.removeGroupIronFromClan());
        assertFalse(config.removeFriendsChatStartup());
        assertEquals(150, config.chatboxOpacity());
        assertEquals(-1, config.buttonOpacity());
        assertTrue(config.opacityDialogueMenus());
        assertTrue(config.enableOfflineIcon());
        assertTrue(config.enableOfflineColor());
        assertEquals(14, config.fontSize());
        assertEquals(13, config.dialogueOptionFontSize());
        assertEquals(0, config.dialogueLineSpacing());
        assertFalse(config.boldText());
        assertTrue(config.antiAlias());
        assertFalse(config.dialogueTextShadow());
        assertTrue(config.replaceNpc());
        assertTrue(config.replacePlayer());
        assertTrue(config.replaceOptions());
        assertTrue(config.replaceSprite());
    }

    @Test
    public void dialogueSectionUsesTextStylingName() throws Exception {
        ConfigSection section = ImprovedChatConfig.class
            .getField("dialogueFontsSection")
            .getAnnotation(ConfigSection.class);

        assertEquals("Dialogue Text Styling", section.name());
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
