package com.improvedchat.chatbox.offline;

import com.improvedchat.model.MessageNodeIdentity;
import java.util.Objects;
import net.runelite.api.MessageNode;

/**
 * Tracks the unmodified sender name for one logical clan chat message.
 *
 * MessageNode objects can be recycled. The identity snapshot prevents a name saved for an older
 * message from ever being written onto a newer message which happens to reuse the same node object.
 */
final class ClanNameState
{
    private final MessageNodeIdentity identity;
    private String originalName;
    private String lastAppliedName;

    private ClanNameState(MessageNodeIdentity identity, String originalName)
    {
        this.identity = identity;
        this.originalName = normalize(originalName);
        this.lastAppliedName = this.originalName;
    }

    static ClanNameState capture(MessageNode node)
    {
        return new ClanNameState(MessageNodeIdentity.capture(node), node == null ? "" : node.getName());
    }

    boolean matches(MessageNode node)
    {
        return identity != null && identity.matches(node);
    }

    String baseName(String currentName)
    {
        String current = normalize(currentName);

        // Respect another plugin changing the name on the same logical message. Treat its new value
        // as the baseline rather than restoring our older copy over it.
        if (!Objects.equals(current, lastAppliedName) && !Objects.equals(current, originalName))
        {
            originalName = current;
            lastAppliedName = current;
        }

        return originalName;
    }

    void recordApplied(String value)
    {
        lastAppliedName = normalize(value);
    }

    boolean shouldRestore(MessageNode node)
    {
        return matches(node)
            && Objects.equals(normalize(node.getName()), lastAppliedName)
            && !Objects.equals(originalName, lastAppliedName);
    }

    String getOriginalName()
    {
        return originalName;
    }

    private static String normalize(String value)
    {
        return value == null ? "" : value;
    }
}
