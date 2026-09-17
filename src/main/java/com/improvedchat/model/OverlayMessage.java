package com.improvedchat.model;

import net.runelite.api.ChatMessageType;

public final class OverlayMessage {
    private final String message;
    private final long timestamp;
    private final ChatMessageType type;
    private final boolean bossKc;
    private final String sender;
    private final String channelName;
    private final boolean outgoing;
    private final int maxFadeSeconds;
    private int count = 1;

    public static OverlayMessage gameMessage(String message, long timestamp, ChatMessageType type, boolean bossKc) {
        return new OverlayMessage(message, timestamp, type, bossKc, null, null, false, 0);
    }

    public static OverlayMessage senderMessage(String sender, String channelName, String message,
            long timestamp, ChatMessageType type, boolean outgoing) {
        return new OverlayMessage(message, timestamp, type, false, sender, channelName, outgoing, 0);
    }

    public static OverlayMessage loginNotification(String sender, String message, long timestamp, int maxFadeSeconds) {
        return new OverlayMessage(message, timestamp, ChatMessageType.LOGINLOGOUTNOTIFICATION,
                false, sender, null, false, maxFadeSeconds);
    }

    private OverlayMessage(String message, long timestamp, ChatMessageType type, boolean bossKc,
            String sender, String channelName, boolean outgoing, int maxFadeSeconds) {
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.bossKc = bossKc;
        this.sender = sender;
        this.channelName = channelName;
        this.outgoing = outgoing;
        this.maxFadeSeconds = maxFadeSeconds;
    }

    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public ChatMessageType getType() { return type; }
    public boolean isBossKc() { return bossKc; }
    public String getSender() { return sender; }
    public String getChannelName() { return channelName; }
    public boolean isOutgoing() { return outgoing; }
    public int getCount() { return count; }
    public void incrementCount() { count++; }
    public void setCount(int count) { this.count = count; }
    public int getMaxFadeSeconds() { return maxFadeSeconds; }
}
