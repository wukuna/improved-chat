package com.improvedchat.overlay;

import com.improvedchat.model.AttentionSpeed;
import com.improvedchat.model.AttentionStopMode;
import com.improvedchat.model.AttentionTrigger;
import com.improvedchat.model.BorderThickness;
import com.improvedchat.model.FontSize;
import com.improvedchat.model.MessageCategory;
import com.improvedchat.model.PlacementMode;
import com.improvedchat.model.TextAlignment;
import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import net.runelite.api.ChatMessageType;

/** Per-overlay settings for Improved Chat. */
public final class OverlayConfig {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private String id = UUID.randomUUID().toString();
    private String name = "Custom Overlay";
    private Set<ChatMessageType> messageTypes = EnumSet.noneOf(ChatMessageType.class);
    private int maxMessages = 10;
    private int fadeOutDuration;
    private int widgetWidth = 512;

    private int paddingHorizontal = 4;
    private int paddingVertical = 3;
    private int offsetX;
    private int offsetY;
    private PlacementMode placementMode = PlacementMode.FREE;
    private TextAlignment textAlignment;

    private boolean overlayColorOverrideEnabled;
    private Color overlayTextColor = Color.WHITE;

    private boolean dynamicHeight;
    private boolean hideDuplicateCount;
    private boolean contextualColours = true;
    private boolean showTimestamp;
    private boolean showInputPreview;
    private boolean previewOnlyWhenTyping;
    private boolean show = true;
    private boolean alwaysVisible;
    private FontSize fontSize = FontSize.REGULAR;
    private String fontFamily = "RuneScape";
    private boolean boldText;
    private Color backgroundColour = TRANSPARENT;

    private boolean borderEnabled;
    private Color borderColour = Color.WHITE;
    private BorderThickness borderThickness = BorderThickness.NORMAL;

    private boolean flashEnabled;
    private AttentionTrigger attentionTrigger = AttentionTrigger.FLASH_RULES_ONLY;
    private boolean flashWhileFocused;
    private AttentionStopMode attentionStopMode = AttentionStopMode.TIMER_OR_REFOCUS;
    private int flashDurationSeconds = 5;
    private AttentionSpeed attentionSpeed = AttentionSpeed.NORMAL;
    private boolean flashMessage = true;
    private boolean flashBorder;
    private boolean flashBackground;

    public OverlayConfig() {
    }

    public static OverlayConfig createDefault(String name, Set<ChatMessageType> types, boolean alwaysVisible) {
        OverlayConfig config = new OverlayConfig();
        config.name = name;
        config.messageTypes = types == null || types.isEmpty()
                ? EnumSet.noneOf(ChatMessageType.class)
                : EnumSet.copyOf(types);
        config.alwaysVisible = alwaysVisible;
        return config;
    }

    public static OverlayConfig defaultAllOverlay() {
        EnumSet<ChatMessageType> types = EnumSet.noneOf(ChatMessageType.class);
        for (MessageCategory category : MessageCategory.values()) {
            if (category != MessageCategory.PRIVATE) {
                types.addAll(category.getTypes());
            }
        }
        OverlayConfig config = createDefault("Game Alerts", types, false);
        config.showInputPreview = true;
        config.dynamicHeight = true;
        return config;
    }

    public static OverlayConfig defaultPrivateOverlay() {
        EnumSet<ChatMessageType> types = EnumSet.noneOf(ChatMessageType.class);
        types.addAll(MessageCategory.PRIVATE.getTypes());
        OverlayConfig config = createDefault("Private Chat", types, true);
        config.contextualColours = true;
        return config;
    }

    public String getId() {
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public String getName() { return name; }
    public void setName(String value) {
        name = value == null || value.trim().isEmpty() ? "Untitled Overlay" : value.trim();
    }

    public Set<ChatMessageType> getMessageTypes() {
        if (messageTypes == null) {
            messageTypes = EnumSet.noneOf(ChatMessageType.class);
        }
        return messageTypes;
    }

    public void setMessageTypes(Set<ChatMessageType> value) {
        messageTypes = value == null || value.isEmpty()
                ? EnumSet.noneOf(ChatMessageType.class)
                : EnumSet.copyOf(value);
    }

    public int getMaxMessages() { return maxMessages; }
    public void setMaxMessages(int value) { maxMessages = clamp(value, 1, 20); }

    public int getFadeOutDuration() { return fadeOutDuration; }
    public void setFadeOutDuration(int value) { fadeOutDuration = clamp(value, 0, 300); }

    public int getWidgetWidth() { return widgetWidth; }
    public void setWidgetWidth(int value) { widgetWidth = clamp(value, 150, 1024); }

    public int getPaddingHorizontal() { return paddingHorizontal; }
    public void setPaddingHorizontal(int value) { paddingHorizontal = clamp(value, 0, 50); }

    public int getPaddingVertical() { return paddingVertical; }
    public void setPaddingVertical(int value) { paddingVertical = clamp(value, 0, 50); }

    public int getOffsetX() { return offsetX; }
    public void setOffsetX(int value) { offsetX = clamp(value, -500, 500); }

    public int getOffsetY() { return offsetY; }
    public void setOffsetY(int value) { offsetY = clamp(value, -500, 500); }

    public PlacementMode getPlacementMode() {
        return placementMode == null ? PlacementMode.FREE : placementMode;
    }

    public void setPlacementMode(PlacementMode mode) {
        placementMode = mode == null ? PlacementMode.FREE : mode;
    }

    public TextAlignment getTextAlignment() {
        if (textAlignment != null) {
            return textAlignment;
        }
        return getPlacementMode() == PlacementMode.FREE ? TextAlignment.LEFT : TextAlignment.CENTER;
    }

    public void setTextAlignment(TextAlignment value) {
        textAlignment = value;
    }

    public boolean isOverlayColorOverrideEnabled() { return overlayColorOverrideEnabled; }
    public void setOverlayColorOverrideEnabled(boolean enabled) { overlayColorOverrideEnabled = enabled; }

    public Color getOverlayTextColor() { return overlayTextColor == null ? Color.WHITE : overlayTextColor; }
    public void setOverlayTextColor(Color color) { overlayTextColor = color == null ? Color.WHITE : color; }

    public boolean isDynamicHeight() { return dynamicHeight; }
    public void setDynamicHeight(boolean value) { dynamicHeight = value; }

    public boolean isHideDuplicateCount() { return hideDuplicateCount; }
    public void setHideDuplicateCount(boolean value) { hideDuplicateCount = value; }

    public boolean isContextualColours() { return !overlayColorOverrideEnabled && contextualColours; }
    public void setContextualColours(boolean value) { contextualColours = value; }

    public boolean isShowTimestamp() { return showTimestamp; }
    public void setShowTimestamp(boolean value) { showTimestamp = value; }

    public boolean isShowInputPreview() { return showInputPreview; }
    public void setShowInputPreview(boolean value) { showInputPreview = value; }

    public boolean isPreviewOnlyWhenTyping() { return previewOnlyWhenTyping; }
    public void setPreviewOnlyWhenTyping(boolean value) { previewOnlyWhenTyping = value; }

    public boolean isShow() { return show; }
    public void setShow(boolean value) { show = value; }

    public boolean isAlwaysVisible() { return alwaysVisible; }
    public void setAlwaysVisible(boolean value) { alwaysVisible = value; }

    public FontSize getFontSize() { return fontSize == null ? FontSize.REGULAR : fontSize; }
    public void setFontSize(FontSize value) { fontSize = value == null ? FontSize.REGULAR : value; }

    public String getFontFamily() {
        return fontFamily == null || fontFamily.trim().isEmpty() ? "RuneScape" : fontFamily;
    }

    public void setFontFamily(String value) {
        fontFamily = value == null || value.trim().isEmpty() ? "RuneScape" : value.trim();
    }

    public boolean isBoldText() { return boldText; }
    public void setBoldText(boolean value) { boldText = value; }

    public Color getBackgroundColour() { return backgroundColour == null ? TRANSPARENT : backgroundColour; }
    public void setBackgroundColour(Color value) { backgroundColour = value == null ? TRANSPARENT : value; }

    public boolean isBackgroundEnabled() { return getBackgroundColour().getAlpha() > 0; }

    public void setBackgroundEnabled(boolean enabled) {
        Color current = getBackgroundColour();
        if (enabled && current.getAlpha() == 0) {
            backgroundColour = new Color(current.getRed(), current.getGreen(), current.getBlue(), 150);
        } else if (!enabled && current.getAlpha() > 0) {
            backgroundColour = new Color(current.getRed(), current.getGreen(), current.getBlue(), 0);
        }
    }

    public Color getRenderBackgroundColour() {
        return AttentionEngine.adjustBackground(this, getBackgroundColour(), System.currentTimeMillis());
    }

    public boolean isBorderEnabled() { return borderEnabled; }
    public void setBorderEnabled(boolean value) { borderEnabled = value; }

    public Color getBorderColour() { return borderColour == null ? Color.WHITE : borderColour; }
    public void setBorderColour(Color value) { borderColour = value == null ? Color.WHITE : value; }

    public BorderThickness getBorderThickness() {
        return borderThickness == null ? BorderThickness.NORMAL : borderThickness;
    }
    public void setBorderThickness(BorderThickness value) {
        borderThickness = value == null ? BorderThickness.NORMAL : value;
    }

    public boolean isFlashEnabled() { return flashEnabled; }
    public void setFlashEnabled(boolean value) { flashEnabled = value; }

    public AttentionTrigger getAttentionTrigger() {
        return attentionTrigger == null ? AttentionTrigger.FLASH_RULES_ONLY : attentionTrigger;
    }
    public void setAttentionTrigger(AttentionTrigger value) {
        attentionTrigger = value == null ? AttentionTrigger.FLASH_RULES_ONLY : value;
    }

    public boolean isFlashWhileFocused() { return flashWhileFocused; }
    public void setFlashWhileFocused(boolean value) { flashWhileFocused = value; }

    public AttentionStopMode getAttentionStopMode() {
        return attentionStopMode == null ? AttentionStopMode.TIMER_OR_REFOCUS : attentionStopMode;
    }
    public void setAttentionStopMode(AttentionStopMode value) {
        attentionStopMode = value == null ? AttentionStopMode.TIMER_OR_REFOCUS : value;
    }

    public int getFlashDurationSeconds() { return clamp(flashDurationSeconds, 1, 60); }
    public void setFlashDurationSeconds(int value) { flashDurationSeconds = clamp(value, 1, 60); }

    public AttentionSpeed getAttentionSpeed() {
        return attentionSpeed == null ? AttentionSpeed.NORMAL : attentionSpeed;
    }
    public void setAttentionSpeed(AttentionSpeed value) {
        attentionSpeed = value == null ? AttentionSpeed.NORMAL : value;
    }

    public boolean isFlashMessage() { return flashMessage; }
    public void setFlashMessage(boolean value) { flashMessage = value; }

    public boolean isFlashBorder() { return flashBorder; }
    public void setFlashBorder(boolean value) { flashBorder = value; }

    public boolean isFlashBackground() { return flashBackground; }
    public void setFlashBackground(boolean value) { flashBackground = value; }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
