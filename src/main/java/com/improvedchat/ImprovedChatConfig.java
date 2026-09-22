package com.improvedchat;

import com.improvedchat.dialogue.FontChoice;
import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("improvedchat")
public interface ImprovedChatConfig extends Config {
    String GROUP = "improvedchat";
    @ConfigSection(
        name = "General",
        description = "Global Improved Chat settings",
        position = 0,
        closedByDefault = false
    )
    String appearanceSection = "appearance";

    @ConfigSection(
        name = "Chat Colors",
        description = "Baseline RuneLite chat colors",
        position = 1,
        closedByDefault = false
    )
    String coloursSection = "colours";

    @ConfigSection(
        name = "Message Color Rules",
        description = "Rules applied to the chatbox and overlays",
        position = 2,
        closedByDefault = true
    )
    String messageRulesSection = "messageRules";

    @ConfigSection(
        name = "Overlay Color Rules",
        description = "Overlay-only color rules",
        position = 3,
        closedByDefault = true
    )
    String overlayRulesSection = "overlayRules";

    @ConfigItem(
        keyName = "hideSidePanel",
        name = "Hide Improved Chat Panel",
        description = "Hide the Improved Chat side panel",
        position = -1
    )
    default boolean hideSidePanel() { return false; }

    @ConfigItem(keyName = "textShadow", name = "Text Shadow", description = "Draw shadow behind text", section = appearanceSection, position = 1)
    default boolean textShadow() { return true; }

    @ConfigItem(keyName = "wrapText", name = "Wrap Text", description = "Wrap long messages to multiple lines", section = appearanceSection, position = 2)
    default boolean wrapText() { return true; }

    @ConfigItem(keyName = "hidePrivateChat", name = "Hide Default Private Chat", description = "Hide RuneLite's default split private chat", section = appearanceSection, position = 3)
    default boolean hidePrivateChat() { return true; }

    @ConfigItem(keyName = "smartPositioning", name = "Smart Positioning", description = "Automatically reposition free overlays based on client mode and chatbox state", section = appearanceSection, position = 4)
    default boolean smartPositioning() { return true; }

    @ConfigItem(keyName = "collapseDuplicates", name = "Collapse Duplicates", description = "Merge consecutive identical messages into one with a count", section = appearanceSection, position = 5)
    default boolean collapseDuplicates() { return false; }

    @ConfigItem(keyName = "showChannelName", name = "Show Channel Names", description = "Show the channel name prefix for friends and clan chat messages", section = appearanceSection, position = 6)
    default boolean showChannelName() { return true; }

    @ConfigItem(keyName = "useChatFilter", name = "Use Chat Filter", description = "Hide/censor messages using RuneLite's Chat Filter plugin's word & regex lists and Filter Type", section = appearanceSection, position = 7)
    default boolean useChatFilter() { return false; }

    @ConfigItem(keyName = "timestampFormat", name = "Timestamp Format", description = "Timestamp format used by overlays", section = appearanceSection, position = 8)
    default String timestampFormat() { return "[HH:mm]"; }

    @ConfigItem(keyName = "chatColorSource", name = "Chat Color Source", description = "Baseline color source", section = coloursSection, position = 0)
    default ChatColorSource chatColorSource() { return ChatColorSource.AUTOMATIC; }

    @ConfigItem(keyName = "gameColour", name = "Game", description = "Fallback text colour for game messages", section = coloursSection, position = 1)
    default Color gameColour() { return Color.WHITE; }

    @ConfigItem(keyName = "publicColour", name = "Public", description = "Fallback text colour for public chat messages", section = coloursSection, position = 2)
    default Color publicColour() { return new Color(148, 148, 255); }

    @ConfigItem(keyName = "privateColour", name = "Private", description = "Fallback text colour for private messages", section = coloursSection, position = 3)
    default Color privateColour() { return new Color(0, 255, 255); }

    @ConfigItem(keyName = "friendsColour", name = "Friends", description = "Fallback text colour for friends chat messages", section = coloursSection, position = 4)
    default Color friendsColour() { return new Color(239, 80, 80); }

    @ConfigItem(keyName = "clanColour", name = "Clan", description = "Fallback text colour for clan chat messages", section = coloursSection, position = 5)
    default Color clanColour() { return new Color(127, 0, 0); }

    @ConfigItem(keyName = "guestClanColour", name = "Guest Clan", description = "Fallback text colour for guest clan chat messages", section = coloursSection, position = 6)
    default Color guestClanColour() { return new Color(0, 211, 0); }

    @ConfigItem(keyName = "gimClanColour", name = "GIM Clan", description = "Fallback text colour for Group Ironman clan chat messages", section = coloursSection, position = 7)
    default Color gimClanColour() { return new Color(127, 0, 0); }

    @ConfigItem(keyName = "tradeColour", name = "Trade", description = "Fallback text colour for trade messages", section = coloursSection, position = 8)
    default Color tradeColour() { return new Color(223, 32, 255); }

    @ConfigItem(keyName = "challengeColour", name = "Challenge", description = "Fallback text colour for challenge request messages", section = coloursSection, position = 9)
    default Color challengeColour() { return new Color(255, 32, 223); }

    @ConfigItem(keyName = "didYouKnowColour", name = "Did You Know?", description = "Fallback text colour for Did You Know messages", section = coloursSection, position = 10)
    default Color didYouKnowColour() { return new Color(255, 255, 0); }

    @ConfigItem(keyName = "broadcastColour", name = "Broadcast", description = "Fallback text colour for broadcast messages", section = coloursSection, position = 11)
    default Color broadcastColour() { return Color.WHITE; }

    @ConfigItem(keyName = "autoColour", name = "Autochat", description = "Fallback text colour for autochat messages", section = coloursSection, position = 12)
    default Color autoColour() { return new Color(64, 64, 255); }

    @ConfigItem(keyName = "enableSolidRecolor", name = "Enable Message Color Rules", description = "Apply matching colors to chatbox and overlays", section = messageRulesSection, position = 0)
    default boolean enableMessageColorRules() { return false; }

    @ConfigItem(keyName = "solidRecolorRules", name = "Message Color Rules", description = "One rule per line: text::1 through text::9; append ::flash to flash matching overlays", section = messageRulesSection, position = 1)
    default String messageColorRules() {
        return "is about to expire::1\nhas expired::1\nachieved a new::2\nhas reached::4";
    }

    @ConfigItem(keyName = "recolorPlayerChat", name = "Include Player Chat", description = "Allow rules to match player-authored chat", section = messageRulesSection, position = 2)
    default boolean includePlayerChatInColorRules() { return false; }

    @ConfigItem(keyName = "solidColor1", name = "Rule Color 1", description = "Message rule palette color 1", section = messageRulesSection, position = 10)
    default Color messageRuleColor1() { return new Color(239, 16, 32); }
    @ConfigItem(keyName = "solidColor2", name = "Rule Color 2", description = "Message rule palette color 2", section = messageRulesSection, position = 11)
    default Color messageRuleColor2() { return new Color(255, 149, 0); }
    @ConfigItem(keyName = "solidColor3", name = "Rule Color 3", description = "Message rule palette color 3", section = messageRulesSection, position = 12)
    default Color messageRuleColor3() { return new Color(255, 214, 10); }
    @ConfigItem(keyName = "solidColor4", name = "Rule Color 4", description = "Message rule palette color 4", section = messageRulesSection, position = 13)
    default Color messageRuleColor4() { return new Color(52, 199, 89); }
    @ConfigItem(keyName = "solidColor5", name = "Rule Color 5", description = "Message rule palette color 5", section = messageRulesSection, position = 14)
    default Color messageRuleColor5() { return new Color(50, 215, 255); }
    @ConfigItem(keyName = "solidColor6", name = "Rule Color 6", description = "Message rule palette color 6", section = messageRulesSection, position = 15)
    default Color messageRuleColor6() { return new Color(10, 132, 255); }
    @ConfigItem(keyName = "solidColor7", name = "Rule Color 7", description = "Message rule palette color 7", section = messageRulesSection, position = 16)
    default Color messageRuleColor7() { return new Color(94, 92, 230); }
    @ConfigItem(keyName = "solidColor8", name = "Rule Color 8", description = "Message rule palette color 8", section = messageRulesSection, position = 17)
    default Color messageRuleColor8() { return new Color(191, 90, 242); }
    @ConfigItem(keyName = "solidColor9", name = "Rule Color 9", description = "Message rule palette color 9", section = messageRulesSection, position = 18)
    default Color messageRuleColor9() { return Color.WHITE; }

    @ConfigItem(keyName = "enableRainbowRecolor", name = "Enable Overlay Rainbow Rules", description = "Apply rainbow styling to overlays only", section = overlayRulesSection, position = 0)
    default boolean enableOverlayRainbowRules() { return false; }

    @ConfigItem(keyName = "rainbowPatterns", name = "Overlay Rainbow Rules", description = "One pattern per line; prefix regex: for regular expressions", section = overlayRulesSection, position = 1)
    default String overlayRainbowRules() {
        return "achieved a new\nhas reached\nYou have stopped moving!";
    }

    @ConfigItem(keyName = "rainbowStyle", name = "Rainbow Style", description = "Color by word or visible character", section = overlayRulesSection, position = 2)
    default RainbowStyle rainbowStyle() { return RainbowStyle.PER_WORD; }
    @ConfigSection(
        name = "Collapsible Chat",
        description = "Collapse RuneLite chat to a single customizable button",
        position = 10,
        closedByDefault = true
    )
    String collapseChatSection = "collapseChat";

    @ConfigSection(
        name = "Chatbox Opacity",
        description = "Fine tune native transparent chatbox and button opacity",
        position = 11,
        closedByDefault = true
    )
    String chatboxOpacitySection = "chatboxOpacity";

    @ConfigSection(
        name = "Dialogue Text Styling",
        description = "Customize supported dialogue text appearance and readability",
        position = 12,
        closedByDefault = true
    )
    String dialogueFontsSection = "dialogueFonts";

    enum CollapsedButtonContent {
        STATIC_TEXT("Static text"),
        REPORT_BUTTON_TEXT("Report button text");

        private final String label;

        CollapsedButtonContent(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @ConfigItem(keyName = "enableCollapsibleChat", name = "Enable Collapsible Chat", description = "Collapse native chat tabs to one button when chat is hidden", section = collapseChatSection, position = 0)
    default boolean enableCollapsibleChat() { return false; }

    @ConfigItem(keyName = "collapsedButtonContent", name = "Button Content", description = "Content shown on the single collapsed chat button", section = collapseChatSection, position = 1)
    default CollapsedButtonContent collapsedButtonContent() { return CollapsedButtonContent.STATIC_TEXT; }

    @ConfigItem(keyName = "collapsedButtonTransparent", name = "Transparent Button", description = "Make the collapsed button transparent", section = collapseChatSection, position = 2)
    default boolean collapsedButtonTransparent() { return false; }

    @ConfigItem(keyName = "collapsedButtonText", name = "Button Text", description = "Text shown while chat is collapsed", section = collapseChatSection, position = 3)
    default String collapsedButtonContentCustomText() { return "-"; }

    @ConfigItem(keyName = "collapsedButtonHoverText", name = "Hover Text", description = "Text shown while hovering the collapsed button", section = collapseChatSection, position = 4)
    default String collapsedButtonContentCustomTextHovered() { return "+"; }

    @ConfigItem(keyName = "collapseUnreadPublic", name = "Unread Public", description = "Highlight the collapsed button for unread public messages", section = collapseChatSection, position = 10)
    default boolean highlightOnUnreadPublicMessages() { return false; }

    @ConfigItem(keyName = "collapseUnreadPrivate", name = "Unread Private", description = "Highlight the collapsed button for unread private messages", section = collapseChatSection, position = 11)
    default boolean highlightOnUnreadPrivateMessages() { return false; }

    @ConfigItem(keyName = "collapseUnreadFriends", name = "Unread Friends Chat", description = "Highlight the collapsed button for unread friends chat messages", section = collapseChatSection, position = 12)
    default boolean highlightOnUnreadFriendsChatMessages() { return false; }

    @ConfigItem(keyName = "collapseUnreadClan", name = "Unread Clan", description = "Highlight the collapsed button for unread clan messages", section = collapseChatSection, position = 13)
    default boolean highlightOnUnreadClanChatMessages() { return false; }

    @ConfigItem(keyName = "collapseUnreadTrade", name = "Unread Trade", description = "Highlight the collapsed button for unread trade messages", section = collapseChatSection, position = 14)
    default boolean highlightOnUnreadTradeMessages() { return false; }

    @ConfigItem(keyName = "enableChatboxOpacity", name = "Enable Chatbox Opacity", description = "Enable transparent-chat background and button opacity controls", section = chatboxOpacitySection, position = 0)
    default boolean enableChatboxOpacity() { return false; }

    @Range(min = -1, max = 255)
    @ConfigItem(keyName = "chatboxOpacity", name = "Chatbox Opacity", description = "-1 keeps RuneLite default; 0 is opaque and 255 is fully transparent", section = chatboxOpacitySection, position = 1)
    default int chatboxOpacity() { return 150; }

    @Range(min = -1, max = 255)
    @ConfigItem(keyName = "buttonOpacity", name = "Button Opacity", description = "-1 keeps RuneLite default; 0 is opaque and 255 is fully transparent", section = chatboxOpacitySection, position = 2)
    default int buttonOpacity() { return -1; }

    @ConfigItem(keyName = "opacityDialogueMenus", name = "Dialogue & Menus", description = "Apply chatbox opacity to dialogue boxes and option menus shown in the chat area", section = chatboxOpacitySection, position = 3)
    default boolean opacityDialogueMenus() { return true; }

    @ConfigItem(keyName = "enableDialogueFonts", name = "Enable Dialogue Text Styling", description = "Customize supported dialogue text with configurable fonts and readability controls", section = dialogueFontsSection, position = 0)
    default boolean enableDialogueFonts() { return false; }

    @ConfigItem(keyName = "fontFamily", name = "Font", description = "Font used for dialogue text", section = dialogueFontsSection, position = 1)
    default FontChoice fontFamily() { return FontChoice.SANS_SERIF; }

    @Range(min = 10, max = 24)
    @ConfigItem(keyName = "fontSize", name = "Font Size", description = "Dialogue text size in pixels", section = dialogueFontsSection, position = 2)
    default int fontSize() { return 14; }

    @ConfigItem(keyName = "boldText", name = "Bold", description = "Use bold dialogue text", section = dialogueFontsSection, position = 3)
    default boolean boldText() { return false; }

    @ConfigItem(keyName = "antiAlias", name = "Anti-aliasing", description = "Smooth dialogue font edges", section = dialogueFontsSection, position = 4)
    default boolean antiAlias() { return true; }

    @Range(min = 10, max = 24)
    @ConfigItem(keyName = "dialogueOptionFontSize", name = "Option Font Size", description = "Separate font size for dialogue option rows", section = dialogueFontsSection, position = 5)
    default int dialogueOptionFontSize() { return 13; }

    @Range(min = -2, max = 8)
    @Units(Units.PIXELS)
    @ConfigItem(keyName = "dialogueLineSpacing", name = "Line Spacing", description = "Extra spacing between wrapped dialogue lines", section = dialogueFontsSection, position = 6)
    default int dialogueLineSpacing() { return 0; }

    @ConfigItem(keyName = "dialogueTextShadow", name = "Text Shadow", description = "Draw a subtle shadow behind replacement dialogue text", section = dialogueFontsSection, position = 7)
    default boolean dialogueTextShadow() { return false; }

    @Alpha
    @ConfigItem(keyName = "dialogueShadowColor", name = "Shadow Color", description = "Color and opacity of the optional dialogue text shadow", section = dialogueFontsSection, position = 8)
    default Color dialogueShadowColor() { return new Color(0, 0, 0, 150); }

    @ConfigItem(keyName = "replaceNpc", name = "NPC Dialogue", description = "Replace NPC dialogue text", section = dialogueFontsSection, position = 10)
    default boolean replaceNpc() { return true; }

    @ConfigItem(keyName = "replacePlayer", name = "Player Dialogue", description = "Replace player dialogue text", section = dialogueFontsSection, position = 11)
    default boolean replacePlayer() { return true; }

    @ConfigItem(keyName = "replaceOptions", name = "Option Menus", description = "Replace dialogue option-menu text while preserving 1–5 keyboard selection", section = dialogueFontsSection, position = 12)
    default boolean replaceOptions() { return true; }

    @ConfigItem(keyName = "replaceSprite", name = "Item/Action Dialogue", description = "Replace item and action dialogue text", section = dialogueFontsSection, position = 13)
    default boolean replaceSprite() { return true; }

}
