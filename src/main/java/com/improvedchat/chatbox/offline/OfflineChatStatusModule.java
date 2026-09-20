package com.improvedchat.chatbox.offline;

import com.improvedchat.ImprovedChatConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.IndexedSprite;
import net.runelite.api.IterableHashTable;
import net.runelite.api.MessageNode;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanID;
import net.runelite.api.events.ClanMemberJoined;
import net.runelite.api.events.ClanMemberLeft;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

/**
 * Improved Chat offline-clan status marker. The status glyph is generated internally and
 * original message-node names are retained verbatim for clean restoration.
 */
@Singleton
public final class OfflineChatStatusModule {
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ImprovedChatConfig config;
    @Inject private EventBus eventBus;

    private final Map<MessageNode, String> originalNames = new IdentityHashMap<>();
    private boolean started;
    private int iconIndex = -1;
    private String iconTag;

    public synchronized void startUp() {
        if (started || !config.enableOfflineChatStatus()) return;
        started = true;
        eventBus.register(this);
        clientThread.invoke(() -> {
            ensureIcon();
            formatAll();
        });
    }

    public synchronized void shutDown() {
        if (!started) return;
        started = false;
        eventBus.unregister(this);
        clientThread.invoke(this::restoreAll);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!ImprovedChatConfig.GROUP.equals(event.getGroup())) return;
        if ("enableOfflineIcon".equals(event.getKey())
            || "enableOfflineColor".equals(event.getKey())
            || "offlineColor".equals(event.getKey())) {
            clientThread.invokeLater(this::formatAll);
        }
    }

    @Subscribe
    public void onClanMemberJoined(ClanMemberJoined event) {
        clientThread.invokeLater(this::formatAll);
    }

    @Subscribe
    public void onClanMemberLeft(ClanMemberLeft event) {
        clientThread.invokeLater(this::formatAll);
    }

    private void ensureIcon() {
        if (iconIndex != -1) return;
        IndexedSprite[] existing = client.getModIcons();
        if (existing == null) return;

        BufferedImage image = new BufferedImage(11, 11, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(150, 150, 150, 235));
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(1, 1, 8, 8);
            g.drawLine(3, 3, 8, 8);
        } finally {
            g.dispose();
        }

        IndexedSprite marker = ImageUtil.getImageIndexedSprite(image, client);
        iconIndex = existing.length;
        IndexedSprite[] updated = Arrays.copyOf(existing, existing.length + 1);
        updated[iconIndex] = marker;
        client.setModIcons(updated);
        iconTag = "<img=" + iconIndex + ">";
    }

    private void formatAll() {
        if (!started) return;
        ensureIcon();
        IterableHashTable<MessageNode> messages = client.getMessages();
        if (messages == null) return;

        boolean changed = false;
        for (MessageNode message : messages) changed |= format(message);
        if (changed) client.refreshChat();
    }

    private boolean format(MessageNode message) {
        ChatMessageType type = message.getType();
        if (type != ChatMessageType.CLAN_CHAT && type != ChatMessageType.CLAN_GUEST_CHAT) return false;

        originalNames.putIfAbsent(message, message.getName());
        String base = originalNames.get(message);
        if (base == null) base = "";

        String cleanRsn = Text.standardize(Text.removeTags(base));
        boolean online = isOnline(cleanRsn, type);
        String result = base;

        if (!online) {
            if (config.enableOfflineColor()) {
                Color c = config.offlineColor();
                result = String.format("<col=%02x%02x%02x>%s</col>", c.getRed(), c.getGreen(), c.getBlue(), result);
            }
            if (config.enableOfflineIcon() && iconTag != null) result = iconTag + result;
        }

        if (!result.equals(message.getName())) {
            message.setName(result);
            return true;
        }
        return false;
    }

    private boolean isOnline(String standardizedName, ChatMessageType type) {
        ClanChannel channel = type == ChatMessageType.CLAN_GUEST_CHAT
            ? client.getGuestClanChannel()
            : client.getClanChannel(ClanID.CLAN);
        if (channel == null) return false;
        for (ClanChannelMember member : channel.getMembers()) {
            if (Text.standardize(member.getName()).equals(standardizedName)) return true;
        }
        return false;
    }

    private void restoreAll() {
        boolean changed = false;
        for (Map.Entry<MessageNode, String> entry : originalNames.entrySet()) {
            try {
                if (!entry.getValue().equals(entry.getKey().getName())) {
                    entry.getKey().setName(entry.getValue());
                    changed = true;
                }
            } catch (Exception ignored) {
                // Message nodes can expire while the plugin is enabled.
            }
        }
        originalNames.clear();
        if (changed) client.refreshChat();
    }
}
