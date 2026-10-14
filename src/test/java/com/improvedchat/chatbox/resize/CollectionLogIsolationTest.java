package com.improvedchat.chatbox.resize;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.HashTable;
import net.runelite.api.WidgetNode;
import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;
import static org.junit.Assert.*;

public class CollectionLogIsolationTest {
    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
    private WidgetNode node(int group) {
        return (WidgetNode)Proxy.newProxyInstance(WidgetNode.class.getClassLoader(), new Class[]{WidgetNode.class},
                (p,m,a) -> m.getName().equals("getId") ? group : defaultValue(m.getReturnType()));
    }
    @SuppressWarnings("unchecked")
    private Client client(int slot, WidgetNode mounted, AtomicInteger scripts, AtomicInteger widgetReads) {
        HashTable<WidgetNode> table = (HashTable<WidgetNode>)Proxy.newProxyInstance(HashTable.class.getClassLoader(), new Class[]{HashTable.class},
                (p,m,a) -> m.getName().equals("get") && ((Long)a[0]).intValue() == slot ? mounted : defaultValue(m.getReturnType()));
        return (Client)Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[]{Client.class}, (p,m,a) -> {
            switch (m.getName()) {
                case "getComponentTable": return table;
                case "getTopLevelInterfaceId": return InterfaceID.TOPLEVEL;
                case "getGameState": return GameState.LOGGED_IN;
                case "runScript": scripts.incrementAndGet(); return null;
                case "getWidget": widgetReads.incrementAndGet(); return null;
                default: return defaultValue(m.getReturnType());
            }
        });
    }
    @Test public void mountedCollectionLogIsNeverRebuiltOrRevalidatedAcrossLayouts() {
        int[] slots = {InterfaceID.Toplevel.MAINMODAL, InterfaceID.Toplevel.FLOATER,
            InterfaceID.ToplevelOsrsStretch.MAINMODAL, InterfaceID.ToplevelOsrsStretch.FLOATER,
            InterfaceID.ToplevelPreEoc.MAINMODAL, InterfaceID.ToplevelPreEoc.FLOATER};
        for (int slot : slots) {
            AtomicInteger scripts = new AtomicInteger(), reads = new AtomicInteger();
            TopLevelModals modals = new TopLevelModals(client(slot, node(InterfaceID.COLLECTION), scripts, reads));
            assertTrue(modals.isCollectionLogOpen());
            modals.relayout(); modals.relayout();
            assertEquals(0, scripts.get());
            assertEquals(0, reads.get());
        }
    }
    @Test public void otherMountedScreensRetainTheExistingNativeRelayout() {
        AtomicInteger scripts = new AtomicInteger(), reads = new AtomicInteger();
        TopLevelModals modals = new TopLevelModals(client(InterfaceID.Toplevel.MAINMODAL, node(InterfaceID.BANKMAIN), scripts, reads));
        assertFalse(modals.isCollectionLogOpen());
        modals.relayout();
        assertEquals(1, scripts.get());
        assertTrue(reads.get() > 0);
    }
}
