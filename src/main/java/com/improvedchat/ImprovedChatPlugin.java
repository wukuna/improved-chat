package com.improvedchat;

import com.improvedchat.chatbox.clean.CleanChatModule;
import com.improvedchat.chatbox.collapse.ChatCollapseModule;
import com.improvedchat.chatbox.menu.RemoveChatOptionsModule;
import com.improvedchat.chatbox.modern.ModernChatThemeModule;
import com.improvedchat.chatbox.offline.OfflineChatStatusModule;
import com.improvedchat.chatbox.opacity.ChatboxOpacityModule;
import com.improvedchat.chatbox.resize.ChatResizeModule;
import com.improvedchat.dialogue.DialogueFontsModule;
import com.improvedchat.model.MessageCategory;
import com.improvedchat.model.MessageMergeRule;
import com.improvedchat.model.OverlayMessage;
import com.improvedchat.overlay.AttentionEngine;
import com.improvedchat.overlay.DynamicChatOverlay;
import com.improvedchat.overlay.OverlayConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ResizeableChanged;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ChatColorConfig;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.chatfilter.ChatFilterConfig;
import net.runelite.client.plugins.chatfilter.ChatFilterPlugin;
import net.runelite.client.plugins.emojis.EmojiPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Core plugin class. Maintains a single shared message pool ({@link OverlayMessage}) fed by
 * {@code onChatMessage}, and manages a dynamic list of {@link DynamicChatOverlay} instances
 * that each filter and render a subset of the pool based on their {@link OverlayConfig}.
 */
@PluginDescriptor(
        name = "Improved Chat TEST",
        configName = "improvedchat",
        description = "Customizable chat overlays, native chat collapse/resize, modern styling, message rules, and alerts.",
        tags = {"chat", "message", "overlay", "color", "customize", "private", "clan", "resize", "ui"})
public class ImprovedChatPlugin extends Plugin {

    public static final boolean DEBUG = false;

    // Independent Plugin Hub identity and configuration namespace.
    private static final String CONFIG_GROUP = "improvedchat";
    private static final String OVERLAY_CONFIGS_KEY = "overlayConfigs";
    private static final int MAX_POOL_SIZE = 200;

    private static final Pattern BOSS_KC_PATTERN = Pattern.compile("Your .+ count is:");
    private static final MessageMergeRule[] MESSAGE_MERGE_RULES = {
            new MessageMergeRule("You eat", "It heals some health.", true),
            new MessageMergeRule("You drink", Pattern.compile("You have [0-9].*")),
            new MessageMergeRule("You drink", "You have finished your potion.", true),
            new MessageMergeRule("You are now a guest of", "To talk, start each", false),
//            new MessageMergeRule("Now talking in chat-channel", "To talk, start each", false),

    };

    private static final Set<ChatMessageType> SENDER_TYPES = EnumSet.of(
            ChatMessageType.PRIVATECHAT,
            ChatMessageType.PRIVATECHATOUT,
            ChatMessageType.MODPRIVATECHAT,
            ChatMessageType.PUBLICCHAT,
            ChatMessageType.MODCHAT,
            ChatMessageType.AUTOTYPER,
            ChatMessageType.MODAUTOTYPER,
            ChatMessageType.FRIENDSCHAT,
            ChatMessageType.CLAN_CHAT,
            ChatMessageType.CLAN_GUEST_CHAT,
            ChatMessageType.CLAN_GIM_CHAT
    );

    /**
     * Chat types the RuneLite Emojis plugin converts (mirrors its own gate in EmojiPlugin). For
     * these we seed the stored body from the live message node instead of the immutable event
     * message snapshot, so emoji {@code <img=N>} tags inserted by that plugin reach our overlays.
     * All nine are also {@link #SENDER_TYPES}, so reconciliation rebuilds via
     * {@link OverlayMessage#senderMessage}.
     */
    private static final Set<ChatMessageType> EMOJI_WATCHED_TYPES = EnumSet.of(
            ChatMessageType.PUBLICCHAT,
            ChatMessageType.MODCHAT,
            ChatMessageType.FRIENDSCHAT,
            ChatMessageType.CLAN_CHAT,
            ChatMessageType.CLAN_GUEST_CHAT,
            ChatMessageType.CLAN_GIM_CHAT,
            ChatMessageType.PRIVATECHAT,
            ChatMessageType.PRIVATECHATOUT,
            ChatMessageType.MODPRIVATECHAT
    );

    /** All message types the plugin will capture into the shared pool. */
    private static final Set<ChatMessageType> ALL_SUPPORTED_TYPES = EnumSet.of(
            // Game
            ChatMessageType.GAMEMESSAGE, ChatMessageType.SPAM, ChatMessageType.CONSOLE,
            ChatMessageType.WELCOME, ChatMessageType.BROADCAST, ChatMessageType.DIDYOUKNOW,
            ChatMessageType.ENGINE, ChatMessageType.UNKNOWN, ChatMessageType.LEVELUPMESSAGE,
            ChatMessageType.NPC_SAY, ChatMessageType.MESBOX,
            ChatMessageType.PLAYERRELATED, ChatMessageType.TENSECTIMEOUT, ChatMessageType.SNAPSHOTFEEDBACK,
            ChatMessageType.FRIENDNOTIFICATION, ChatMessageType.FRIENDSCHATNOTIFICATION,
            ChatMessageType.IGNORENOTIFICATION,
            ChatMessageType.ITEM_EXAMINE, ChatMessageType.NPC_EXAMINE, ChatMessageType.OBJECT_EXAMINE,
            ChatMessageType.CLAN_MESSAGE, ChatMessageType.CLAN_GUEST_MESSAGE,
            ChatMessageType.CLAN_GIM_MESSAGE, ChatMessageType.CLAN_CREATION_INVITATION,
            // Trade
            ChatMessageType.TRADE, ChatMessageType.TRADE_SENT, ChatMessageType.TRADEREQ,
            ChatMessageType.CHALREQ_TRADE, ChatMessageType.CHALREQ_FRIENDSCHAT,
            // Public
            ChatMessageType.PUBLICCHAT, ChatMessageType.MODCHAT,
            ChatMessageType.AUTOTYPER, ChatMessageType.MODAUTOTYPER,
            // Friends chat
            ChatMessageType.FRIENDSCHAT,
            // Clan
            ChatMessageType.CLAN_CHAT, ChatMessageType.CLAN_GUEST_CHAT, ChatMessageType.CLAN_GIM_CHAT,
            ChatMessageType.CHALREQ_CLANCHAT,
            // Private
            ChatMessageType.PRIVATECHAT, ChatMessageType.PRIVATECHATOUT, ChatMessageType.MODPRIVATECHAT,
            ChatMessageType.LOGINLOGOUTNOTIFICATION
    );

    @Inject
    private Client client;

    @Inject
    private ImprovedChatConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ChatColorConfig chatColorConfig;

    @Inject
    private Gson gson;

    @Inject
    private PluginManager pluginManager;

    @Inject
    private ChatResizeModule chatResizeModule;

    @Inject
    private ChatCollapseModule chatCollapseModule;

    @Inject
    private ModernChatThemeModule modernChatThemeModule;

    @Inject
    private CleanChatModule cleanChatModule;

    @Inject
    private ChatboxOpacityModule chatboxOpacityModule;

    @Inject
    private RemoveChatOptionsModule removeChatOptionsModule;

    @Inject
    private OfflineChatStatusModule offlineChatStatusModule;

    @Inject
    private DialogueFontsModule dialogueFontsModule;


    // Shared message pool
    private final CopyOnWriteArrayList<OverlayMessage> messages = new CopyOnWriteArrayList<>();

    // Mirrors the Chat Filter plugin's word/regex lists when "Use Chat Filter" is enabled
    private final ChatMessageFilter chatMessageFilter = new ChatMessageFilter();
    private Plugin chatFilterPlugin;
    private Plugin emojiPlugin;

    // Dynamic overlays
    private final List<OverlayConfig> overlayConfigs = new ArrayList<>();
    private final List<DynamicChatOverlay> overlays = new ArrayList<>();

    private NavigationButton navButton;
    private ImprovedChatPanel panel;
    private boolean pmWidgetsHidden = false;

    // Messages whose backing MessageNode may be rewritten in place by another plugin after we
    // capture it — Chat Commands (command results) or the RuneLite Emojis plugin (<img=N> tags).
    // Drained on subsequent game ticks by reconcilePendingUpdates().
    private final List<PendingMessageUpdate> pendingUpdates = new ArrayList<>();

    // The Chat Commands plugin rewrites a command's result into the RuneLite-format message; it can
    // take several ticks to arrive, so command entries linger until then.
    private static final int COMMAND_UPDATE_TICKS = 10;
    private static final Function<MessageNode, String> COMMAND_VALUE = MessageNode::getRuneLiteFormatMessage;

    // The Emojis plugin converts shortcuts to <img=N> synchronously within the same dispatch, so a
    // single follow-up tick suffices before the entry is dropped.
    private static final int EMOJI_UPDATE_TICKS = 1;
    private static final Function<MessageNode, String> EMOJI_VALUE = node -> {
        String value = node.getValue();
        return value == null ? null : value.trim();
    };

    /**
     * A pooled message whose backing {@link MessageNode} may be rewritten in place by another
     * plugin after capture. Reconciled on subsequent ticks by {@link #reconcilePendingUpdates()}:
     * once {@code valueAccessor} reports a value different from {@code originalText}, the pooled
     * message is rebuilt with it; the entry is dropped when it updates or after its tick budget.
     */
    private static class PendingMessageUpdate {
        final OverlayMessage widgetMessage;
        final MessageNode messageNode;
        final Function<MessageNode, String> valueAccessor;
        final String originalText;
        int ticksRemaining;

        PendingMessageUpdate(OverlayMessage widgetMessage, MessageNode messageNode,
                Function<MessageNode, String> valueAccessor, String originalText, int ticksRemaining) {
            this.widgetMessage = widgetMessage;
            this.messageNode = messageNode;
            this.valueAccessor = valueAccessor;
            this.originalText = originalText;
            this.ticksRemaining = ticksRemaining;
        }
    }

    @Override
    protected void startUp() {
        loadOverlayConfigs();
        MessageColorRuleEngine.configure(config);
        OverlayColorRuleEngine.configure(config);
        rebuildChatFilter();

        for (OverlayConfig oc : overlayConfigs) {
            addOverlay(oc);
        }

        updatePmWidgetVisibility();

        chatResizeModule.startUp();
        chatCollapseModule.startUp();
        modernChatThemeModule.startUp(this);
        cleanChatModule.startUp();
        chatboxOpacityModule.startUp();
        removeChatOptionsModule.startUp();
        offlineChatStatusModule.startUp();
        dialogueFontsModule.startUp();

        panel = new ImprovedChatPanel(this);
        BufferedImage icon;
        try {
            icon = ImageUtil.loadImageResource(getClass(), "/panelicon.png");
        } catch (Exception e) {
            icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        }
        navButton = NavigationButton.builder()
                .tooltip("Improved Chat TEST")
                .icon(icon)
                .priority(10)
                .panel(panel)
                .build();
        if (!config.hideSidePanel()) {
            clientToolbar.addNavigation(navButton);
        }
    }

    @Override
    protected void shutDown() {
        dialogueFontsModule.shutDown();
        offlineChatStatusModule.shutDown();
        removeChatOptionsModule.shutDown();
        chatboxOpacityModule.shutDown();
        cleanChatModule.shutDown();
        modernChatThemeModule.shutDown();
        chatCollapseModule.shutDown();
        chatResizeModule.shutDown();

        for (DynamicChatOverlay overlay : overlays) {
            overlayManager.remove(overlay);
        }
        overlays.clear();
        pendingUpdates.clear();
        MessageColorRuleEngine.configure(null);
        OverlayColorRuleEngine.configure(null);
        AttentionEngine.setClientFocused(true);

        if (pmWidgetsHidden) {
            setPmWidgetsHidden(false);
        }

        if (navButton != null) {
            clientToolbar.removeNavigation(navButton);
        }
    }

    // --- Overlay config management ---

    public List<OverlayConfig> getOverlayConfigs() {
        return overlayConfigs;
    }

    public void addNewOverlay() {
        OverlayConfig oc = new OverlayConfig();
        overlayConfigs.add(oc);
        addOverlay(oc);
        saveOverlayConfigs();
        if (panel != null) {
            panel.rebuild();
        }
    }

    public void removeOverlay(OverlayConfig oc) {
        overlayConfigs.remove(oc);
        DynamicChatOverlay toRemove = null;
        for (DynamicChatOverlay overlay : overlays) {
            if (overlay.getOverlayConfig() == oc) {
                toRemove = overlay;
                break;
            }
        }
        if (toRemove != null) {
            overlayManager.remove(toRemove);
            overlays.remove(toRemove);
        }
        saveOverlayConfigs();
        updatePmWidgetVisibility();
        if (panel != null) {
            panel.rebuild();
        }
    }

    /**
     * Deletes every existing overlay and restores the two defaults created on first install
     * (same set and order as {@link #loadOverlayConfigs()}). Destructive — callers should confirm.
     */
    public void resetOverlays() {
        for (DynamicChatOverlay overlay : overlays) {
            overlayManager.remove(overlay);
        }
        overlays.clear();
        overlayConfigs.clear();

        overlayConfigs.add(OverlayConfig.defaultPrivateOverlay());
        overlayConfigs.add(OverlayConfig.defaultAllOverlay());
        for (OverlayConfig oc : overlayConfigs) {
            addOverlay(oc);
        }

        saveOverlayConfigs();
        updatePmWidgetVisibility();
        if (panel != null) {
            panel.rebuild();
        }
    }

    public void onOverlayConfigChanged() {
        saveOverlayConfigs();
        updatePmWidgetVisibility();
        for (DynamicChatOverlay overlay : overlays) {
            overlay.setOverlayConfig(overlay.getOverlayConfig());
        }
    }

    public void moveOverlay(OverlayConfig oc, int direction) {
        int index = overlayConfigs.indexOf(oc);
        int newIndex = index + direction;
        if (newIndex < 0 || newIndex >= overlayConfigs.size()) {
            return;
        }
        Collections.swap(overlayConfigs, index, newIndex);
        Collections.swap(overlays, index, newIndex);
        refreshOverlayPriorities();
        saveOverlayConfigs();
        if (panel != null) {
            panel.rebuild();
        }
    }

    private void refreshOverlayPriorities() {
        for (int i = 0; i < overlays.size(); i++) {
            DynamicChatOverlay overlay = overlays.get(i);
            overlayManager.remove(overlay);
            overlay.setPriority(10f + i);
        }
        for (DynamicChatOverlay overlay : overlays) {
            overlayManager.add(overlay);
        }
    }

    private void addOverlay(OverlayConfig oc) {
        DynamicChatOverlay overlay = new DynamicChatOverlay(this, config, client,
                chatColorConfig, oc);
        overlays.add(overlay);
        overlayManager.add(overlay);
        refreshOverlayPriorities();
    }

    // --- Persistence ---

    private void loadOverlayConfigs() {
        overlayConfigs.clear();
        String json = configManager.getConfiguration(CONFIG_GROUP, OVERLAY_CONFIGS_KEY);
        if (json != null && !json.isEmpty()) {
            try {
                Type listType = new TypeToken<List<OverlayConfig>>() {}.getType();
                List<OverlayConfig> loaded = gson.fromJson(json, listType);
                if (loaded != null && !loaded.isEmpty()) {
                    for (OverlayConfig overlayConfig : loaded) {
                        if (overlayConfig != null) {
                            overlayConfigs.add(overlayConfig);
                        }
                    }
                    if (!overlayConfigs.isEmpty()) {
                        return;
                    }
                }
            } catch (Exception e) {
                // Fall through to defaults
            }
        }
        // First run — create defaults
        overlayConfigs.add(OverlayConfig.defaultPrivateOverlay());
        overlayConfigs.add(OverlayConfig.defaultAllOverlay());
        saveOverlayConfigs();
    }

    public void saveOverlayConfigs() {
        String json = gson.toJson(overlayConfigs);
        configManager.setConfiguration(CONFIG_GROUP, OVERLAY_CONFIGS_KEY, json);
    }

    // --- Private-chat visibility ---

    private void updatePmWidgetVisibility() {
        boolean shouldHide = config.hidePrivateChat();
        setPmWidgetsHidden(shouldHide);
        pmWidgetsHidden = shouldHide;
    }

    // --- Event handlers ---

    @Subscribe
    public void onFocusChanged(FocusChanged event) {
        AttentionEngine.setClientFocused(event.isFocused());
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            updatePmWidgetVisibility();
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == InterfaceID.PM_CHAT && pmWidgetsHidden) {
            setPmWidgetsHidden(true);
        }
    }

    @Subscribe
    public void onVarClientIntChanged(VarClientIntChanged event) {
        if (event.getIndex() == VarClientID.CHAT_VIEW) {
            for (DynamicChatOverlay overlay : overlays) {
                if (overlay.getOverlayConfig().getPlacementMode() == com.improvedchat.model.PlacementMode.FREE) {
                    updateSmartPosition(overlay);
                }
            }
        }
    }

    @Subscribe
    public void onResizeableChanged(ResizeableChanged event) {
        for (DynamicChatOverlay overlay : overlays) {
            if (overlay.getOverlayConfig().getPlacementMode() == com.improvedchat.model.PlacementMode.FREE) {
                updateDefaultPosition(overlay);
                updateSmartPosition(overlay);
            }
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        String option = event.getMenuOption();
        if (option == null) {
            return;
        }

        // Handle chatbox tab "Clear history" options
        if (option.contains("Clear")) {
            if (option.contains("Game:")) {
                clearMessagesForCategories(
                        MessageCategory.GAME,
                        MessageCategory.GAME_CLAN);
                return;
            }
            if (option.contains("Public:")) {
                clearMessagesForCategories(MessageCategory.PUBLIC_CHAT, MessageCategory.AUTO);
                return;
            }
            if (option.contains("Private:")) {
                clearMessagesForCategories(MessageCategory.PRIVATE);
                return;
            }
            if (option.contains("Channel:")) {
                clearMessagesForCategories(MessageCategory.FRIENDS_CHAT);
                return;
            }
            if (option.contains("Clan:")) {
                clearMessagesForCategories(
                        MessageCategory.GAME_CLAN,
                        MessageCategory.CLAN_CHAT,
                        MessageCategory.GUEST_CLAN_CHAT,
                        MessageCategory.GIM_CLAN_CHAT);
                return;
            }
        }
    }

    /**
     * Handles clicks on the overlay-level "Clear [name] history" right-click entries. These are
     * {@link net.runelite.client.ui.overlay.OverlayMenuEntry} entries dispatched via
     * {@link OverlayMenuClicked} (not {@code MenuOptionClicked}), so we use the clicked overlay
     * reference directly rather than parsing the menu target string.
     */
    @Subscribe
    public void onOverlayMenuClicked(OverlayMenuClicked event) {
        if (event.getOverlay() instanceof DynamicChatOverlay) {
            DynamicChatOverlay overlay = (DynamicChatOverlay) event.getOverlay();
            clearMessagesForTypes(overlay.getOverlayConfig().getMessageTypes());
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        ChatMessageType type = event.getType();

        if (DEBUG) {
            prependDebugType(event);
        }

        if (!ALL_SUPPORTED_TYPES.contains(type)) {
            return;
        }

        String message = event.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        message = message.trim();

        // RuneLite's normal chatbox expands Jagex symbolic macros before display. ChatMessage can
        // still expose those tokens, so expand system/game messages before we store or style them.
        if (!SENDER_TYPES.contains(type)
                && type != ChatMessageType.LOGINLOGOUTNOTIFICATION
                && message.indexOf('@') >= 0) {
            String expanded = client.macroExpand(message);
            if (expanded != null && !expanded.isEmpty()) {
                message = expanded;
            }
        }

        // Emoji support: for emoji-eligible types that aren't chat commands, seed the stored body
        // from the message node (mutated in place by the RuneLite Emojis plugin to insert <img=N>)
        // rather than the immutable pre-plugin event message. Gated on the Emojis plugin being
        // enabled so we only switch capture source when there's a reason to — otherwise an unrelated
        // plugin's in-place node edit (e.g. Chat Filter censoring) could leak into what we store.
        // Commands (message.startsWith("!")) stay on the command path below. Reconciled on the next
        // tick in onGameTick, catching the case where Emojis is registered after us and hasn't
        // converted yet at capture.
        MessageNode messageNode = event.getMessageNode();
        MessageColorRuleEngine.applyToChatbox(client, event, config, message);

        boolean emojiWatched = EMOJI_WATCHED_TYPES.contains(type)
                && !message.startsWith("!")
                && isEmojiPluginEnabled();
        if (emojiWatched && messageNode != null) {
            String nodeValue = messageNode.getValue();
            if (nodeValue != null && !nodeValue.trim().isEmpty()) {
                message = nodeValue.trim();
            }
        }

        // Drop messages matching the Chat Filter plugin's lists — only while that plugin is enabled.
        if (config.useChatFilter() && isChatFilterEnabled() && chatMessageFilter.matches(message)) {
            return;
        }

        String sender = cleanSender(event.getName());
        String channelName = cleanSender(event.getSender());
        boolean isOutgoing = type == ChatMessageType.PRIVATECHATOUT;
        boolean isBossKc = BOSS_KC_PATTERN.matcher(message).find();

        // Handle message merging for game-type messages
        if (!SENDER_TYPES.contains(type) && type != ChatMessageType.LOGINLOGOUTNOTIFICATION) {
            for (MessageMergeRule rule : MESSAGE_MERGE_RULES) {
                if (rule.matchesPreviousPrefix(message)) {
                    message = message.replace("<br>", " ");
                    break;
                }
            }

            if (!messages.isEmpty()) {
                OverlayMessage lastMsg = messages.get(messages.size() - 1);
                String merged = tryMergeMessages(lastMsg.getMessage(), message);
                if (merged != null) {
                    int existingCount = 0;
                    if (config.collapseDuplicates()) {
                        String mergedStripped = stripTags(merged);
                        for (int i = messages.size() - 2; i >= 0; i--) {
                            OverlayMessage existing = messages.get(i);
                            if (stripTags(existing.getMessage()).equals(mergedStripped)) {
                                existingCount = existing.getCount();
                                messages.remove(i);
                                break;
                            }
                        }
                    }
                    OverlayMessage mergedMsg = OverlayMessage.gameMessage(
                            merged, System.currentTimeMillis(), lastMsg.getType(), lastMsg.isBossKc());
                    if (existingCount > 0) {
                        mergedMsg.setCount(existingCount + 1);
                    }
                    messages.set(messages.size() - 1, mergedMsg);
                    return;
                }
            }
        }

        // Create the new message
        OverlayMessage newMsg;
        if (type == ChatMessageType.LOGINLOGOUTNOTIFICATION) {
            int maxFade = 5;
            newMsg = OverlayMessage.loginNotification(
                    sender != null ? sender : "System", message, System.currentTimeMillis(), maxFade);
        } else if (SENDER_TYPES.contains(type)) {
            newMsg = OverlayMessage.senderMessage(
                    sender != null ? sender : "Unknown", channelName, message, System.currentTimeMillis(), type, isOutgoing);
        } else {
            newMsg = OverlayMessage.gameMessage(message, System.currentTimeMillis(), type, isBossKc);
        }

        // Collapse duplicates (except login notifications). newMsg isn't in the pool yet, so pass
        // skipIndex -1 and base count 1 — the fold adds the matched entry's own count on top.
        if (config.collapseDuplicates() && type != ChatMessageType.LOGINLOGOUTNOTIFICATION) {
            newMsg.setCount(collapseDuplicate(messages, stripTags(message), newMsg.getSender(), 1, -1));
        }

        messages.add(newMsg);

        while (messages.size() > MAX_POOL_SIZE) {
            messages.remove(0);
        }

        // Track potential chat commands for delayed updates by Chat Commands plugin; otherwise
        // watch emoji-eligible messages for the Emojis plugin's next-tick <img=N> conversion.
        if (messageNode != null && message.startsWith("!")) {
            pendingUpdates.add(new PendingMessageUpdate(
                    newMsg, messageNode, COMMAND_VALUE, message, COMMAND_UPDATE_TICKS));
        } else if (emojiWatched && messageNode != null) {
            pendingUpdates.add(new PendingMessageUpdate(
                    newMsg, messageNode, EMOJI_VALUE, message, EMOJI_UPDATE_TICKS));
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        reconcilePendingUpdates();
    }

    /**
     * Drains {@link #pendingUpdates}, rebuilding any pooled message whose backing node has been
     * rewritten since capture (Chat Commands results, Emojis {@code <img=N>} tags). Each entry is
     * dropped once its value changes and the message is rebuilt, or once its tick budget runs out.
     */
    private void reconcilePendingUpdates() {
        for (int i = pendingUpdates.size() - 1; i >= 0; i--) {
            PendingMessageUpdate pending = pendingUpdates.get(i);
            pending.ticksRemaining--;

            String currentValue = pending.valueAccessor.apply(pending.messageNode);
            if (currentValue != null && !currentValue.equals(pending.originalText)) {
                rebuildPooledMessage(pending.widgetMessage, currentValue);
                pendingUpdates.remove(i);
            } else if (pending.ticksRemaining <= 0) {
                pendingUpdates.remove(i);
            }
        }
    }

    /**
     * Replaces the pooled {@code old} message with a copy carrying {@code newBody}, preserving its
     * kind (sender vs game), count, and metadata. Re-applies duplicate collapsing afterwards: the
     * body only reaches its final form here (a command result, or an Emojis {@code <img=N>} tag),
     * so a duplicate the capture-time text couldn't match may only surface post-rewrite. No-op if
     * {@code old} has already been evicted from the pool.
     */
    private void rebuildPooledMessage(OverlayMessage old, String newBody) {
        int idx = messages.indexOf(old);
        if (idx < 0) {
            return;
        }
        OverlayMessage updated;
        if (old.getSender() != null) {
            updated = OverlayMessage.senderMessage(
                    old.getSender(), old.getChannelName(), newBody,
                    old.getTimestamp(), old.getType(), old.isOutgoing());
        } else {
            updated = OverlayMessage.gameMessage(newBody, old.getTimestamp(), old.getType(), old.isBossKc());
        }
        if (old.getCount() > 1) {
            updated.setCount(old.getCount());
        }
        messages.set(idx, updated);

        // The shortcut-vs-<img> (or command-vs-result) mismatch at capture time can hide a
        // duplicate that only matches once the body reaches its final form here. updated is already
        // in the pool, so skip its own slot and fold on top of the count it already carries.
        if (config.collapseDuplicates() && updated.getType() != ChatMessageType.LOGINLOGOUTNOTIFICATION) {
            updated.setCount(collapseDuplicate(
                    messages, stripTags(newBody), updated.getSender(), updated.getCount(), idx));
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        // Keep our copy of the Chat Filter lists in sync as the user edits them.
        if ("chatfilter".equals(event.getGroup())) {
            rebuildChatFilter();
            return;
        }
        if (!event.getGroup().equals(CONFIG_GROUP)) {
            return;
        }
        if ("hideSidePanel".equals(event.getKey())) {
            if (config.hideSidePanel()) {
                clientToolbar.removeNavigation(navButton);
            } else {
                clientToolbar.addNavigation(navButton);
            }
        }
        if ("hidePrivateChat".equals(event.getKey())) {
            updatePmWidgetVisibility();
        }
        if ("enableResizableChat".equals(event.getKey())) {
            if (config.enableResizableChat()) {
                chatResizeModule.startUp();
            } else {
                chatResizeModule.shutDown();
            }
        }
        if ("enableCollapsibleChat".equals(event.getKey())) {
            if (config.enableCollapsibleChat()) {
                chatCollapseModule.startUp();
            } else {
                chatCollapseModule.shutDown();
            }
        }
        if ("enableChatboxOpacity".equals(event.getKey())) {
            if (config.enableChatboxOpacity()) chatboxOpacityModule.startUp(); else chatboxOpacityModule.shutDown();
        }
        if ("enableRemoveChatOptions".equals(event.getKey())) {
            if (config.enableRemoveChatOptions()) removeChatOptionsModule.startUp(); else removeChatOptionsModule.shutDown();
        }
        if ("enableOfflineChatStatus".equals(event.getKey())) {
            if (config.enableOfflineChatStatus()) offlineChatStatusModule.startUp(); else offlineChatStatusModule.shutDown();
        }
        if ("enableDialogueFonts".equals(event.getKey())) {
            if (config.enableDialogueFonts()) dialogueFontsModule.startUp(); else dialogueFontsModule.shutDown();
        }
    }

    /** Recompiles the Chat Filter patterns from the Chat Filter plugin's live config. */
    private void rebuildChatFilter() {
        chatMessageFilter.rebuild(configManager.getConfig(ChatFilterConfig.class));
    }

    /**
     * True when RuneLite's built-in Chat Filter plugin is currently enabled. The config lists are
     * always readable, but we only filter when the plugin itself is on — otherwise the user isn't
     * filtering their chat and shouldn't have overlay messages silently removed.
     */
    private boolean isChatFilterEnabled() {
        if (chatFilterPlugin == null) {
            for (Plugin p : pluginManager.getPlugins()) {
                if (p instanceof ChatFilterPlugin) {
                    chatFilterPlugin = p;
                    break;
                }
            }
        }
        return chatFilterPlugin != null && pluginManager.isPluginEnabled(chatFilterPlugin);
    }

    /**
     * True when RuneLite's Emojis plugin is currently enabled. Only then do we seed captured bodies
     * from the live message node (and watch for its {@code <img=N>} rewrite); otherwise we keep the
     * raw event message so an unrelated plugin's in-place node edit (e.g. Chat Filter censoring)
     * can't silently change what we store and render.
     */
    private boolean isEmojiPluginEnabled() {
        if (emojiPlugin == null) {
            for (Plugin p : pluginManager.getPlugins()) {
                if (p instanceof EmojiPlugin) {
                    emojiPlugin = p;
                    break;
                }
            }
        }
        return emojiPlugin != null && pluginManager.isPluginEnabled(emojiPlugin);
    }

    // --- Message access for overlays ---

    public List<OverlayMessage> getMessagesForOverlay(OverlayConfig overlayConfig) {
        Set<ChatMessageType> types = overlayConfig.getMessageTypes();
        int size = messages.size();
        if (size == 0 || types.isEmpty()) {
            return new ArrayList<>(0);
        }

        long currentTime = System.currentTimeMillis();
        int fadeOutDuration = overlayConfig.getFadeOutDuration();
        long fadeOutThreshold = fadeOutDuration > 0 ? (fadeOutDuration * 2000L) + 2000 : 0;
        boolean gameFilterEnabled = isGameFilterEnabled();
        boolean bossKcFilterEnabled = isBossKcFilterEnabled();

        int maxMessages = overlayConfig.getMaxMessages();
        List<OverlayMessage> filtered = new ArrayList<>(maxMessages);
        int msgCount = 0;

        for (int i = size - 1; i >= 0; i--) {
            OverlayMessage msg = messages.get(i);

            if (!types.contains(msg.getType())) {
                continue;
            }

            if (fadeOutThreshold > 0) {
                int msgMaxFade = msg.getMaxFadeSeconds();
                long threshold = msgMaxFade > 0 ? (msgMaxFade * 1000L) + 2000 : fadeOutThreshold;
                if (currentTime - msg.getTimestamp() >= threshold) {
                    continue;
                }
            }

            if (gameFilterEnabled && msg.getType() == ChatMessageType.SPAM) {
                continue;
            }

            if (bossKcFilterEnabled && msg.isBossKc()) {
                continue;
            }

            // Login notifications don't count against max
            boolean isLoginNotification = msg.getType() == ChatMessageType.LOGINLOGOUTNOTIFICATION;
            if (!isLoginNotification && msgCount >= maxMessages) {
                continue;
            }

            filtered.add(0, msg);
            if (!isLoginNotification) {
                msgCount++;
            }
        }

        return filtered;
    }

    public void clearMessagesForTypes(Set<ChatMessageType> types) {
        messages.removeIf(msg -> types.contains(msg.getType()));
    }

    public void clearAllMessages() {
        messages.clear();
    }

    // --- Utility ---

    private String cleanSender(String name) {
        if (name == null) {
            return null;
        }
        return name.replace('\u00A0', ' ').trim();
    }

    private String tryMergeMessages(String previousMessage, String newMessage) {
        for (MessageMergeRule rule : MESSAGE_MERGE_RULES) {
            if (rule.matches(previousMessage, newMessage)) {
                return rule.merge(previousMessage, newMessage);
            }
        }
        return null;
    }

    private static String stripTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("</?col[^>]*>", "");
    }

    /**
     * Folds a duplicate message into a target's slot: scans {@code pool} for an entry with the same
     * colour-stripped body and sender, and on a hit removes it and returns the combined count.
     *
     * @param pool         the shared message pool, mutated in place (the matched entry is removed)
     * @param strippedBody the target's body with colour tags stripped ({@link #stripTags})
     * @param sender       the target's sender ({@code null} for game messages)
     * @param baseCount    the count the target carries before folding — 1 for a freshly captured
     *                     message, or its preserved count when rebuilt after a node rewrite
     * @param skipIndex    the target's own index when it already lives in {@code pool} (so it isn't
     *                     matched against itself), or -1 when it has not been added yet
     * @return {@code baseCount} plus the matched entry's count, or {@code baseCount} unchanged when
     *         no duplicate is found (pool left untouched)
     */
    static int collapseDuplicate(List<OverlayMessage> pool, String strippedBody, String sender,
            int baseCount, int skipIndex) {
        for (int i = pool.size() - 1; i >= 0; i--) {
            if (i == skipIndex) {
                continue;
            }
            OverlayMessage other = pool.get(i);
            String otherSender = other.getSender();
            if (stripTags(other.getMessage()).equals(strippedBody)
                    && (otherSender == null ? sender == null : otherSender.equals(sender))) {
                pool.remove(i);
                return baseCount + other.getCount();
            }
        }
        return baseCount;
    }

    /**
     * DEBUG aid: prepends the message's {@link ChatMessageType} to the live chatbox line so the
     * type of every message (including ones the plugin doesn't capture) is visible in-game. Only
     * the chatbox node is modified; the overlay pool reads {@code event.getMessage()}, a separate
     * copy, so overlays still render the clean message.
     */
    private void prependDebugType(ChatMessage event) {
        MessageNode node = event.getMessageNode();
        if (node == null) {
            return;
        }
        node.setValue("<col=ff0000>[" + event.getType().name() + "]</col> " + node.getValue());
    }

    private void clearMessagesForCategories(MessageCategory... categories) {
        EnumSet<ChatMessageType> types = EnumSet.noneOf(ChatMessageType.class);
        for (MessageCategory cat : categories) {
            types.addAll(cat.getTypes());
        }
        clearMessagesForTypes(types);
    }

    public boolean isChatboxHidden() {
        return isChatboxMinimized() || isChatboxWidgetHidden();
    }

    private boolean isChatboxMinimized() {
        return client.getVarcIntValue(VarClientID.CHAT_VIEW) == 1337;
    }

    private boolean isChatboxWidgetHidden() {
        Widget chatboxWidget = client.getWidget(InterfaceID.Chatbox.CHATAREA);
        return (chatboxWidget != null && chatboxWidget.isHidden());
    }

    public boolean isGameFilterEnabled() {
        return client.getVarbitValue(VarbitID.GAME_FILTER) == 1;
    }

    public boolean isBossKcFilterEnabled() {
        return client.getVarbitValue(VarbitID.BOSS_KILLCOUNT_FILTERED) == 1;
    }

    private void updateDefaultPosition(net.runelite.client.ui.overlay.Overlay overlay) {
        OverlayPosition defaultPos = client.isResized()
                ? OverlayPosition.ABOVE_CHATBOX_RIGHT
                : OverlayPosition.BOTTOM_LEFT;
        overlay.setPosition(defaultPos);
    }

    private void updateSmartPosition(net.runelite.client.ui.overlay.Overlay overlay) {
        if (!config.smartPositioning()) {
            return;
        }

        OverlayPosition currentPos = overlay.getPreferredPosition() != null
                ? overlay.getPreferredPosition()
                : overlay.getPosition();

        if (isTopPosition(currentPos)) {
            return;
        }

        OverlayPosition targetPos;
        if (!client.isResized()) {
            targetPos = OverlayPosition.BOTTOM_LEFT;
        } else if (isChatboxHidden()) {
            targetPos = OverlayPosition.ABOVE_CHATBOX_RIGHT;
        } else {
            return;
        }

        if (currentPos != targetPos) {
            overlay.setPreferredPosition(targetPos);
        }
    }

    private boolean isTopPosition(OverlayPosition position) {
        return position == OverlayPosition.TOP_CENTER
                || position == OverlayPosition.TOP_RIGHT
                || position == OverlayPosition.TOP_LEFT
                || position == OverlayPosition.CANVAS_TOP_RIGHT;
    }

    private void setPmWidgetsHidden(boolean hidden) {
        setGameframePmContainerHidden(InterfaceID.TOPLEVEL, 36, hidden);
        setGameframePmContainerHidden(InterfaceID.TOPLEVEL_OSRS_STRETCH, 93, hidden);
        setGameframePmContainerHidden(InterfaceID.TOPLEVEL_PRE_EOC, 90, hidden);

        Widget pmContainer = client.getWidget(InterfaceID.PM_CHAT, 0);
        if (pmContainer != null) {
            pmContainer.setHidden(hidden);
            Widget[] dynamicChildren = pmContainer.getDynamicChildren();
            if (dynamicChildren != null) {
                for (Widget child : dynamicChildren) {
                    if (child != null) {
                        child.setHidden(hidden);
                    }
                }
            }
        }
    }

    private void setGameframePmContainerHidden(int group, int child, boolean hidden) {
        Widget container = client.getWidget(group, child);
        if (container != null) {
            container.setHidden(hidden);
        }
    }

    @Provides
    ImprovedChatConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ImprovedChatConfig.class);
    }

    boolean isHoveringGameChatControls() {
        final Point mouse = client.getMouseCanvasPosition();
        return isHoveringWidget(mouse, client.getWidget(InterfaceID.Chatbox.CHAT_ALL)) ||
                isHoveringWidget(mouse, client.getWidget(InterfaceID.Chatbox.CHAT_GAME)) ||
                isHoveringWidget(mouse, client.getWidget(InterfaceID.Chatbox.CHAT_PUBLIC));
    }

    boolean isHoveringWidget(Point mouse, Widget target) {
        if (target == null || target.isHidden()) {
            return false;
        }
        return target.getBounds().contains(mouse.getX(), mouse.getY());
    }
}
