package com.improvedchat;

import com.improvedchat.chatbox.clean.data.IndentMode;
import com.improvedchat.dialogue.FontChoice;
import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.config.Alpha;

@ConfigGroup(ImprovedChatConfig.GROUP)
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
        name = "Resizable Chat",
        description = "Resize RuneLite chat in resizable and fixed layouts",
        position = 11,
        closedByDefault = true
    )
    String resizeChatSection = "resizeChat";

    @ConfigSection(
        name = "Drag Resizing",
        description = "Resize chat by dragging its border",
        position = 12,
        closedByDefault = true
    )
    String dragResizeSection = "dragResize";

    @ConfigSection(
        name = "Secondary Chat Size",
        description = "Swap to a second chat size with a keybind",
        position = 13,
        closedByDefault = true
    )
    String secondarySizeSection = "secondarySize";

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

    enum Revert {
        UNGROW, UNSHRINK, BOTH, NEITHER;

        public boolean ungrows() {
            return this == UNGROW || this == BOTH;
        }

        public boolean unshrinks() {
            return this == UNSHRINK || this == BOTH;
        }
    }

    enum Mode {
        HOLD, TOGGLE
    }

    String HEIGHT_CHANGE = "heightChange";
    String WIDTH_CHANGE = "widthChange";
    String REWRAP_PRIVATE_CHAT = "rewrapPrivateChat";
    String RESIZE_TAB_BUTTONS = "resizeTabButtons";
    String GROW_INTERFACES = "growInterfaces";
    String FIXED_HEIGHT_CHANGE = "fixedHeightChange";
    String FIXED_TAB_COLLAPSE = "fixedTabCollapse";
    String FIXED_ADJUST_VIEWPORT = "fixedAdjustViewport";
    String REVERT_FOR_DIALOGS = "revertForDialogs";
    String REVERT_FOR_MODALS = "revertForModals";
    String TOGGLE_SHOW_CHAT = "toggleShowChat";
    String INDICATOR_COLOR = "indicatorColor";
    String DRAG_MODIFIER = "dragModifier";
    String LIVE_REWRAP = "liveRewrap";
    String SWAP_HEIGHT_CHANGE = "swapHeightChange";
    String SWAP_WIDTH_CHANGE = "swapWidthChange";
    String SWAP_SIZE_KEYBIND = "swapSizeKeybind";
    String SWAP_SIZE_MODE = "swapSizeMode";
    String NO_BORDERS = "noBorders";
    String NO_BACKGROUND_ZOOM = "noBackgroundZoom";

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

    @ConfigItem(keyName = "enableResizableChat", name = "Enable Resizable Chat", description = "Enable advanced native chat resizing", section = resizeChatSection, position = 0)
    default boolean enableResizableChat() { return false; }

    @Range(min = -165, max = 10000)
    @Units(Units.PIXELS)
    @ConfigItem(keyName = HEIGHT_CHANGE, name = "Resizable Height Change", description = "Add or subtract native chat height in resizable layout", section = resizeChatSection, position = 1)
    default int heightChange() { return 28; }

    @Range(min = -519, max = 10000)
    @Units(Units.PIXELS)
    @ConfigItem(keyName = WIDTH_CHANGE, name = "Resizable Width Change", description = "Add or subtract native chat width in resizable layout", section = resizeChatSection, position = 2)
    default int widthChange() { return 80; }

    @ConfigItem(keyName = REWRAP_PRIVATE_CHAT, name = "Adjust Private Split Width", description = "Match split-private-message width to resized chat", section = resizeChatSection, position = 3)
    default boolean rewrapPrivateChat() { return true; }

    @ConfigItem(keyName = RESIZE_TAB_BUTTONS, name = "Resize Chat Tab Buttons", description = "Stretch chat tabs to match adjusted width", section = resizeChatSection, position = 4)
    default boolean resizeTabButtons() { return false; }

    @ConfigItem(keyName = GROW_INTERFACES, name = "Grow Interface Height", description = "Let interfaces reclaim space freed by a smaller chatbox", section = resizeChatSection, position = 5)
    default boolean growInterfaces() { return true; }

    @Range(min = -165, max = 10000)
    @Units(Units.PIXELS)
    @ConfigItem(keyName = FIXED_HEIGHT_CHANGE, name = "Fixed Height Change", description = "Add or subtract native chat height in fixed layout", section = resizeChatSection, position = 10)
    default int fixedHeightChange() { return 0; }

    @ConfigItem(keyName = FIXED_TAB_COLLAPSE, name = "Hideable Fixed Chat", description = "Allow fixed-layout chat to be hidden like resizable chat", section = resizeChatSection, position = 11)
    default boolean fixedTabCollapse() { return true; }

    @ConfigItem(keyName = FIXED_ADJUST_VIEWPORT, name = "Adjust Camera On Grow", description = "Keep the player centered when fixed chat grows", section = resizeChatSection, position = 12)
    default boolean fixedAdjustViewport() { return false; }

    @ConfigItem(keyName = REVERT_FOR_DIALOGS, name = "Revert For Dialogs", description = "Temporarily return adjusted dimensions toward stock while chat dialogs are open", section = resizeChatSection, position = 20)
    default Revert revertForDialogs() { return Revert.BOTH; }

    @ConfigItem(keyName = REVERT_FOR_MODALS, name = "Revert For Interfaces", description = "Temporarily return adjusted dimensions toward stock while top-level interfaces are open", section = resizeChatSection, position = 21)
    default Revert revertForModals() { return Revert.UNGROW; }

    @ConfigItem(keyName = TOGGLE_SHOW_CHAT, name = "Show/Hide Chat Keybind", description = "Hide or unhide the native chatbox", section = resizeChatSection, position = 6)
    default Keybind toggleShowChat() { return Keybind.NOT_SET; }

    @ConfigItem(keyName = NO_BORDERS, name = "Don't Draw Resize Borders", description = "Hide Improved Chat's resize frame; native dialogue and option-menu borders remain untouched", section = resizeChatSection, position = 30)
    default boolean noBorders() { return false; }

    @ConfigItem(keyName = NO_BACKGROUND_ZOOM, name = "Don't Zoom Background", description = "Keep the normal opaque chat background at its native artwork scale", section = resizeChatSection, position = 31)
    default boolean noBackgroundZoom() { return false; }

    @ConfigItem(keyName = DRAG_MODIFIER, name = "Drag-Resize Modifier", description = "Hold this key while dragging a chat border to resize; unset disables drag resizing", section = dragResizeSection, position = 0)
    default Keybind dragModifier() { return Keybind.NOT_SET; }

    @ConfigItem(keyName = LIVE_REWRAP, name = "Live Re-wrap", description = "Re-wrap chat continuously while drag-resizing", section = dragResizeSection, position = 1)
    default boolean liveRewrap() { return true; }

    @Alpha
    @ConfigItem(keyName = INDICATOR_COLOR, name = "Drag Indicator Color", description = "Color of the active drag-resize border", section = dragResizeSection, position = 2)
    default Color indicatorColor() { return Color.GREEN; }

    @Range(min = -165, max = 10000)
    @Units(Units.PIXELS)
    @ConfigItem(keyName = SWAP_HEIGHT_CHANGE, name = "Secondary Height Change", description = "Height change while the secondary size is active", section = secondarySizeSection, position = 0)
    default int secondaryHeightChange() { return 0; }

    @Range(min = -519, max = 10000)
    @Units(Units.PIXELS)
    @ConfigItem(keyName = SWAP_WIDTH_CHANGE, name = "Secondary Width Change", description = "Width change while the secondary size is active", section = secondarySizeSection, position = 1)
    default int secondaryWidthChange() { return 0; }

    @ConfigItem(keyName = SWAP_SIZE_MODE, name = "Secondary Size Mode", description = "Hold or toggle the secondary chat size", section = secondarySizeSection, position = 2)
    default Mode secondaryMode() { return Mode.HOLD; }

    @ConfigItem(keyName = SWAP_SIZE_KEYBIND, name = "Secondary Size Keybind", description = "Switch to the secondary chat size", section = secondarySizeSection, position = 3)
    default Keybind secondaryKeybind() { return Keybind.NOT_SET; }

    // ---------------------------------------------------------------------
    // Consolidated companion chat features
    // ---------------------------------------------------------------------

    String HIDE_SCROLLBAR_KEY = "hideScrollbar";
    String DEFAULT_CUSTOM_CHANNEL_NAME = "[<col=0000ff>$$</col>]";

    @ConfigSection(
        name = "General Chat Cleanup",
        description = "General message and layout cleanup; each option works independently",
        position = 14,
        closedByDefault = true
    )
    String cleanChatSection = "cleanChat";

    @ConfigSection(
        name = "Chat Color Bar",
        description = "Optional per-message channel color marker",
        position = 15,
        closedByDefault = true
    )
    String cleanColorBarSection = "cleanColorBar";

    @ConfigSection(
        name = "Clan Cleanup",
        description = "Clean up clan chat presentation",
        position = 16,
        closedByDefault = true
    )
    String cleanClanSection = "cleanClan";

    @ConfigSection(
        name = "Guest Clan Cleanup",
        description = "Clean up guest clan chat presentation",
        position = 17,
        closedByDefault = true
    )
    String cleanGuestClanSection = "cleanGuestClan";

    @ConfigSection(
        name = "GIM Cleanup",
        description = "Clean up Group Ironman chat presentation",
        position = 18,
        closedByDefault = true
    )
    String cleanGimSection = "cleanGim";

    @ConfigSection(
        name = "Friends Chat Cleanup",
        description = "Clean up friends chat presentation",
        position = 19,
        closedByDefault = true
    )
    String cleanFriendsSection = "cleanFriends";

    @ConfigSection(
        name = "Chatbox Opacity",
        description = "Fine tune native transparent chatbox and button opacity",
        position = 20,
        closedByDefault = true
    )
    String chatboxOpacitySection = "chatboxOpacity";

    @ConfigSection(
        name = "Chat Menu",
        description = "Simplify right-click options in the chatbox",
        position = 21,
        closedByDefault = true
    )
    String chatMenuSection = "chatMenu";

    @ConfigSection(
        name = "Offline Clan Status",
        description = "Mark offline clan members in chat",
        position = 22,
        closedByDefault = true
    )
    String offlineClanSection = "offlineClan";

    @ConfigSection(
        name = "Dialogue Text Styling",
        description = "Customize supported dialogue text appearance and readability",
        position = 23,
        closedByDefault = true
    )
    String dialogueFontsSection = "dialogueFonts";

        @ConfigItem(keyName = "removeWelcome", name = "Remove Welcome Message", description = "Remove the Welcome to Old School RuneScape message", section = cleanChatSection, position = 0)
    default boolean removeWelcome() { return false; }

    @ConfigItem(keyName = "lineBreakIndentationMode", name = "Indent Mode", description = "Choose where wrapped channel-message lines begin", section = cleanChatSection, position = 1)
    default IndentMode indentationMode() { return IndentMode.MESSAGE; }

    @ConfigItem(keyName = HIDE_SCROLLBAR_KEY, name = "Hide Scrollbar", description = "Hide the chat scrollbar while keeping mouse-wheel scrolling", section = cleanChatSection, position = 2)
    default boolean hideScrollbar() { return false; }

    @ConfigItem(keyName = "hideSpecs", name = "Remove Special Attack Text", description = "Remove Dragon and Crystal equipment special-attack chat text", section = cleanChatSection, position = 3)
    default boolean hideSpecs() { return false; }

    @ConfigItem(keyName = "improvedTimestamps", name = "Fixed-width Timestamps", description = "Use equal-width digits when chat timestamps are enabled", section = cleanChatSection, position = 4)
    default boolean isFixedWidthTimestampEnabled() { return false; }

    @ConfigItem(keyName = "colorBar", name = "Enable Color Bar", description = "Draw a thin channel-colored marker beside each native chat message", section = cleanColorBarSection, position = 0)
    default boolean isColorBarEnabled() { return false; }

    @Units(Units.PIXELS)
    @Range(min = -1000, max = 1000)
    @ConfigItem(keyName = "colorBarOffset", name = "Color Bar Offset", description = "Horizontal offset of the message color bar", section = cleanColorBarSection, position = 1)
    default int colorBarOffset() { return 0; }

    @Units(Units.PIXELS)
    @Range(min = 1, max = 20)
    @ConfigItem(keyName = "colorBarWidth", name = "Color Bar Width", description = "Width of the message color bar", section = cleanColorBarSection, position = 2)
    default int colorBarWidth() { return 1; }

    @Alpha
    @ConfigItem(keyName = "noChannelColor", name = "No Channel", description = "Color bar color for messages without a channel", section = cleanColorBarSection, position = 3)
    default Color noChannelColor() { return new Color(0, true); }

    @Alpha
    @ConfigItem(keyName = "clanColor", name = "Clan", description = "Color bar color for clan chat", section = cleanColorBarSection, position = 4)
    default Color clanChannelColor() { return new Color(0x0B3CC4); }

    @Alpha
    @ConfigItem(keyName = "friendColor", name = "Friends Chat", description = "Color bar color for friends chat", section = cleanColorBarSection, position = 5)
    default Color friendsChannelColor() { return new Color(0xF8EC3B); }

    @Alpha
    @ConfigItem(keyName = "groupIronColor", name = "Group Iron", description = "Color bar color for Group Ironman chat", section = cleanColorBarSection, position = 6)
    default Color groupIronChannelColor() { return new Color(0x195985); }

    @Alpha
    @ConfigItem(keyName = "guestClanColor", name = "Guest Clan", description = "Color bar color for guest clan chat", section = cleanColorBarSection, position = 7)
    default Color guestClanChannelColor() { return new Color(0x00855E); }

    @ConfigItem(keyName = "removeClanInstruction", name = "Remove Startup Message", description = "Remove clan-channel usage instructions", section = cleanClanSection, position = 0)
    default boolean removeClanInstruction() { return false; }

    @ConfigItem(keyName = "removeClanName", name = "Remove Clan Name", description = "Remove the clan name prefix from clan messages", section = cleanClanSection, position = 1)
    default boolean removeClanName() { return false; }

    @ConfigItem(keyName = "shortClanName", name = "Custom Clan Name", description = "Replacement clan label; use $$ for the current clan name", section = cleanClanSection, position = 2)
    default String getShortClanName() { return DEFAULT_CUSTOM_CHANNEL_NAME; }

    @ConfigItem(keyName = "removeClanRank", name = "Remove Clan Rank", description = "Remove clan-rank icons from usernames", section = cleanClanSection, position = 3)
    default boolean removeClanRank() { return false; }

    @ConfigItem(keyName = "removeGuestClanInstruction", name = "Remove Startup Message", description = "Remove guest-clan usage instructions", section = cleanGuestClanSection, position = 0)
    default boolean removeGuestClanInstruction() { return false; }

    @ConfigItem(keyName = "removeGuestClanReconnecting", name = "Remove Reconnecting Message", description = "Remove guest-clan automatic reconnect messages", section = cleanGuestClanSection, position = 1)
    default boolean removeGuestClanReconnecting() { return false; }

    @ConfigItem(keyName = "removeGuestClanName", name = "Remove Guest Clan Name", description = "Remove the guest clan name prefix", section = cleanGuestClanSection, position = 2)
    default boolean removeGuestClanName() { return false; }

    @ConfigItem(keyName = "shortGuestClanName", name = "Custom Guest Clan Name", description = "Replacement guest-clan label; use $$ for the current clan name", section = cleanGuestClanSection, position = 3)
    default String getShortGuestClanName() { return DEFAULT_CUSTOM_CHANNEL_NAME; }

    @ConfigItem(keyName = "removeGroupIronInstruction", name = "Remove Startup Message", description = "Remove Group Ironman channel usage instructions", section = cleanGimSection, position = 0)
    default boolean removeGroupIronInstruction() { return false; }

    @ConfigItem(keyName = "removeGroupIronName", name = "Remove GIM Name", description = "Remove the Group Ironman channel name prefix", section = cleanGimSection, position = 1)
    default boolean removeGroupIronName() { return false; }

    @ConfigItem(keyName = "moveGroupIronBroadcasts", name = "Move GIM Broadcasts", description = "Keep GIM broadcasts out of the clan tab", section = cleanGimSection, position = 2)
    default boolean removeGroupIronFromClan() { return false; }

    @ConfigItem(keyName = "shortGroupIronName", name = "Custom GIM Name", description = "Replacement GIM label; use $$ for the current group name", section = cleanGimSection, position = 3)
    default String getShortGroupIronName() { return DEFAULT_CUSTOM_CHANNEL_NAME; }

    @ConfigItem(keyName = "removeFriendsChatInstruction", name = "Remove Startup Message", description = "Remove friends-chat usage instructions", section = cleanFriendsSection, position = 0)
    default boolean removeFriendsChatStartup() { return false; }

    @ConfigItem(keyName = "removeFriendsChatName", name = "Remove Friends Chat Name", description = "Remove the friends-chat channel prefix", section = cleanFriendsSection, position = 1)
    default boolean removeFriendsChatName() { return false; }

    @ConfigItem(keyName = "removeFriendsAttempting", name = "Remove Attempting to Join", description = "Remove friends-chat join-attempt messages", section = cleanFriendsSection, position = 2)
    default boolean removeFriendsAttempting() { return false; }

    @ConfigItem(keyName = "removeFriendsNowTalking", name = "Remove Now Talking In", description = "Remove friends-chat now-talking messages", section = cleanFriendsSection, position = 3)
    default boolean removeFriendsNowTalking() { return false; }

    @ConfigItem(keyName = "shortFriendsName", name = "Custom Friends Chat Name", description = "Replacement friends-chat label; use $$ for the current channel name", section = cleanFriendsSection, position = 4)
    default String getShortFriendsName() { return DEFAULT_CUSTOM_CHANNEL_NAME; }

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

    @ConfigItem(keyName = "enableRemoveChatOptions", name = "Remove Chat Options", description = "Remove chat-message context-menu options; hold Control to temporarily show them", section = chatMenuSection, position = 0)
    default boolean enableRemoveChatOptions() { return false; }

    @ConfigItem(keyName = "removeLookupChatOption", name = "Remove Lookup", description = "Also remove the player Lookup entry from chat-message menus", section = chatMenuSection, position = 1)
    default boolean removeLookupChatOption() { return false; }

    @ConfigItem(keyName = "enableOfflineChatStatus", name = "Enable Offline Clan Status", description = "Mark offline clan members in native clan chat", section = offlineClanSection, position = 0)
    default boolean enableOfflineChatStatus() { return false; }

    @ConfigItem(keyName = "enableOfflineIcon", name = "Show Offline Icon", description = "Show an Improved Chat offline-status icon beside offline clan members", section = offlineClanSection, position = 1)
    default boolean enableOfflineIcon() { return true; }

    @ConfigItem(keyName = "enableOfflineColor", name = "Color Offline Names", description = "Recolor offline clan member names", section = offlineClanSection, position = 2)
    default boolean enableOfflineColor() { return true; }

    @Alpha
    @ConfigItem(keyName = "offlineColor", name = "Offline Color", description = "Name color for offline clan members", section = offlineClanSection, position = 3)
    default Color offlineColor() { return Color.DARK_GRAY; }

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
