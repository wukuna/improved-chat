package com.improvedchat;

import java.lang.reflect.Proxy;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MessageNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PendingMessageUpdateGuardTest
{
    @Test
    public void commandRewriteIsDroppedWhenNodeGetsRecycled()
    {
        NodeState state = new NodeState(500, 8000, ChatMessageType.CLAN_CHAT, "!kc");
        MessageNode node = state.proxy();

        ImprovedChatPlugin.PendingMessageUpdate pending =
            new ImprovedChatPlugin.PendingMessageUpdate(
                null, node, MessageNode::getRuneLiteFormatMessage, "!kc", 10);

        state.runeLiteFormatMessage = "Kill count: 123";
        assertTrue(pending.stillRepresentsOriginalMessage());

        state.id = 501;
        state.timestamp = 8001;
        state.value = "new player's message";
        state.runeLiteFormatMessage = "new player's message";

        assertFalse(pending.stillRepresentsOriginalMessage());
    }

    @Test
    public void emojiRewriteIsDroppedWhenNodeGetsRecycled()
    {
        NodeState state = new NodeState(600, 9000, ChatMessageType.CLAN_CHAT, ":smile:");
        MessageNode node = state.proxy();

        ImprovedChatPlugin.PendingMessageUpdate pending =
            new ImprovedChatPlugin.PendingMessageUpdate(
                null, node,
                n -> n.getValue() == null ? null : n.getValue().trim(),
                ":smile:", 1);

        state.value = "<img=42>";
        assertTrue(pending.stillRepresentsOriginalMessage());

        state.id = 601;
        state.name = "Richard";
        state.value = "my next clan message";

        assertFalse(pending.stillRepresentsOriginalMessage());
    }

    private static final class NodeState
    {
        private int id;
        private int timestamp;
        private ChatMessageType type;
        private String name = "Alice";
        private String value;
        private String runeLiteFormatMessage;

        private NodeState(int id, int timestamp, ChatMessageType type, String value)
        {
            this.id = id;
            this.timestamp = timestamp;
            this.type = type;
            this.value = value;
            this.runeLiteFormatMessage = value;
        }

        private MessageNode proxy()
        {
            return (MessageNode) Proxy.newProxyInstance(
                MessageNode.class.getClassLoader(),
                new Class<?>[] {MessageNode.class},
                (proxy, method, args) ->
                {
                    switch (method.getName())
                    {
                        case "getId": return id;
                        case "getTimestamp": return timestamp;
                        case "getType": return type;
                        case "getName": return name;
                        case "setName": name = (String) args[0]; return null;
                        case "getValue": return value;
                        case "setValue": value = (String) args[0]; return null;
                        case "getRuneLiteFormatMessage": return runeLiteFormatMessage;
                        case "setRuneLiteFormatMessage": runeLiteFormatMessage = (String) args[0]; return null;
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        case "toString": return "MessageNodeProxy";
                        default:
                            Class<?> returnType = method.getReturnType();
                            if (returnType == boolean.class) return false;
                            if (returnType == int.class) return 0;
                            if (returnType == long.class) return 0L;
                            return null;
                    }
                });
        }
    }
}
