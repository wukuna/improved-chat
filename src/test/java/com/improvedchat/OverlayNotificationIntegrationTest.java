package com.improvedchat;

import com.improvedchat.model.OverlayMessage;
import com.improvedchat.overlay.OverlayConfig;
import com.improvedchat.overlay.OverlayFilterMode;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.Notification;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayNotificationIntegrationTest {
    private static class TestPlugin extends ImprovedChatPlugin {
        final List<String> alerts=new ArrayList<>();
        @Override public boolean isGameFilterEnabled() { return false; }
        @Override public boolean isBossKcFilterEnabled() { return false; }
        @Override protected void sendOverlayNotification(Notification notification,String message) { alerts.add(message); }
    }
    @SuppressWarnings("unchecked")
    @Test public void realOverlayFilteringAndCollapsedHistoryControlDispatch() throws Exception {
        TestPlugin plugin=new TestPlugin();
        set(plugin,"config",new ImprovedChatConfig() { @Override public boolean collapseDuplicates() { return true; } });
        set(plugin,"client",Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(proxy,method,args) -> {
            if (method.getName().equals("getGameState")) return GameState.LOGGED_IN;
            if (!method.getReturnType().isPrimitive()) return null;
            if (method.getReturnType()==boolean.class) return false;
            return 0;
        }));
        Field history=ImprovedChatPlugin.class.getDeclaredField("messages"); history.setAccessible(true);
        List<OverlayMessage> messages=(List<OverlayMessage>)history.get(plugin);
        OverlayConfig a=OverlayConfig.preset("Private","private"), b=OverlayConfig.preset("Other","private");
        a.setNotification(Notification.ON); b.setNotification(Notification.ON);
        b.setFilterMode(OverlayFilterMode.HIDE_MATCHES); b.setFilteredWords("hello");
        plugin.getOverlayConfigs().add(a); plugin.getOverlayConfigs().add(b);
        for (int i=0;i<2;i++) {
            OverlayMessage message=OverlayMessage.senderMessage("Alice",null,"hello",System.currentTimeMillis(),ChatMessageType.PRIVATECHAT,false);
            messages.add(message); plugin.notifyOverlays(message);
        }
        assertEquals(2,plugin.alerts.size()); assertTrue(plugin.alerts.get(0).startsWith("[Private]"));
        plugin.getMessagesForOverlay(a); plugin.getMessagesForOverlay(a); assertEquals(2,plugin.alerts.size());
        a.setShow(false);
        OverlayMessage next=OverlayMessage.senderMessage("Alice",null,"hello",System.currentTimeMillis(),ChatMessageType.PRIVATECHAT,false);
        messages.add(next); plugin.notifyOverlays(next); assertEquals(2,plugin.alerts.size());
    }
    private static void set(Object target,String name,Object value) throws Exception {
        Field field=ImprovedChatPlugin.class.getDeclaredField(name); field.setAccessible(true); field.set(target,value);
    }
}
