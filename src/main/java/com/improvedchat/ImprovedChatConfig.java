package com.improvedchat;

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

    @ConfigItem(keyName = NO_BORDERS, name = "Don't Draw Resize Borders", description = "Hide Improved Chat\'s resize frame; native dialogue and option-menu borders remain untouched", section = resizeChatSection, position = 30)
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


}
