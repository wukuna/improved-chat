package com.improvedchat.chatbox.clean;

import com.improvedchat.chatbox.clean.util.CleanChatUtil;
import com.improvedchat.chatbox.clean.util.FormatterExtractor;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FixedTimestampLayoutTest
{
    @Test
    public void clanPrefixWidgetsMoveByExactTimestampDelta()
    {
        WidgetState channelState = new WidgetState("[19:36] [Clan]", 0, 100, 0);
        WidgetState rankState = new WidgetState("", 66, 11, 0);
        WidgetState nameState = new WidgetState("Alice", 79, 40, 0);
        WidgetState messageState = new WidgetState("hello", 125, 300, 0);
        WidgetState clickState = new WidgetState("", 0, 425, 0);

        Widget channel = channelState.proxy();
        Widget rank = rankState.proxy();
        Widget name = nameState.proxy();
        Widget message = messageState.proxy();
        Widget click = clickState.proxy();
        Client client = clientProxy();

        ChatWidgetGroup group = new ChatWidgetGroup(channel, rank, name, message, click);
        FormatterExtractor.ExtractionResult template =
            FormatterExtractor.createFromFormatString("[HH:mm]");

        int actualWidth = CleanChatUtil.getTextLength("[19:36]", client);
        int targetWidth = actualWidth + 18;
        int oldMessageRight = messageState.x + messageState.width;
        int expectedDelta = targetWidth - actualWidth;
        int expectedSpaces = targetWidth / CleanChatUtil.getTextLength(" ", client);

        group.extractTimestamp(template, targetWidth, client);

        assertEquals(0, group.getTimestampX());
        assertEquals("timestamp-bearing channel widget must stay at the native X", 0, channelState.x);
        assertEquals(100 + expectedDelta, channelState.width);
        assertEquals(66 + expectedDelta, rankState.x);
        assertEquals(79 + expectedDelta, nameState.x);
        assertEquals(125 + expectedDelta, messageState.x);
        assertEquals(oldMessageRight, messageState.x + messageState.width);
        assertEquals(" ".repeat(expectedSpaces) + " [Clan]", channelState.text);
    }

    @Test
    public void gameMessageKeepsNativeOriginAndReservesFixedSlotWithSpaces()
    {
        WidgetState channelState = new WidgetState("", 0, 0, 0);
        WidgetState rankState = new WidgetState("", 0, 0, 0);
        WidgetState nameState = new WidgetState("", 0, 0, 0);
        WidgetState messageState = new WidgetState("[19:36] Game message", 8, 300, 0);
        WidgetState clickState = new WidgetState("", 0, 300, 0);

        Client client = clientProxy();
        ChatWidgetGroup group = new ChatWidgetGroup(
            channelState.proxy(), rankState.proxy(), nameState.proxy(), messageState.proxy(), clickState.proxy());

        FormatterExtractor.ExtractionResult template =
            FormatterExtractor.createFromFormatString("[HH:mm]");

        int targetWidth = 45;
        int expectedSpaces = targetWidth / CleanChatUtil.getTextLength(" ", client);
        int originalRight = messageState.x + messageState.width;

        group.extractTimestamp(template, targetWidth, client);

        assertEquals(8, group.getTimestampX());
        assertEquals("message widget origin must not move", 8, messageState.x);
        assertEquals(originalRight, messageState.x + messageState.width);
        assertEquals(" ".repeat(expectedSpaces) + " Game message", messageState.text);
    }

    private static Client clientProxy()
    {
        return (Client) Proxy.newProxyInstance(
            Client.class.getClassLoader(),
            new Class<?>[] {Client.class},
            (proxy, method, args) ->
            {
                if ("macroExpand".equals(method.getName()))
                {
                    return args[0];
                }
                Class<?> type = method.getReturnType();
                if (type == boolean.class) return false;
                if (type == int.class) return 0;
                if (type == long.class) return 0L;
                return null;
            });
    }

    private static final class WidgetState
    {
        private String text;
        private int x;
        private int width;
        private final int y;

        private WidgetState(String text, int x, int width, int y)
        {
            this.text = text;
            this.x = x;
            this.width = width;
            this.y = y;
        }

        private Widget proxy()
        {
            Map<String, Object> values = new HashMap<>();
            return (Widget) Proxy.newProxyInstance(
                Widget.class.getClassLoader(),
                new Class<?>[] {Widget.class},
                (proxy, method, args) ->
                {
                    switch (method.getName())
                    {
                        case "getText": return text;
                        case "setText": text = (String) args[0]; return proxy;
                        case "getOriginalX": return x;
                        case "setOriginalX": x = (Integer) args[0]; return proxy;
                        case "getOriginalWidth": return width;
                        case "setOriginalWidth": width = (Integer) args[0]; return proxy;
                        case "getCanvasLocation": return new Point(x, y);
                        case "revalidate": return null;
                        case "isHidden": return false;
                        case "getWidth": return width;
                        case "getHeight": return 14;
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        case "toString": return "WidgetProxy";
                        default:
                            Class<?> type = method.getReturnType();
                            if (type == boolean.class) return false;
                            if (type == int.class) return 0;
                            if (type == long.class) return 0L;
                            return values.get(method.getName());
                    }
                });
        }
    }
}
