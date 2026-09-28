package com.improvedchat.chatbox.collapse;

import com.improvedchat.ImprovedChatConfig;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import com.improvedchat.chatbox.resize.ChatResizeModule;
import com.improvedchat.chatbox.resize.internal.RawScripts;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import java.awt.Canvas;
import java.awt.event.KeyEvent;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.HotkeyListener;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ChatCollapsePresentationTest {
    @Test
    public void keybindIsListedUnderCollapseAndKeepsExistingSavedKey() throws Exception {
        ConfigItem item = ImprovedChatConfig.class.getMethod("toggleShowChat").getAnnotation(ConfigItem.class);
        assertEquals("Collapse Chat Keybind", item.name());
        assertEquals(ImprovedChatConfig.collapseChatSection, item.section());
        assertEquals("toggleShowChat", item.keyName());
    }

    @Test
    public void keybindClosesAndReopensSelectedTabWithoutResizing() throws Exception {
        Fixture f = new Fixture(false);
        ChatCollapseModule module = f.module(new ImprovedChatConfig() {});
        f.tab = 3;
        module.toggleChat();
        assertArrayEquals(new Object[] {RawScripts.CHAT_TAB_CLICKED, 1, 3}, f.lastScript);
        f.tab = RawScripts.COLLAPSED_TAB;
        module.toggleChat();
        assertArrayEquals(new Object[] {RawScripts.CHAT_TAB_CLICKED, 1, 3}, f.lastScript);
        assertEquals(2, f.scriptCalls);
        f.gameState = GameState.LOGIN_SCREEN;
        module.toggleChat();
        assertEquals(2, f.scriptCalls);
        f.gameState = GameState.LOGGED_IN;
        inject(module, "started", false);
        module.toggleChat();
        assertEquals(2, f.scriptCalls);
    }

    @Test
    public void keybindDelegatesToResizeControllerWhenBothFeaturesAreEnabled() throws Exception {
        Fixture f = new Fixture(false);
        ImprovedChatConfig config = new ImprovedChatConfig() {
            @Override public boolean enableResizableChat() { return true; }
            @Override public boolean enableCollapsibleChat() { return true; }
            @Override public Keybind toggleShowChat() { return new Keybind(KeyEvent.VK_BACK_QUOTE, 0); }
        };
        ChatCollapseModule module = f.module(config);
        int[] calls = {0};
        ChatResizeModule resize = new ChatResizeModule() {
            @Override public void toggleChat() { calls[0]++; }
        };
        ClientThread immediate = new ClientThread() {
            @Override public void invoke(Runnable action) { action.run(); }
        };
        inject(module, "chatResizeModule", resize);
        inject(module, "clientThread", immediate);
        inject(resize, "clientThread", immediate);
        inject(resize, "config", config);
        KeyEvent press = new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_QUOTE, '`') {
            @Override public int getExtendedKeyCode() { return KeyEvent.VK_BACK_QUOTE; }
        };
        hotkey(ChatResizeModule.class, resize, "hideChatHotkey").keyPressed(press);
        assertFalse("Resize listener must not consume the collapse binding", press.isConsumed());
        hotkey(ChatCollapseModule.class, module, "collapseHotkey").keyPressed(press);
        assertTrue(press.isConsumed());
        assertEquals(1, calls[0]);
        assertEquals(0, f.scriptCalls);
    }

    private static HotkeyListener hotkey(Class<?> type, Object target, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return (HotkeyListener) field.get(target);
    }

    @Test
    public void collapseLeavesOnlyAllButtonAndExpansionRestoresBar() throws Exception {
        Fixture f = new Fixture(false);
        f.manager.updateChatWidgets(f.collapsed());
        assertTrue(f.background.hidden);
        assertFalse(f.widgets.get(ChatButton.ALL.containerID).hidden);
        for (int id : ChatButton.CONTAINER_IDS_TO_TOGGLE) assertTrue(f.widgets.get(id).hidden);
        ChatState expanded = new ChatState();
        expanded.collapseState = ChatCollapseState.EXPANDED;
        expanded.selectedChatButton = ChatButton.ALL;
        f.manager.updateChatWidgets(expanded);
        assertFalse(f.background.hidden);
        for (int id : ChatButton.CONTAINER_IDS_TO_TOGGLE) assertFalse(f.widgets.get(id).hidden);
    }

    @Test
    public void resetUsedByShutdownRestoresOriginalVisibility() throws Exception {
        for (boolean originallyHidden : new boolean[] {false, true}) {
            Fixture f = new Fixture(originallyHidden);
            ChatState state = f.collapsed();
            f.manager.updateChatWidgets(state);
            state.reset();
            f.manager.updateChatWidgets(state);
            assertEquals(originallyHidden, f.background.hidden);
        }
    }

    @Test
    public void rebuiltWidgetsAreHiddenWithoutLosingRestoreState() throws Exception {
        Fixture f = new Fixture(false);
        ChatState state = f.collapsed();
        f.manager.updateChatWidgets(state);
        FakeWidget old = f.background;
        FakeWidget replacement = new FakeWidget();
        f.widgets.put(InterfaceID.Chatbox.CONTROLS_BACKGROUND_GRAPHIC, replacement);
        f.manager.updateChatWidgets(state);
        assertFalse(old.hidden);
        assertTrue(replacement.hidden);
        f.widgets.remove(InterfaceID.Chatbox.CONTROLS_BACKGROUND_GRAPHIC);
        f.manager.updateChatWidgets(state);
        assertFalse(replacement.hidden);
    }

    @Test
    public void collapseRunsAfterLayoutUpdatesAndDoesNotRecaptureHiddenState() throws Exception {
        Fixture f = new Fixture(false);
        ChatCollapseModule module = new ChatCollapseModule();
        inject(module, "client", f.client);
        inject(module, "widgetManager", f.manager);
        inject(module, "started", true);
        EventBus bus = new EventBus();
        bus.register(module);
        bus.register(new Object() {
            @Subscribe public void onBeforeRender(BeforeRender event) {
                f.background.hidden = false;
                for (int id : ChatButton.CONTAINER_IDS_TO_TOGGLE) f.widgets.get(id).hidden = false;
            }
        });
        for (int i = 0; i < 3; i++) {
            bus.post(new BeforeRender());
            assertTrue(f.background.hidden);
            assertFalse(f.widgets.get(ChatButton.ALL.containerID).hidden);
            for (int id : ChatButton.CONTAINER_IDS_TO_TOGGLE) assertTrue(f.widgets.get(id).hidden);
        }
        f.manager.updateChatWidgets(new ChatState());
        assertFalse(f.background.hidden);
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Class<?> type = target.getClass();
        while (type.isAnonymousClass()) type = type.getSuperclass();
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class Fixture {
        final Map<Integer, FakeWidget> widgets = new HashMap<>();
        final FakeWidget background = new FakeWidget();
        final ChatWidgetManager manager = new ChatWidgetManager();
        int tab;
        int scriptCalls;
        Object[] lastScript;
        GameState gameState = GameState.LOGGED_IN;
        final Client client = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[] {Client.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getGameState": return gameState;
                    case "isResized": return true;
                    case "getVarcIntValue": return tab;
                    case "runScript": lastScript = (Object[]) args[0]; scriptCalls++; return null;
                }
                if (method.getName().equals("getWidget") && args.length == 1) {
                    FakeWidget widget = widgets.get((Integer) args[0]);
                    return widget == null ? null : widget.widget;
                }
                return defaultValue(method.getReturnType());
            });

        Fixture(boolean hidden) throws Exception {
            background.hidden = hidden;
            widgets.put(InterfaceID.Chatbox.CONTROLS_BACKGROUND_GRAPHIC, background);
            for (ChatButton button : ChatButton.values()) {
                widgets.put(button.containerID, new FakeWidget());
                widgets.put(button.graphicID, new FakeWidget());
                widgets.put(button.textID, new FakeWidget());
            }
            inject(manager, "client", client);
            inject(manager, "config", new ImprovedChatConfig() {});
        }

        ChatState collapsed() {
            ChatState state = new ChatState();
            state.collapseState = ChatCollapseState.COLLAPSED;
            return state;
        }

        ChatCollapseModule module(ImprovedChatConfig config) throws Exception {
            ChatCollapseModule module = new ChatCollapseModule();
            inject(module, "client", client);
            inject(module, "config", config);
            inject(module, "started", true);
            return module;
        }
    }

    private static final class FakeWidget {
        boolean hidden;
        int sprite = SpriteID.ChatTabButton.BUTTON;
        String text = "All";
        final Widget widget = (Widget) Proxy.newProxyInstance(Widget.class.getClassLoader(), new Class<?>[] {Widget.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setHidden": hidden = (Boolean) args[0]; break;
                    case "isHidden": case "isSelfHidden": return hidden;
                    case "getSpriteId": return sprite;
                    case "setSpriteId": sprite = (Integer) args[0]; break;
                    case "getText": return text;
                    case "setText": text = (String) args[0]; break;
                }
                return method.getReturnType() == Widget.class ? proxy : defaultValue(method.getReturnType());
            });
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        return null;
    }
}
