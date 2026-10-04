package com.improvedchat.model;

import java.lang.reflect.Proxy;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MessageNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MessageNodeIdentityTest
{
    @Test
    public void valueAndNameRewritesDoNotChangeLogicalIdentity()
    {
        NodeState state = new NodeState(101, 12345, ChatMessageType.CLAN_CHAT, "Alice", "hello");
        MessageNode node = state.proxy();
        MessageNodeIdentity identity = MessageNodeIdentity.capture(node);

        state.name = "<col=808080>Alice</col>";
        state.value = "<img=12>hello";

        assertTrue(identity.matches(node));
    }

    @Test
    public void recycledNodeIsRejectedWhenMessageIdChanges()
    {
        NodeState state = new NodeState(101, 12345, ChatMessageType.CLAN_CHAT, "Alice", "hello");
        MessageNode node = state.proxy();
        MessageNodeIdentity identity = MessageNodeIdentity.capture(node);

        state.id = 102;
        state.name = "Richard";
        state.value = "my clan message";

        assertFalse(identity.matches(node));
    }

    @Test
    public void recycledNodeIsRejectedWhenTimestampOrTypeChanges()
    {
        NodeState state = new NodeState(101, 12345, ChatMessageType.CLAN_CHAT, "Alice", "hello");
        MessageNode node = state.proxy();
        MessageNodeIdentity identity = MessageNodeIdentity.capture(node);

        state.timestamp = 12346;
        assertFalse(identity.matches(node));

        state.timestamp = 12345;
        state.type = ChatMessageType.CLAN_GUEST_CHAT;
        assertFalse(identity.matches(node));
    }

    private static final class NodeState
    {
        private int id;
        private int timestamp;
        private ChatMessageType type;
        private String name;
        private String value;

        private NodeState(int id, int timestamp, ChatMessageType type, String name, String value)
        {
            this.id = id;
            this.timestamp = timestamp;
            this.type = type;
            this.name = name;
            this.value = value;
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
