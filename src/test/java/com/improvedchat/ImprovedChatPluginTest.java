package com.improvedchat;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ImprovedChatPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(ImprovedChatPlugin.class);
        RuneLite.main(args);
    }
}
