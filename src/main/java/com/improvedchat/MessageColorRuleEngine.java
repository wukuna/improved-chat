package com.improvedchat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;

/**
 * Message Color Rules shared by RuneLite chat and Improved Chat overlays.
 * Color precedence: RuneLite Chat Color -> Message Color Rules -> overlay-only override.
 */
public final class MessageColorRuleEngine {
    private static volatile ImprovedChatConfig config;
    private static volatile String cachedSource;
    private static volatile List<SolidRule> cachedRules = new ArrayList<>();

    private MessageColorRuleEngine() {}

    public static void configure(ImprovedChatConfig newConfig) {
        config = newConfig;
        cachedSource = null;
        cachedRules = new ArrayList<>();
    }

    /** Returns the Message Color Rules color for a message, or null when this layer does not apply. */
    public static Color colorFor(String rawMessage, ChatMessageType type) {
        ImprovedChatConfig c = config;
        if (c == null || rawMessage == null || rawMessage.isEmpty() || !c.enableMessageColorRules()) return null;
        if (!c.includePlayerChatInColorRules() && isPlayerChat(type)) return null;

        String plain = plainText(rawMessage);
        if (plain.isEmpty()) return null;
        for (SolidRule rule : rules(c.messageColorRules())) {
            if (rule.rule.matches(plain)) return colorForGroup(c, rule.group);
        }
        return null;
    }

    /** True when a matching Message Color Rule includes the optional ::flash modifier. */
    public static boolean flashFor(String rawMessage, ChatMessageType type) {
        ImprovedChatConfig c = config;
        if (c == null || rawMessage == null || rawMessage.isEmpty() || !c.enableMessageColorRules()) return false;
        if (!c.includePlayerChatInColorRules() && isPlayerChat(type)) return false;
        String plain = plainText(rawMessage);
        if (plain.isEmpty()) return false;
        for (SolidRule rule : rules(c.messageColorRules())) {
            if (rule.flash && rule.rule.matches(plain)) return true;
        }
        return false;
    }

    /**
     * Applies Message Color Rules to the live RuneLite chatbox. The Improved Chat capture body is deliberately
     * left untouched so filtering, merging, emoji reconciliation, and duplicate detection still operate
     * on the original semantic message. The renderer independently calls {@link #colorFor}.
     */
    public static void applyToChatbox(Client client, ChatMessage event, ImprovedChatConfig c, String message) {
        if (client == null || event == null || c == null || message == null || message.isEmpty()) return;
        // Keep the static renderer view synchronized even during config hot reloads.
        if (config != c) configure(c);

        ChatMessageType type = event.getType();
        Color color = colorFor(message, type);
        if (color == null) return;

        String expanded = message;
        try {
            if (expanded.indexOf('@') >= 0) {
                String m = client.macroExpand(expanded);
                if (m != null && !m.isEmpty()) expanded = m;
            }
        } catch (RuntimeException ignored) {
            // Keep original message if macro expansion changes unexpectedly.
        }

        String recolored = forceColor(expanded, color);
        MessageNode node = event.getMessageNode();
        if (node == null) return;
        try {
            node.setValue(recolored);
            node.setRuneLiteFormatMessage(recolored);
            client.refreshChat();
        } catch (RuntimeException ignored) {
            // Chat rendering must never fail because another plugin rewrote the node first.
        }
    }

    private static String forceColor(String message, Color color) {
        String prefix = "";
        String body = message;
        if (body.startsWith("CA_ID:")) {
            int pipe = body.indexOf('|');
            if (pipe >= 0 && pipe + 1 < body.length()) {
                prefix = body.substring(0, pipe + 1);
                body = body.substring(pipe + 1);
            }
        }

        // Force means force: remove only color-control tags so embedded colors cannot beat this layer.
        body = body.replaceAll("(?i)</?col(?:=[0-9a-f]{6}|NORMAL|HIGHLIGHT)?[^>]*>", "");
        String hex = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        return prefix + "<col=" + hex + ">" + body + "</col>";
    }

    public static String plainText(String text) {
        if (text == null) return "";
        String out = text;
        if (out.startsWith("CA_ID:")) {
            int pipe = out.indexOf('|');
            if (pipe >= 0 && pipe + 1 < out.length()) out = out.substring(pipe + 1);
        }
        return out.replaceAll("(?i)<(?!lt>|gt>|at>)[^>]+>", "")
                .replace("<lt>", "<")
                .replace("<gt>", ">")
                .replace("<at>", "@")
                .replace('\u00a0', ' ')
                .trim();
    }

    private static boolean isPlayerChat(ChatMessageType type) {
        return type == ChatMessageType.PRIVATECHAT
            || type == ChatMessageType.PRIVATECHATOUT
            || type == ChatMessageType.MODPRIVATECHAT
            || type == ChatMessageType.PUBLICCHAT
            || type == ChatMessageType.MODCHAT
            || type == ChatMessageType.AUTOTYPER
            || type == ChatMessageType.MODAUTOTYPER
            || type == ChatMessageType.FRIENDSCHAT
            || type == ChatMessageType.CLAN_CHAT
            || type == ChatMessageType.CLAN_GUEST_CHAT
            || type == ChatMessageType.CLAN_GIM_CHAT;
    }

    private static Color colorForGroup(ImprovedChatConfig c, int group) {
        switch (group) {
            case 1: return c.messageRuleColor1();
            case 2: return c.messageRuleColor2();
            case 3: return c.messageRuleColor3();
            case 4: return c.messageRuleColor4();
            case 5: return c.messageRuleColor5();
            case 6: return c.messageRuleColor6();
            case 7: return c.messageRuleColor7();
            case 8: return c.messageRuleColor8();
            case 9: return c.messageRuleColor9();
            default: return null;
        }
    }

    private static List<SolidRule> rules(String source) {
        if (source == null) source = "";
        if (source.equals(cachedSource)) return cachedRules;
        synchronized (MessageColorRuleEngine.class) {
            if (source.equals(cachedSource)) return cachedRules;
            List<SolidRule> out = new ArrayList<>();
            for (String line : source.split("\\R")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                boolean flash = false;
                if (line.length() >= 7 && line.regionMatches(true, line.length() - 7, "::flash", 0, 7)) {
                    flash = true;
                    line = line.substring(0, line.length() - 7).trim();
                }
                int sep = line.lastIndexOf("::");
                if (sep <= 0 || sep + 2 >= line.length()) continue;
                String expression = line.substring(0, sep).trim();
                try {
                    int group = Integer.parseInt(line.substring(sep + 2).trim());
                    if (group < 1 || group > 9 || expression.isEmpty()) continue;
                    TextRule rule = TextRule.compile(expression);
                    if (rule != null) out.add(new SolidRule(rule, group, flash));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed rules instead of affecting chat stability.
                }
            }
            cachedRules = out;
            cachedSource = source;
            return out;
        }
    }

    private static final class SolidRule {
        final TextRule rule;
        final int group;
        final boolean flash;
        SolidRule(TextRule rule, int group, boolean flash) { this.rule = rule; this.group = group; this.flash = flash; }
    }

    private static final class TextRule {
        final String literalLower;
        final Pattern regex;
        private TextRule(String literalLower, Pattern regex) { this.literalLower = literalLower; this.regex = regex; }

        static TextRule compile(String expression) {
            if (expression.regionMatches(true, 0, "regex:", 0, 6)) {
                String pattern = expression.substring(6).trim();
                if (pattern.isEmpty()) return null;
                try {
                    return new TextRule(null, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
                } catch (PatternSyntaxException ignored) { return null; }
            }
            return new TextRule(expression.toLowerCase(Locale.ROOT), null);
        }

        boolean matches(String text) {
            return regex != null ? regex.matcher(text).find() : text.toLowerCase(Locale.ROOT).contains(literalLower);
        }
    }
}
