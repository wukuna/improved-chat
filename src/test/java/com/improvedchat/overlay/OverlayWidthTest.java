package com.improvedchat.overlay;

import java.awt.Dimension;
import java.lang.reflect.Proxy;
import net.runelite.api.Client;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayWidthTest {
    @Test public void editorWidthReplacesDraggedWidthAndPreservesHeight() {
        Client client = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[] {Client.class},
                (p, m, a) -> m.getReturnType() == boolean.class ? false : null);
        OverlayConfig config = new OverlayConfig();
        DynamicChatOverlay overlay = new DynamicChatOverlay(null, null, client, null, null, config);
        overlay.setPreferredSize(new Dimension(700, 140));
        assertEquals(700, overlay.getEffectiveWidth());
        overlay.setWidth(350);
        assertEquals(350, overlay.getEffectiveWidth());
        assertEquals(new Dimension(350, 140), overlay.getPreferredSize());
        assertEquals(350, config.getWidgetWidth());
    }
}
