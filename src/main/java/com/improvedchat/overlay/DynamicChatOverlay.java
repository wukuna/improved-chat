package com.improvedchat.overlay;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.ImprovedChatPlugin;
import com.improvedchat.model.FontSize;
import com.improvedchat.model.MessageCategory;
import com.improvedchat.model.OverlayMessage;
import com.improvedchat.model.PlacementMode;
import com.improvedchat.model.TextAlignment;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ChatColorConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * One configurable Improved Chat overlay.
 *
 * <p>Free overlays use RuneLite's normal movable/resizable overlay system. Player-attached
 * overlays are positioned from the local player's canvas point with explicit X/Y offsets.
 */
public class DynamicChatOverlay extends Overlay {
    private static final int MIN_ZOOM = -22;
    private static final int MAX_ZOOM = 1400;
    private static final int BELOW_OFFSET_MIN_ZOOM = 40;
    private static final int BELOW_OFFSET_MAX_ZOOM = 150;
    private static final int ABOVE_OFFSET_MIN_ZOOM = -40;
    private static final int ABOVE_OFFSET_MAX_ZOOM = -120;

    private final ImprovedChatPlugin plugin;
    private final ImprovedChatConfig globalConfig;
    private final Client client;
    private final ChatColorConfig chatColorConfig;
    private OverlayConfig overlayConfig;
    private PlacementMode appliedPlacementMode;

    public DynamicChatOverlay(ImprovedChatPlugin plugin, ImprovedChatConfig globalConfig, Client client,
            ChatColorConfig chatColorConfig, OverlayConfig overlayConfig) {
        super(plugin);
        this.plugin = plugin;
        this.globalConfig = globalConfig;
        this.client = client;
        this.chatColorConfig = chatColorConfig;
        this.overlayConfig = overlayConfig;

        setPosition(client.isResized() ? OverlayPosition.ABOVE_CHATBOX_RIGHT : OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setMinimumSize(150);
        applyPlacementPolicy(true);
        rebuildMenuEntries();
    }

    @Override
    public String getName() {
        return "Improved Chat " + (overlayConfig == null ? "Overlay" : overlayConfig.getId());
    }

    public OverlayConfig getOverlayConfig() {
        return overlayConfig;
    }

    public void setOverlayConfig(OverlayConfig overlayConfig) {
        this.overlayConfig = overlayConfig;
        applyPlacementPolicy(false);
        rebuildMenuEntries();
    }

    private void rebuildMenuEntries() {
        getMenuEntries().clear();
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Clear",
                overlayConfig.getName() + " history"));
    }

    private void applyPlacementPolicy(boolean initial) {
        PlacementMode mode = overlayConfig == null ? PlacementMode.FREE : overlayConfig.getPlacementMode();
        if (!initial && mode == appliedPlacementMode) {
            return;
        }
        appliedPlacementMode = mode;

        setResizable(true);
        if (mode == PlacementMode.FREE) {
            setMovable(true);
            setSnappable(true);
            // Do not clear preferredLocation here. RuneLite owns and persists free-overlay placement.
        } else {
            setMovable(false);
            setSnappable(false);
            setPreferredPosition(null);
            setPreferredLocation(null);
        }
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        applyPlacementPolicy(false);
        if (!shouldRender()) {
            return null;
        }

        FontSize fontSize = overlayConfig.getFontSize();
        FontMetrics metrics = FontStyler.setupGraphics(graphics, fontSize, overlayConfig);
        IndexedSprite[] modIcons = client.getModIcons();

        Dimension preferredSize = getPreferredSize();
        int widgetWidth = preferredSize != null && preferredSize.width > 0
                ? preferredSize.width
                : overlayConfig.getWidgetWidth();
        widgetWidth = Math.max(150, widgetWidth);

        int paddingX = overlayConfig.getPaddingHorizontal();
        int paddingY = overlayConfig.getPaddingVertical();
        int contentWidth = Math.max(32, widgetWidth - paddingX * 2);
        int lineHeight = Math.max(1, metrics.getHeight() - (fontSize == FontSize.SMALL ? 2 : 3) + 1);
        long currentTime = System.currentTimeMillis();
        boolean drawShadow = globalConfig.textShadow();
        boolean wrapText = globalConfig.wrapText();
        boolean retainContextualColours = overlayConfig.isContextualColours();
        boolean hideDuplicateCount = overlayConfig.isHideDuplicateCount();
        PlacementMode placementMode = overlayConfig.getPlacementMode();
        boolean followPlayer = placementMode != PlacementMode.FREE;
        boolean useDynamicHeight = followPlayer || overlayConfig.isDynamicHeight();

        int fadeOutDuration = overlayConfig.getFadeOutDuration();
        long fadeOutMs = fadeOutDuration * 1000L;
        long fadeOutThreshold = fadeOutMs + 5000L;
        int maxMessages = overlayConfig.getMaxMessages();

        List<OverlayMessage> messages = plugin.getMessagesForOverlay(overlayConfig);
        int startIndex = Math.max(0, messages.size() - maxMessages);
        List<RenderLine> renderableLines = new ArrayList<>();

        for (int i = startIndex; i < messages.size(); i++) {
            OverlayMessage msg = messages.get(i);
            int msgMaxFade = msg.getMaxFadeSeconds();
            long msgFadeThreshold = msgMaxFade > 0
                    ? msgMaxFade * 1000L + 5000L
                    : fadeOutThreshold;

            if ((fadeOutDuration == 0 && msgMaxFade == 0)
                    || currentTime - msg.getTimestamp() < msgFadeThreshold) {
                Color inherited = getCategoryColour(msg.getType());
                Color msgColor = OverlayStyleEngine.resolveTextColor(overlayConfig, inherited);
                List<RenderLine> msgLines = ChatRenderUtils.buildMessageLines(
                        msg,
                        metrics,
                        contentWidth,
                        currentTime,
                        fadeOutMs,
                        wrapText,
                        msgColor,
                        retainContextualColours,
                        hideDuplicateCount,
                        fontSize,
                        modIcons,
                        overlayConfig.isShowTimestamp(),
                        globalConfig.timestampFormat(),
                        globalConfig.showChannelName(),
                        chatColorConfig);

                for (RenderLine line : msgLines) {
                    if (line.alpha > 0) {
                        renderableLines.add(line);
                    }
                }
            }
        }

        List<TextSegment> inputSegments = buildInputPreviewSegments(metrics, fontSize, modIcons);
        if (renderableLines.isEmpty() && inputSegments == null) {
            return null;
        }

        int inputExtra = inputSegments == null ? 0 : lineHeight;
        int contentHeight = useDynamicHeight
                ? renderableLines.size() * lineHeight + inputExtra
                : maxMessages * lineHeight + inputExtra;
        int widgetHeight = Math.max(lineHeight + paddingY * 2, contentHeight + paddingY * 2);

        if (followPlayer) {
            translateToPlayer(graphics, placementMode, widgetWidth, widgetHeight);
        }

        Shape originalClip = graphics.getClip();
        graphics.setClip(0, 0, widgetWidth, widgetHeight);

        Color background = overlayConfig.getRenderBackgroundColour();
        if (background.getAlpha() > 0) {
            graphics.setColor(background);
            graphics.fillRect(0, 0, widgetWidth, widgetHeight);
        }

        int y = widgetHeight - paddingY - metrics.getDescent();
        if (inputSegments != null) {
            drawSegments(graphics, inputSegments, 255, y, widgetWidth,
                    paddingX, fontSize, metrics, modIcons, drawShadow);
            y -= lineHeight;
        }

        for (int i = renderableLines.size() - 1; i >= 0; i--) {
            RenderLine line = renderableLines.get(i);
            if (line.alpha <= 0) {
                continue;
            }
            drawSegments(graphics, line.segments, line.alpha, y, widgetWidth,
                    paddingX, fontSize, metrics, modIcons, drawShadow);
            y -= lineHeight;
        }

        graphics.setClip(originalClip);
        AttentionEngine.drawBorder(graphics, overlayConfig, widgetWidth, widgetHeight, currentTime);

        return followPlayer ? null : new Dimension(widgetWidth, widgetHeight);
    }

    private void translateToPlayer(Graphics2D graphics, PlacementMode placementMode,
            int widgetWidth, int widgetHeight) {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        Point playerPoint;
        if (placementMode == PlacementMode.BELOW_PLAYER) {
            playerPoint = localPlayer.getCanvasTextLocation(graphics, "", 0);
        } else {
            playerPoint = localPlayer.getCanvasTextLocation(graphics, "", localPlayer.getLogicalHeight());
        }
        if (playerPoint == null) {
            return;
        }

        int x = playerPoint.getX() - widgetWidth / 2 + overlayConfig.getOffsetX();
        int y = playerPoint.getY() + calculateZoomOffset(placementMode) - widgetHeight / 2
                + overlayConfig.getOffsetY();
        graphics.translate(x - getBounds().x, y - getBounds().y);
    }

    private boolean shouldRender() {
        if (!overlayConfig.isShow() || client.getGameState() != GameState.LOGGED_IN) {
            return false;
        }
        return overlayConfig.isAlwaysVisible() || plugin.isChatboxHidden();
    }

    private int calculateZoomOffset(PlacementMode placementMode) {
        int zoom = client.get3dZoom();
        double normalizedZoom = (double) (zoom - MIN_ZOOM) / (MAX_ZOOM - MIN_ZOOM);
        normalizedZoom = Math.max(0, Math.min(1, normalizedZoom));

        int minOffset;
        int maxOffset;
        if (placementMode == PlacementMode.BELOW_PLAYER) {
            minOffset = BELOW_OFFSET_MIN_ZOOM;
            maxOffset = BELOW_OFFSET_MAX_ZOOM;
        } else {
            minOffset = ABOVE_OFFSET_MIN_ZOOM;
            maxOffset = ABOVE_OFFSET_MAX_ZOOM;
        }
        return (int) (minOffset + normalizedZoom * (maxOffset - minOffset));
    }

    private Color getCategoryColour(ChatMessageType type) {
        if (type == ChatMessageType.DIDYOUKNOW) {
            return globalConfig.didYouKnowColour();
        }
        if (type == ChatMessageType.BROADCAST) {
            return globalConfig.broadcastColour();
        }

        switch (MessageCategory.fromType(type)) {
            case GAME:
            case GAME_CLAN:
                return globalConfig.gameColour();
            case TRADE:
                return globalConfig.tradeColour();
            case CHALLENGE:
                return globalConfig.challengeColour();
            case PUBLIC_CHAT:
                return globalConfig.publicColour();
            case AUTO:
                return globalConfig.autoColour();
            case FRIENDS_CHAT:
                return globalConfig.friendsColour();
            case CLAN_CHAT:
                return globalConfig.clanColour();
            case GUEST_CLAN_CHAT:
                return globalConfig.guestClanColour();
            case GIM_CLAN_CHAT:
                return globalConfig.gimClanColour();
            case PRIVATE:
                return globalConfig.privateColour();
            default:
                return globalConfig.gameColour();
        }
    }

    private int calculateLineWidth(List<TextSegment> segments, FontMetrics metrics) {
        int width = 0;
        for (TextSegment segment : segments) {
            width += segment.iconId >= 0 ? segment.width : metrics.stringWidth(segment.text);
        }
        return width;
    }

    static int calculateAlignedX(TextAlignment alignment, int paddingX, int contentWidth, int lineWidth) {
        int remaining = Math.max(0, contentWidth - lineWidth);
        if (alignment == TextAlignment.RIGHT) {
            return paddingX + remaining;
        }
        if (alignment == TextAlignment.CENTER) {
            return paddingX + remaining / 2;
        }
        return paddingX;
    }

    private void drawSegments(Graphics2D graphics, List<TextSegment> segments, int alpha, int y,
            int widgetWidth, int paddingX, FontSize fontSize,
            FontMetrics metrics, IndexedSprite[] modIcons, boolean drawShadow) {
        int lineWidth = calculateLineWidth(segments, metrics);
        int contentWidth = Math.max(0, widgetWidth - paddingX * 2);
        int x = calculateAlignedX(overlayConfig.getTextAlignment(), paddingX, contentWidth, lineWidth);

        for (TextSegment segment : segments) {
            if (segment.iconId >= 0) {
                BufferedImage img = ChatRenderUtils.getModIconImage(segment.iconId, modIcons);
                if (img != null) {
                    x += ChatRenderUtils.drawIcon(graphics, img, fontSize, metrics, x, y);
                } else {
                    x += segment.width;
                }
            } else {
                Color segmentColor = segment.color == null ? Color.WHITE : segment.color;
                x += ChatRenderUtils.drawText(graphics, segment.text, segmentColor, alpha, x, y,
                        drawShadow, metrics);
            }
        }
    }

    private List<TextSegment> buildInputPreviewSegments(FontMetrics metrics, FontSize fontSize,
            IndexedSprite[] modIcons) {
        if (!overlayConfig.isShowInputPreview()) {
            return null;
        }

        String typed = client.getVarcStrValue(VarClientID.CHATINPUT);
        String text = resolveInputText(typed);
        if (text == null || text.isEmpty()) {
            return null;
        }

        boolean hasTyped = typed != null && !typed.isEmpty();
        if (overlayConfig.isPreviewOnlyWhenTyping() && !hasTyped) {
            return null;
        }

        Color inherited = hasTyped ? globalConfig.publicColour() : Color.WHITE;
        Color messageColor = OverlayStyleEngine.resolveTextColor(overlayConfig, inherited);
        Color nameColor = overlayConfig.isOverlayColorOverrideEnabled() ? messageColor : Color.WHITE;

        List<TextSegment> segments = new ArrayList<>();
        int colonIdx = text.indexOf(':');
        if (colonIdx >= 0) {
            segments.addAll(ChatRenderUtils.parseTextWithColoursAndIcons(
                    text.substring(0, colonIdx + 1), metrics, modIcons, false,
                    nameColor, fontSize, chatColorConfig));
            segments.addAll(ChatRenderUtils.parseTextWithColoursAndIcons(
                    text.substring(colonIdx + 1), metrics, modIcons, false,
                    messageColor, fontSize, chatColorConfig));
        } else {
            segments.addAll(ChatRenderUtils.parseTextWithColoursAndIcons(
                    text, metrics, modIcons, false, messageColor, fontSize, chatColorConfig));
        }
        return segments;
    }

    private String resolveInputText(String typed) {
        Widget input = client.getWidget(InterfaceID.Chatbox.INPUT);
        if (input != null) {
            String text = input.getText();
            if (text != null && !text.isEmpty()) {
                return text;
            }
        }

        Player local = client.getLocalPlayer();
        if (local == null || local.getName() == null) {
            return null;
        }
        return local.getName() + ": " + (typed == null ? "" : typed) + "*";
    }
}
