package com.improvedchat.overlay;

import com.google.gson.Gson;
import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.MessageColorRuleEngine;
import com.improvedchat.model.AttentionTrigger;
import com.improvedchat.model.OverlayMessage;
import java.util.Collections;
import net.runelite.api.ChatMessageType;
import net.runelite.client.config.Notification;
import net.runelite.client.config.NotificationSound;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayNotificationsTest {
    @After public void clearRules() { MessageColorRuleEngine.configure(null); }
    private OverlayMessage message(ChatMessageType type, boolean outgoing) {
        return OverlayMessage.senderMessage("Alice", null, "<col=ff0000>hello</col>", System.currentTimeMillis(), type, outgoing);
    }
    @Test public void oldConfigsRemainQuietAndExplicitNullIsSafe() {
        OverlayConfig legacy=OverlayConfigStore.read(new Gson(), "[{\"name\":\"Old\"}]").get(0);
        assertFalse(legacy.getNotification().isEnabled());
        legacy.setNotification(null); assertFalse(legacy.getNotification().isEnabled());
    }
    @Test public void onlyShownAndSelectedMessagesAreEligible() {
        OverlayConfig config=new OverlayConfig(); OverlayMessage message=message(ChatMessageType.PUBLICCHAT,false);
        assertFalse(OverlayNotifications.eligible(config,message,Collections.singletonList(message)));
        config.setNotification(Notification.ON);
        assertTrue(OverlayNotifications.eligible(config,message,Collections.singletonList(message)));
        assertFalse(OverlayNotifications.eligible(config,message,Collections.emptyList()));
        config.setShow(false); assertFalse(OverlayNotifications.eligible(config,message,Collections.singletonList(message)));
    }
    @Test public void outgoingAndNotifierConsoleMessagesCannotLoop() {
        OverlayConfig config=new OverlayConfig(); config.getTextSettings().setSolidEnabled(true); config.getTextSettings().setSolidRules("expire::1::flash"); config.setNotification(Notification.ON);
        OverlayMessage outgoing=message(ChatMessageType.PRIVATECHATOUT,true), console=message(ChatMessageType.CONSOLE,false);
        assertFalse(OverlayNotifications.eligible(config,outgoing,Collections.singletonList(outgoing)));
        assertFalse(OverlayNotifications.eligible(config,console,Collections.singletonList(console)));
    }
    @Test public void flashRuleTriggerIsIndependentOfOverlayFlashing() {
        MessageColorRuleEngine.configure(new ImprovedChatConfig() {
            @Override public boolean enableMessageColorRules() { return true; }
            @Override public String messageColorRules() { return "expire::1::flash"; }
        });
        OverlayConfig config=new OverlayConfig(); config.getTextSettings().setSolidEnabled(true); config.getTextSettings().setSolidRules("expire::1::flash"); config.setNotification(Notification.ON);
        config.setNotificationTrigger(AttentionTrigger.FLASH_RULES_ONLY);
        OverlayMessage hit=OverlayMessage.gameMessage("Your boost will expire",0,ChatMessageType.GAMEMESSAGE,false);
        assertTrue(OverlayNotifications.eligible(config,hit,Collections.singletonList(hit)));
        assertFalse(config.isFlashEnabled());
        OverlayMessage miss=message(ChatMessageType.PUBLICCHAT,false);
        assertFalse(OverlayNotifications.eligible(config,miss,Collections.singletonList(miss)));
    }
    @Test public void configurationRoundTripsAndCopiesStayIndependent() {
        Gson gson=new Gson(); OverlayConfig original=new OverlayConfig();
        original.setNotification(Notification.ON.withInitialized(true).withOverride(true).withSound(NotificationSound.CUSTOM).withSoundName("clan.wav").withVolume(35).withTray(true));
        OverlayConfig copy=OverlayConfigStore.read(gson,"["+gson.toJson(original)+"]").get(0); copy.renewId();
        assertTrue(copy.getNotification().isOverride()); assertEquals("clan.wav",copy.getNotification().getSoundName());
        assertEquals(35,copy.getNotification().getVolume()); copy.setNotification(copy.getNotification().withVolume(10));
        assertEquals(35,original.getNotification().getVolume()); assertNotEquals(original.getId(),copy.getId());
    }
    @Test public void invalidSavedValuesAndFilePathsAreNormalized() {
        OverlayConfig config=new OverlayConfig();
        config.setNotification(Notification.ON.withVolume(999).withTimeout(-1).withSoundName("../outside.wav"));
        assertEquals(100,config.getNotification().getVolume()); assertEquals(0,config.getNotification().getTimeout());
        assertNull(config.getNotification().getSoundName()); assertNotNull(config.getNotification().getRequestFocus());
    }
    @Test public void notificationTextIsPlainAndBounded() {
        OverlayConfig config=new OverlayConfig(); config.setName("Clan");
        assertEquals("[Clan] Alice: hello",OverlayNotifications.message(config,message(ChatMessageType.CLAN_CHAT,false)));
        OverlayMessage longMessage=OverlayMessage.gameMessage(String.join("",Collections.nCopies(1000,"a")),0,ChatMessageType.GAMEMESSAGE,false);
        assertEquals(500,OverlayNotifications.message(config,longMessage).length());
    }
}
