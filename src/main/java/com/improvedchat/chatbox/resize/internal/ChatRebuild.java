/* Derived from Chat Resizer by shanktank under BSD 2-Clause. See THIRD_PARTY_NOTICES.md. */
package com.improvedchat.chatbox.resize.internal;

import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;

// Runs a chat-rebuild script so it takes effect on the current frame
public final class ChatRebuild {
    private ChatRebuild() {}

    // Backdating the stamp defeats the chat builder's once-per-cycle coalescing, otherwise it would swallow the rebuild
    public static void now(Client client, int rebuildScript) {
        if (client.getWidget(InterfaceID.Chatbox.SCROLLAREA) == null) return; // Chatbox not live (login/hop)
        client.setVarcIntValue(VarClientID.CHAT_LASTREBUILD, client.getGameCycle() - 1);
        client.runScript(rebuildScript);
    }
}