package com.improvedchat;

import com.improvedchat.model.FontSize;
import com.improvedchat.model.AttentionSpeed;
import com.improvedchat.model.AttentionStopMode;
import com.improvedchat.model.AttentionTrigger;
import com.improvedchat.model.BorderThickness;
import com.improvedchat.model.AnimatedTextStyle;
import com.improvedchat.model.MessageCategory;
import com.improvedchat.model.PlacementMode;
import com.improvedchat.model.TextAlignment;
import com.improvedchat.overlay.OverlayConfig;
import com.improvedchat.overlay.OverlayFilterMode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.improvedchat.overlay.OverlayMessageFilter;
import com.improvedchat.model.OverlayMessage;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import net.runelite.api.ChatMessageType;
import net.runelite.client.config.Notification;
import net.runelite.client.config.NotificationSound;
import net.runelite.client.config.RequestFocusType;
import net.runelite.client.config.FlashNotification;
import net.runelite.client.ui.PluginPanel;

/** Compact side panel for Improved Chat. */
public class ImprovedChatPanel extends PluginPanel {
    private static final Color BG = new Color(31, 34, 39);
    private static final Color CARD = new Color(41, 45, 52);
    private static final Color CARD_ALT = new Color(49, 54, 62);
    private static final Color CONTROL = new Color(27, 30, 35);
    private static final Color BORDER = new Color(68, 74, 84);
    private static final Color TEXT = new Color(232, 235, 240);
    private static final Color MUTED = new Color(151, 159, 171);
    private static final Color ACCENT = new Color(61, 181, 218);
    private static final Color DANGER = new Color(225, 86, 86);
    private static final String[] FONT_FAMILIES = {
            "RuneScape", "Arial", "Verdana", "Tahoma", "Segoe UI", "Georgia", "Courier New"
    };
    private static final PlacementMode[] PLACEMENT_MODES = {
            PlacementMode.FREE, PlacementMode.BELOW_PLAYER, PlacementMode.ABOVE_PLAYER
    };

    private final ImprovedChatPlugin plugin;
    private final Map<String, Boolean> openSections = new HashMap<>();
    private final Map<String, JButton> overlaySelectors = new HashMap<>();
    private final Map<String, Integer> selectedTabs = new HashMap<>();
    private String editingOverlayId;
    private String renderedOverlayId;
    private JScrollPane pageScroll;
    private int rebuildVersion;

    public ImprovedChatPanel(ImprovedChatPlugin plugin) {
        super(false);
        this.plugin = plugin;
        setLayout(new BorderLayout());
        setBackground(BG);
        rebuild();
    }

    public final void rebuild() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::rebuild);
            return;
        }
        boolean samePage = java.util.Objects.equals(renderedOverlayId, editingOverlayId);
        int scrollPosition = samePage && pageScroll != null ? pageScroll.getVerticalScrollBar().getValue() : 0;
        int version = ++rebuildVersion;
        overlaySelectors.clear();
        removeAll();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildOverlaysPage(), BorderLayout.CENTER);
        renderedOverlayId = editingOverlayId;
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            if (version != rebuildVersion) return;
            if (pageScroll != null) pageScroll.getVerticalScrollBar().setValue(scrollPosition);
        });
    }

    private Component buildHeader() {
        JPanel root = panel(BG);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(9, 7, 7, 7));


        JLabel title = label("IMPROVED CHAT", TEXT, 15, Font.BOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);
        root.add(help("Create and edit overlays here. Chatbox and cleanup options are in plugin settings."));
        JButton settings = button("OPEN PLUGIN SETTINGS");
        settings.setAlignmentX(Component.LEFT_ALIGNMENT);
        settings.addActionListener(e -> plugin.openSettings());
        root.add(settings);
        if (plugin.getConfigRecoveryMessage() != null) root.add(help(plugin.getConfigRecoveryMessage()));
        return root;
    }


    private Component buildOverlaysPage() {
        if (findOverlay(editingOverlayId) == null) {
            editingOverlayId = plugin.getOverlayConfigs().isEmpty() ? null : plugin.getOverlayConfigs().get(0).getId();
        }
        WidthTrackingPanel body = verticalPage();
        body.add(pageTitle("OVERLAYS"));
        body.add(Box.createVerticalStrut(6));

        if (plugin.getOverlayConfigs().isEmpty()) {
            body.add(help("No overlays yet. Create one from a preset, then choose where and when it appears."));
        }
        for (OverlayConfig oc : plugin.getOverlayConfigs()) {
            body.add(overlayCard(oc));
            body.add(Box.createVerticalStrut(6));
        }

        JButton add = button("+ CREATE OVERLAY");
        add.setBackground(ACCENT.darker());
        add.setAlignmentX(Component.LEFT_ALIGNMENT);
        add.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
        add.addActionListener(e -> showCreateOverlayMenu(add));
        body.add(add);
        OverlayConfig selected = findOverlay(editingOverlayId);
        if (selected != null) {
            body.add(Box.createVerticalStrut(10));
            body.add(buildOverlayEditor(selected));
        }
        body.add(help("Changes save automatically."));
        body.add(Box.createVerticalGlue());
        return scroll(body);
    }

    private Component overlayCard(OverlayConfig oc) {
        JPanel card = panel(CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(oc.getId().equals(editingOverlayId) ? ACCENT : BORDER),
                BorderFactory.createEmptyBorder(4, 5, 4, 5)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel top = panel(CARD);
        top.setLayout(new BorderLayout(4, 0));
        JButton edit = button(oc.getName() + "  ›");
        edit.setName("overlay-selector");
        edit.setHorizontalAlignment(JButton.LEFT);
        edit.setToolTipText("Edit " + oc.getName());
        edit.addActionListener(e -> { editingOverlayId = oc.getId(); rebuild(); });
        overlaySelectors.put(oc.getId(), edit);
        top.add(edit, BorderLayout.CENTER);
        JCheckBox enabled = new JCheckBox("", oc.isShow());
        enabled.setToolTipText("Enable " + oc.getName());
        enabled.getAccessibleContext().setAccessibleName("Enable " + oc.getName());
        enabled.setOpaque(false);
        enabled.addActionListener(e -> { oc.setShow(enabled.isSelected()); saveOverlay(); rebuild(); });
        top.add(enabled, BorderLayout.EAST);
        card.add(top);
        card.add(compactMeta(messageSummary(oc) + "  •  " + fadeLabel(oc)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private Component buildOverlayEditor(OverlayConfig oc) {
        WidthTrackingPanel body = verticalPage();

        JPanel messages = panel(CARD);
        messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
        JPanel looks = panel(CARD);
        looks.setLayout(new BoxLayout(looks, BoxLayout.Y_AXIS));
        JPanel alerts = panel(CARD);
        alerts.setLayout(new BoxLayout(alerts, BoxLayout.Y_AXIS));
        JButton preview = button("PREVIEW (10 SECONDS)");
        preview.addActionListener(e -> plugin.previewOverlay(oc));
        body.add(pageTitle(oc.getName()));
        body.add(preview);
        body.add(help("Preview requires login. Samples bypass filters and visibility without entering chat history."));

        messages.add(accordion(oc, "general", "MESSAGES & VISIBILITY", true,
                textFieldRow("Name", oc.getName(), v -> { oc.setName(v); saveOverlay(); }),
                checkRow("Enabled", oc.isShow(), v -> { oc.setShow(v); saveOverlay(); rebuild(); }),
                comboRow("Show overlay", new String[] {"With chatbox hidden", "With chatbox open or hidden"},
                        oc.isAlwaysVisible() ? "With chatbox open or hidden" : "With chatbox hidden",
                        v -> { oc.setAlwaysVisible("With chatbox open or hidden".equals(v)); saveOverlay(); }),
                help("The overlay appears when it has messages. Empty overlays stay hidden."),
                messageTypesRow(oc),
                messageSelectionHelp(oc),
                spinnerRow("Max Messages", oc.getMaxMessages(), 1, 20, 1, v -> { oc.setMaxMessages(v); saveOverlay(); }),
                spinnerRow("Fade (sec)", oc.getFadeOutDuration(), 0, 300, 1, v -> { oc.setFadeOutDuration(v); saveOverlay(); })
        ));

        JPanel appearance = panel(CARD);
        appearance.setLayout(new BoxLayout(appearance, BoxLayout.Y_AXIS));
        appearance.add(comboRow("Text Size", FontSize.values(), oc.getFontSize(), v -> { oc.setFontSize(v); saveOverlay(); }));
        appearance.add(comboRow("Text Font", FONT_FAMILIES, oc.getFontFamily(), v -> { oc.setFontFamily(v); saveOverlay(); }));
        appearance.add(comboRow("Text Alignment", TextAlignment.values(), oc.getTextAlignment(), v -> { oc.setTextAlignment(v); saveOverlay(); }));
        appearance.add(checkRow("Bold", oc.isBoldText(), v -> { oc.setBoldText(v); saveOverlay(); }));
        appearance.add(spinnerRow("Width (px)", plugin.getOverlayWidth(oc), 150, Math.max(1024, plugin.getOverlayWidth(oc)), 8, v -> plugin.setOverlayWidth(oc, v)));
        appearance.add(spinnerRow("Horizontal Padding", oc.getPaddingHorizontal(), 0, 50, 1, v -> { oc.setPaddingHorizontal(v); saveOverlay(); }));
        appearance.add(spinnerRow("Vertical Padding", oc.getPaddingVertical(), 0, 50, 1, v -> { oc.setPaddingVertical(v); saveOverlay(); }));
        appearance.add(checkRow("Background", oc.isBackgroundEnabled(), v -> { oc.setBackgroundEnabled(v); saveOverlay(); rebuild(); }));
        if (oc.isBackgroundEnabled()) {
            appearance.add(colorRow("Background Color", oc.getBackgroundColour(), v -> {
                int a = oc.getBackgroundColour().getAlpha();
                Color c = new Color(v.getRed(), v.getGreen(), v.getBlue(), a);
                oc.setBackgroundColour(c);
                saveOverlay();
            }));
        }
        if (oc.isBackgroundEnabled()) {
            appearance.add(spinnerRow("Background opacity (%)", Math.max(1, Math.round(oc.getBackgroundColour().getAlpha() * 100f / 255)), 1, 100, 1, v -> {
                Color c = oc.getBackgroundColour();
                oc.setBackgroundColour(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(v * 255f / 100)));
                saveOverlay();
            }));
            appearance.add(help("100% is solid. Turn Background off for full transparency."));
        }
        appearance.add(checkRow("Border", oc.isBorderEnabled(), v -> { oc.setBorderEnabled(v); saveOverlay(); rebuild(); }));
        if (oc.isBorderEnabled()) {
            appearance.add(checkRow("RGB Clockwise Border (Beta)", oc.isRgbBorder(), v -> { oc.setRgbBorder(v); saveOverlay(); rebuild(); }));
            appearance.add(colorRow("Border Color", oc.getBorderColour(), v -> { oc.setBorderColour(v); saveOverlay(); }));
            appearance.add(comboRow("Border Thickness", BorderThickness.values(), oc.getBorderThickness(), v -> { oc.setBorderThickness(v); saveOverlay(); }));
        }
        appearance.add(comboRow("Text effect", AnimatedTextStyle.values(), oc.getAnimatedTextStyle(), v -> { oc.setAnimatedTextStyle(v); saveOverlay(); rebuild(); }));
        if (oc.getAnimatedTextStyle() == AnimatedTextStyle.RGB_STATIC) {
            appearance.add(comboRow("RGB spread", RainbowStyle.values(), oc.getTextSettings().rainbowStyle(), v -> { oc.getTextSettings().setSpread(v); saveOverlay(); }));
        }
        if (oc.getAnimatedTextStyle() == AnimatedTextStyle.RGB_CYCLE
                || oc.getTextSettings().getHighlightStyle() == AnimatedTextStyle.RGB_CYCLE) {
            appearance.add(comboRow("Cycle palette", new String[] {"Full spectrum", "Custom pair"},
                    oc.isCustomRgbCycle() ? "Custom pair" : "Full spectrum",
                    v -> { oc.setCustomRgbCycle("Custom pair".equals(v)); saveOverlay(); rebuild(); }));
            if (oc.isCustomRgbCycle()) {
                appearance.add(colorRow("Color 1", oc.getRgbColor1(), v -> { oc.setRgbColor1(v); saveOverlay(); }));
                appearance.add(colorRow("Color 2", oc.getRgbColor2(), v -> { oc.setRgbColor2(v); saveOverlay(); }));
            }
        }
        if (oc.isRgbBorder() || oc.getAnimatedTextStyle() == AnimatedTextStyle.RGB_CYCLE
                || oc.getAnimatedTextStyle() == AnimatedTextStyle.RGB_WAVE
                || oc.getTextSettings().getHighlightStyle() == AnimatedTextStyle.RGB_CYCLE
                || oc.getTextSettings().getHighlightStyle() == AnimatedTextStyle.RGB_WAVE) {
            appearance.add(spinnerRow("RGB cycle (seconds)", oc.getRgbCycleSeconds(), 2, 30, 1, v -> { oc.setRgbCycleSeconds(v); saveOverlay(); }));
            appearance.add(help("Beta: effects apply only to this overlay. Cycling fades between two colors or through the full spectrum. Wave uses all colors. Icons and notification flashes stay unchanged. Higher seconds = slower."));
        }
        appearance.add(checkRow("Timestamps", oc.isShowTimestamp(), v -> { oc.setShowTimestamp(v); saveOverlay(); }));
        looks.add(accordionPanel(oc, "appearance", "TEXT & FRAME", true, appearance));

        JPanel positionContent = panel(CARD);
        positionContent.setLayout(new BoxLayout(positionContent, BoxLayout.Y_AXIS));
        positionContent.add(comboRow("Mode", PLACEMENT_MODES, oc.getPlacementMode(), v -> {
            oc.setPlacementMode(v);
            saveOverlay();
            rebuild();
        }));
        if (oc.getPlacementMode() == PlacementMode.ABOVE_PLAYER || oc.getPlacementMode() == PlacementMode.BELOW_PLAYER) {
            positionContent.add(spinnerRow("X Offset", oc.getOffsetX(), -500, 500, 1, v -> { oc.setOffsetX(v); saveOverlay(); }));
            positionContent.add(spinnerRow("Y Offset", oc.getOffsetY(), -500, 500, 1, v -> { oc.setOffsetY(v); saveOverlay(); }));
        }
        if (oc.getPlacementMode() == PlacementMode.FREE) positionContent.add(help(plugin.movementHint()));
        looks.add(accordionPanel(oc, "position", "POSITION", false, positionContent));

        messages.add(accordionPanel(oc, "filtering", "MESSAGE FILTERING", false, buildFiltering(oc)));

        JPanel clanDisplay = panel(CARD);
        clanDisplay.setLayout(new BoxLayout(clanDisplay, BoxLayout.Y_AXIS));
        clanDisplay.add(checkRow("Show Clan Rank Icons", oc.isShowClanRankIcons(), v -> {
            oc.setShowClanRankIcons(v);
            saveOverlay();
        }));
        clanDisplay.add(checkRow("Show Offline Status", oc.isShowOfflineStatus(), v -> {
            oc.setShowOfflineStatus(v);
            saveOverlay();
            rebuild();
        }));
        if (oc.isShowOfflineStatus()) {
            clanDisplay.add(checkRow("Show Offline Icon", oc.isShowOfflineIcon(), v -> {
                oc.setShowOfflineIcon(v);
                saveOverlay();
            }));
            clanDisplay.add(checkRow("Color Offline Names", oc.isColorOfflineNames(), v -> {
                oc.setColorOfflineNames(v);
                saveOverlay();
                rebuild();
            }));
            if (oc.isColorOfflineNames()) {
                clanDisplay.add(colorRow("Offline Color", oc.getOfflineColor(), v -> {
                    oc.setOfflineColor(v);
                    saveOverlay();
                }));
            }
        }
        if (hasClanMessages(oc)) messages.add(accordionPanel(oc, "clanDisplay", "CLAN DISPLAY", false, clanDisplay));

        JPanel overlayColors = panel(CARD);
        overlayColors.setLayout(new BoxLayout(overlayColors, BoxLayout.Y_AXIS));
        overlayColors.add(checkRow("Text Override", oc.isOverlayColorOverrideEnabled(), v -> {
            oc.setOverlayColorOverrideEnabled(v);
            saveOverlay();
            rebuild();
        }));
        if (oc.isOverlayColorOverrideEnabled()) {
            overlayColors.add(colorRow("Text Color", oc.getOverlayTextColor(), v -> { oc.setOverlayTextColor(v); saveOverlay(); }));
        }
        overlayColors.add(controlRow("Scope", label("Overlay Only", ACCENT, 9, Font.BOLD)));
        overlayColors.add(checkRow("Retain message colors", oc.isContextualColours(), v -> { oc.setContextualColours(v); saveOverlay(); }));
        overlayColors.add(comboRow("RuneLite color source", ChatColorSource.values(), oc.getTextSettings().chatColorSource(), v -> { oc.getTextSettings().setSource(v); saveOverlay(); }));
        JPanel fallback = panel(CARD);
        fallback.setLayout(new BoxLayout(fallback, BoxLayout.Y_AXIS));
        fallback.add(help("Preserved channel colors used when RuneLite has no configured color. These affect overlays only."));
        String[] channelNames = {"Game", "Public", "Private", "Friends chat", "Clan", "Guest clan", "GIM", "Trade", "Challenge", "Did you know", "Broadcast", "Autochat"};
        for (int i = 0; i < channelNames.length; i++) {
            final int index = i;
            fallback.add(colorRow(channelNames[i], oc.getTextSettings().getChannelColor(i), v -> { oc.getTextSettings().setChannelColor(index, v); saveOverlay(); }));
        }
        overlayColors.add(accordionPanel(oc, "fallbackColors", "CHANNEL FALLBACK COLORS", false, fallback));
        looks.add(accordionPanel(oc, "overlayColors", "TEXT COLORS", false, overlayColors));

        alerts.add(notificationControls(oc));

        JPanel attention = panel(CARD);
        attention.setLayout(new BoxLayout(attention, BoxLayout.Y_AXIS));
        attention.add(checkRow("Flash", oc.isFlashEnabled(), v -> { oc.setFlashEnabled(v); saveOverlay(); rebuild(); }));
        if (oc.isFlashEnabled()) {
            attention.add(comboRow("Trigger", AttentionTrigger.values(), oc.getAttentionTrigger(), v -> { oc.setAttentionTrigger(v); saveOverlay(); }));
            attention.add(checkRow("Flash While Focused", oc.isFlashWhileFocused(), v -> { oc.setFlashWhileFocused(v); saveOverlay(); }));
            attention.add(comboRow("Stop", AttentionStopMode.values(), oc.getAttentionStopMode(), v -> { oc.setAttentionStopMode(v); saveOverlay(); }));
            attention.add(spinnerRow("Duration (sec)", oc.getFlashDurationSeconds(), 1, 60, 1, v -> { oc.setFlashDurationSeconds(v); saveOverlay(); }));
            attention.add(comboRow("Speed", AttentionSpeed.values(), oc.getAttentionSpeed(), v -> { oc.setAttentionSpeed(v); saveOverlay(); }));
            attention.add(checkRow("Message", oc.isFlashMessage(), v -> { oc.setFlashMessage(v); saveOverlay(); }));
            attention.add(checkRow("Border", oc.isFlashBorder(), v -> { oc.setFlashBorder(v); saveOverlay(); }));
            attention.add(checkRow("Background", oc.isFlashBackground(), v -> { oc.setFlashBackground(v); saveOverlay(); }));
        }
        alerts.add(accordionPanel(oc, "attention", "OVERLAY FLASH", false, attention));

        looks.add(accordion(oc, "advanced", "LAYOUT & INPUT", false,
                checkRow("Dynamic Height", oc.isDynamicHeight(), v -> { oc.setDynamicHeight(v); saveOverlay(); }),
                checkRow("Hide Duplicate Count", oc.isHideDuplicateCount(), v -> { oc.setHideDuplicateCount(v); saveOverlay(); }),
                checkRow("Input Preview", oc.isShowInputPreview(), v -> { oc.setShowInputPreview(v); saveOverlay(); rebuild(); }),
                inputPreviewOption(oc)
        ));
        body.add(Box.createVerticalStrut(8));

        messages.add(buildHighlights(oc));
        messages.add(buildReplacements(oc));
        JTabbedPane tabs = new JTabbedPane() {
            @Override public Dimension getPreferredSize() {
                Component active = getSelectedComponent();
                int rows = getUI() == null ? 1 : Math.max(1, getUI().getTabRunCount(this));
                int headerHeight = Math.max(35, rows * (getFontMetrics(getFont()).getHeight() + 10) + 6);
                return new Dimension(0, active == null ? headerHeight : active.getPreferredSize().height + headerHeight);
            }
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        tabs.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected Insets getTabInsets(int placement, int index) {
                return new Insets(4, 3, 4, 3);
            }
        });
        alignStackChildren(messages); alignStackChildren(looks); alignStackChildren(alerts);
        tabs.addTab("Messages", messages);
        tabs.addTab("Appearance", looks);
        tabs.addTab("Notifications", alerts);
        tabs.setSelectedIndex(selectedTabs.getOrDefault(oc.getId(), 0));
        tabs.addChangeListener(e -> {
            selectedTabs.put(oc.getId(), tabs.getSelectedIndex());
            body.revalidate();
            body.repaint();
        });
        body.add(tabs);
        JButton duplicate = button("DUPLICATE OVERLAY");
        duplicate.setAlignmentX(Component.LEFT_ALIGNMENT);
        duplicate.addActionListener(e -> {
            editingOverlayId = plugin.duplicateOverlay(oc).getId();
            rebuild();
        });
        body.add(duplicate);
        body.add(Box.createVerticalStrut(8));
        JButton delete = button("DELETE OVERLAY");
        delete.setForeground(DANGER);
        delete.setAlignmentX(Component.LEFT_ALIGNMENT);
        delete.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, help("Delete “" + oc.getName() + "”? This removes its saved settings."),
                    "Delete overlay", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) return;
            plugin.removeOverlay(oc);
            editingOverlayId = null;
            rebuild();
        });
        body.add(delete);
        body.add(Box.createVerticalGlue());
        return body;
    }


    private JPanel notificationControls(OverlayConfig oc) {
        JPanel controls = panel(CARD);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        Notification notification = oc.getNotification();
        controls.add(checkRow("Enable Notifications", notification.isEnabled(), value -> {
            oc.setNotification(oc.getNotification().withEnabled(value)); saveOverlay(); rebuild();
        }));
        if (!notification.isEnabled()) return controls;
        controls.add(comboRow("Notify For", AttentionTrigger.values(), oc.getNotificationTrigger(), value -> {
            oc.setNotificationTrigger(value); saveOverlay();
        }));
        controls.add(checkRow("Customize Notification", notification.isOverride(), value -> {
            oc.setNotification(value ? plugin.customizeNotification(oc.getNotification()) : oc.getNotification().withOverride(false));
            saveOverlay(); rebuild();
        }));
        if (!notification.isOverride()) {
            controls.add(help("Uses RuneLite's Notification Settings, including sound and focus preferences."));
            return controls;
        }
        controls.add(checkRow("Desktop Notification", notification.isTray(), value -> updateNotification(oc, n -> n.withTray(value))));
        controls.add(comboRow("Sound", NotificationSound.values(), notification.getSound(), value -> {
            updateNotification(oc, n -> n.withSound(value)); rebuild();
        }));
        if (notification.getSound() == NotificationSound.CUSTOM) {
            controls.add(textFieldRow("Sound Filename", notification.getSoundName() == null ? "" : notification.getSoundName(), value ->
                    updateNotification(oc, n -> n.withSoundName(value))));
            controls.add(help("Use a WAV filename from RuneLite's notifications folder. Blank uses RuneLite's default custom sound."));
        }
        controls.add(spinnerRow("Volume (%)", notification.getVolume(), 0, 100, 5, value -> updateNotification(oc, n -> n.withVolume(value))));
        controls.add(checkRow("Notify While Focused", notification.isSendWhenFocused(), value -> updateNotification(oc, n -> n.withSendWhenFocused(value))));
        controls.add(comboRow("Request Focus", RequestFocusType.values(), notification.getRequestFocus(), value -> updateNotification(oc, n -> n.withRequestFocus(value))));
        controls.add(comboRow("Window Flash", FlashNotification.values(), notification.getFlash(), value -> updateNotification(oc, n -> n.withFlash(value))));
        controls.add(colorRow("Flash Color", notification.getFlashColor(), value -> updateNotification(oc, n -> n.withFlashColor(value))));
        controls.add(spinnerRow("Timeout (ms)", notification.getTimeout(), 0, Integer.MAX_VALUE, 1000, value -> updateNotification(oc, n -> n.withTimeout(value))));
        controls.add(checkRow("Chatbox Notification", notification.isGameMessage(), value -> updateNotification(oc, n -> n.withGameMessage(value))));
        return controls;
    }

    private Component buildHighlights(OverlayConfig oc) {
        com.improvedchat.overlay.OverlayTextSettings rules = oc.getTextSettings();
        JPanel content = panel(CARD);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(help("Optional matching rules for this overlay only. Unmatched messages use its Appearance settings. Existing rule definitions and palette colors are preserved."));
        content.add(checkRow("Highlights override defaults", rules.isHighlightsOverrideDefaults(), v -> { rules.setHighlightsOverrideDefaults(v); saveOverlay(); }));
        content.add(help("Off preserves the former precedence: text overrides and animated text win over matching rules."));
        content.add(checkRow("Solid color rules", rules.enableMessageColorRules(), v -> { rules.setSolidEnabled(v); saveOverlay(); rebuild(); }));
        if (rules.enableMessageColorRules()) {
            content.add(textAreaRow("Color rules", rules.messageColorRules(), v -> { rules.setSolidRules(v); saveOverlay(); }));
            content.add(help("One rule per line: phrase::1 through phrase::9. Add ::flash for notification/flash triggers; regex: is supported."));
            content.add(checkRow("Include player chat", rules.includePlayerChatInColorRules(), v -> { rules.setIncludePlayer(v); saveOverlay(); }));
            JPanel palette = panel(CARD);
            palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));
            for (int i = 0; i < 9; i++) {
                final int index = i;
                palette.add(colorRow("Rule color " + (i + 1), rules.getPalette(i), v -> { rules.setPalette(index, v); saveOverlay(); }));
            }
            content.add(accordionPanel(oc, "highlightPalette", "RULE PALETTE", false, palette));
        }
        content.add(checkRow("RGB highlight rules", rules.enableOverlayRainbowRules(), v -> { rules.setRainbowEnabled(v); saveOverlay(); rebuild(); }));
        if (rules.enableOverlayRainbowRules()) {
            content.add(textAreaRow("RGB patterns", rules.overlayRainbowRules(), v -> { rules.setRainbowRules(v); saveOverlay(); }));
            content.add(help("One pattern per line; prefix regex: for a regular expression. Invalid expressions are ignored."));
            content.add(comboRow("Highlight effect", new String[] {"Use overlay default", "RGB Static", "RGB Cycling (Beta)", "RGB Wave (Beta)"},
                    rules.getHighlightStyle() == AnimatedTextStyle.NORMAL ? "Use overlay default" : rules.getHighlightStyle().toString(),
                    v -> { rules.setHighlightStyle("Use overlay default".equals(v) ? AnimatedTextStyle.NORMAL : "RGB Static".equals(v) ? AnimatedTextStyle.RGB_STATIC : "RGB Cycling (Beta)".equals(v) ? AnimatedTextStyle.RGB_CYCLE : AnimatedTextStyle.RGB_WAVE); saveOverlay(); rebuild(); }));
            content.add(comboRow("RGB spread", RainbowStyle.values(), rules.rainbowStyle(), v -> { rules.setSpread(v); saveOverlay(); }));
            content.add(help("RGB palette and speed are in Appearance. Static RGB retains the original word/character style."));
        }
        return accordionPanel(oc, "highlights", "MESSAGE HIGHLIGHTS", false, content);
    }

    private Component buildReplacements(OverlayConfig oc) {
        JPanel content = panel(CARD);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(checkRow("Enable message replacer (Beta)", oc.isMessageReplacerEnabled(), v -> { oc.setMessageReplacerEnabled(v); saveOverlay(); rebuild(); }));
        content.add(help("Beta: replace complete messages in this overlay only. Matching ignores case and chat colors. Chatbox text, filters, highlights and notification triggers still use the original message. Changes save automatically."));
        if (oc.isMessageReplacerEnabled()) {
            for (com.improvedchat.overlay.OverlayTextReplacement rule : oc.getTextReplacements()) {
                if (rule == null) continue;
                String id = rule.getId();
                JPanel fields = panel(CARD);
                fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
                fields.add(checkRow("Use this replacement", rule.isEnabled(), v -> changeReplacement(oc, id, r -> r.withEnabled(v))));
                fields.add(textFieldRow("Original message", rule.getOriginal(), v -> changeReplacement(oc, id, r -> r.withOriginal(v))));
                fields.add(textFieldRow("Show instead", rule.getReplacement(), v -> changeReplacement(oc, id, r -> r.withReplacement(v))));
                JButton remove = button("REMOVE REPLACEMENT");
                remove.addActionListener(e -> { changeReplacement(oc, id, r -> null); rebuild(); });
                fields.add(remove);
                content.add(accordionPanel(oc, "replacement:" + id, "REPLACEMENT", true, fields));
            }
            JButton add = button("+ ADD REPLACEMENT");
            add.addActionListener(e -> {
                java.util.List<com.improvedchat.overlay.OverlayTextReplacement> rules = new java.util.ArrayList<>(oc.getTextReplacements());
                rules.add(new com.improvedchat.overlay.OverlayTextReplacement());
                oc.setTextReplacements(rules); saveOverlay(); rebuild();
            });
            content.add(add);
            content.add(help("Fill both fields to activate a rule. The first matching enabled rule wins; replacements never trigger other replacements. Example: Your thrall returns to the grave. → You need to summon a thrall!"));
            JTextField sample = new JTextField(); styleField(sample);
            content.add(controlRow("Test message text", sample));
            JTextArea result = (JTextArea)help("Test a message here without logging in or adding it to chat.");
            JButton test = button("TEST REPLACEMENTS");
            test.addActionListener(e -> result.setText("Overlay text: " + MessageColorRuleEngine.plainText(
                    com.improvedchat.overlay.OverlayTextReplacements.displayText(oc, sample.getText()))));
            content.add(test); content.add(result);
        }
        return accordionPanel(oc, "replacements", "MESSAGE REPLACER (BETA)", false, content);
    }

    private void changeReplacement(OverlayConfig oc, String id,
            java.util.function.UnaryOperator<com.improvedchat.overlay.OverlayTextReplacement> change) {
        java.util.List<com.improvedchat.overlay.OverlayTextReplacement> rules = new java.util.ArrayList<>(oc.getTextReplacements());
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i) == null || !id.equals(rules.get(i).getId())) continue;
            com.improvedchat.overlay.OverlayTextReplacement next = change.apply(rules.get(i));
            if (next == null) rules.remove(i); else rules.set(i, next);
            oc.setTextReplacements(rules); saveOverlay(); return;
        }
    }

    private void updateNotification(OverlayConfig oc, java.util.function.UnaryOperator<Notification> change) {
        oc.setNotification(change.apply(oc.getNotification())); saveOverlay();
    }

    private Component help(String text) {
        JTextArea area = new JTextArea(text) {
            @Override public Dimension getPreferredSize() {
                int width = getParent() != null && getParent().getWidth() > 0
                        ? getParent().getWidth() - getParent().getInsets().left - getParent().getInsets().right
                        : Math.max(140, ImprovedChatPanel.this.getWidth() - 48);
                setSize(Math.max(100, width), Short.MAX_VALUE);
                Dimension preferred = super.getPreferredSize();
                return new Dimension(width, preferred.height);
            }
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
            @Override public Dimension getMinimumSize() {
                return new Dimension(0, getPreferredSize().height);
            }
        };
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setForeground(MUTED);
        area.setFont(UIManager.getFont("Label.font"));
        area.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    private Component messageSelectionHelp(OverlayConfig oc) {
        return help(oc.getMessageTypes().isEmpty()
                ? "No message types selected. Choose at least one to see live messages."
                : "Select the channels and message types this overlay should display.");
    }

    private Component inputPreviewOption(OverlayConfig oc) {
        Component row = checkRow("Preview only while typing", oc.isPreviewOnlyWhenTyping(),
                v -> { oc.setPreviewOnlyWhenTyping(v); saveOverlay(); });
        enableTree(row, oc.isShowInputPreview());
        return row;
    }

    private void enableTree(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) enableTree(child, enabled);
        }
    }

    private JPanel buildFiltering(OverlayConfig oc) {
        JPanel filtering = panel(CARD);
        filtering.setLayout(new BoxLayout(filtering, BoxLayout.Y_AXIS));
        boolean overridden = plugin.isGlobalFilterOverride();
        Component globalToggle = checkRow("Use RuneLite Chat Filter", oc.isUseGlobalChatFilter(), v -> {
            oc.setUseGlobalChatFilter(v); saveOverlay(); rebuild();
        });
        enableTree(globalToggle, !overridden);
        filtering.add(globalToggle);
        boolean usesGlobal = overridden || oc.isUseGlobalChatFilter();
        if (usesGlobal) {
            filtering.add(help(overridden
                    ? "Global override is on. Change ‘Use Chat Filter Globally’ in Improved Chat settings to use these custom rules."
                    : "This overlay uses RuneLite's Chat Filter rules."));
            filtering.add(help(plugin.isGlobalFilterAvailable()
                    ? "RuneLite Chat Filter is enabled. Custom rules below are saved but inactive."
                    : "RuneLite Chat Filter is disabled: no content filtering is applied. Enable it in RuneLite or turn off global filtering."));
        }
        JPanel custom = panel(CARD);
        custom.setLayout(new BoxLayout(custom, BoxLayout.Y_AXIS));
        custom.add(comboRow("Custom filter mode", OverlayFilterMode.values(), oc.getFilterMode(), v -> {
            oc.setFilterMode(v); saveOverlay(); rebuild();
        }));
        JPanel feedback = panel(CARD);
        feedback.setLayout(new BoxLayout(feedback, BoxLayout.Y_AXIS));
        Runnable validate = () -> {
            feedback.removeAll();
            if (oc.getFilterMode() != OverlayFilterMode.OFF) {
                for (String error : new OverlayMessageFilter().validationMessages(oc)) feedback.add(help(error));
            }
            feedback.revalidate(); feedback.repaint();
        };
        if (oc.getFilterMode() != OverlayFilterMode.OFF) {
            custom.add(textFieldRow("Words or phrases", oc.getFilteredWords(), v -> {
                oc.setFilteredWords(v); saveOverlay(); validate.run();
            }));
            custom.add(help("Separate words or phrases with commas. Matching ignores case."));
            custom.add(textAreaRow("Regular expressions", oc.getFilteredRegex(), v -> {
                oc.setFilteredRegex(v); saveOverlay(); validate.run();
            }));
            custom.add(help("One expression per line, e.g. ^You have .* coins$. Invalid lines are ignored."));
            custom.add(textAreaRow("Player names", oc.getFilteredNames(), v -> {
                oc.setFilteredNames(v); saveOverlay(); validate.run();
            }));
            custom.add(help("One rule per line. Names match part of a name; for an exact match use regex:^Alice$."));
            custom.add(feedback);
            JTextField sampleName = new JTextField();
            JTextField sampleBody = new JTextField();
            styleField(sampleName); styleField(sampleBody);
            custom.add(controlRow("Test player name", sampleName));
            custom.add(controlRow("Test message", sampleBody));
            JButton test = button("TEST CUSTOM RULES");
            JTextArea result = (JTextArea) help("Tests content rules only; message types and visibility still apply.");
            test.addActionListener(e -> {
                boolean hidden = new OverlayMessageFilter().shouldExclude(oc,
                        OverlayMessage.senderMessage(sampleName.getText(), null, sampleBody.getText(), 0, ChatMessageType.PUBLICCHAT, false));
                result.setText(hidden ? "Hidden by these custom rules." : "Allowed by these custom rules.");
                validate.run();
            });
            custom.add(test); custom.add(result);
        }
        validate.run();
        enableTree(custom, !usesGlobal);
        filtering.add(custom);
        return filtering;
    }

    private Component accordion(OverlayConfig oc, String key, String title, boolean defaultOpen, Component... rows) {
        JPanel content = panel(CARD);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        for (Component row : rows) content.add(row);
        return accordionPanel(oc, key, title, defaultOpen, content);
    }

    private Component accordionPanel(OverlayConfig oc, String key, String title, boolean defaultOpen, JPanel content) {
        String stateKey = oc.getId() + ":" + key;
        boolean open = openSections.containsKey(stateKey) ? openSections.get(stateKey) : defaultOpen;

        JPanel root = panel(CARD);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createLineBorder(BORDER));
        root.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JButton header = button((open ? "▼  " : "▶  ") + title);
        header.setHorizontalAlignment(JButton.LEFT);
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        header.setForeground(ACCENT);
        header.setBackground(CARD_ALT);
        header.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        root.add(header);

        alignStackChildren(content);
        content.setBorder(BorderFactory.createEmptyBorder(3, 7, 6, 7));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        content.setVisible(open);
        root.add(content);

        header.addActionListener(e -> {
            boolean next = !content.isVisible();
            content.setVisible(next);
            openSections.put(stateKey, next);
            header.setText((next ? "▼  " : "▶  ") + title);
            // Invalidate the active tab's BoxLayout cache before measuring its new height.
            for (java.awt.Container ancestor = root; ancestor != null; ancestor = ancestor.getParent()) {
                ancestor.invalidate();
                if (ancestor == ImprovedChatPanel.this) break;
            }
            root.revalidate();
            root.repaint();
        });
        return root;
    }

    private static void onTextChange(javax.swing.text.Document document, Runnable save) {
        document.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { save.run(); }
            public void removeUpdate(DocumentEvent event) { save.run(); }
            public void changedUpdate(DocumentEvent event) { save.run(); }
        });
    }

    private boolean hasClanMessages(OverlayConfig oc) {
        return oc.getMessageTypes().contains(ChatMessageType.CLAN_CHAT)
                || oc.getMessageTypes().contains(ChatMessageType.CLAN_GUEST_CHAT)
                || oc.getMessageTypes().contains(ChatMessageType.CLAN_GIM_CHAT);
    }

    private Component textFieldRow(String name, String value, java.util.function.Consumer<String> setter) {
        JTextField field = new JTextField(value);
        styleField(field);
        onTextChange(field.getDocument(), () -> setter.accept(field.getText()));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { setter.accept(field.getText()); }
        });
        return controlRow(name, field);
    }

    private Component textAreaRow(String name, String value, java.util.function.Consumer<String> setter) {
        JTextArea area = new JTextArea(value == null ? "" : value, 3, 12);
        area.setName(name);
        area.setLineWrap(false);
        area.setBackground(CONTROL);
        area.setForeground(TEXT);
        area.setCaretColor(TEXT);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        onTextChange(area.getDocument(), () -> setter.accept(area.getText()));
        area.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { setter.accept(area.getText()); }
        });
        JScrollPane scroller = new JScrollPane(area);
        scroller.setPreferredSize(new Dimension(180, 86));
        scroller.setBorder(BorderFactory.createLineBorder(BORDER));
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel row = panel(CARD);
        row.setLayout(new BorderLayout(0, 4));
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = label(name, TEXT, 12, Font.PLAIN);
        title.setLabelFor(area);
        row.add(title, BorderLayout.NORTH);
        row.add(scroller, BorderLayout.CENTER);
        return row;
    }

    private Component checkRow(String name, boolean selected, java.util.function.Consumer<Boolean> setter) {
        JCheckBox box = new JCheckBox(name, selected);
        box.setOpaque(false);
        box.setForeground(TEXT);
        box.setFont(UIManager.getFont("Label.font"));
        box.setFocusPainted(true);
        box.setName(name);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.addActionListener(e -> setter.accept(box.isSelected()));
        JPanel row = panel(CARD);
        row.setLayout(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(box, BorderLayout.WEST);
        return row;
    }


    private Component spinnerRow(String name, int value, int min, int max, int step, java.util.function.Consumer<Integer> setter) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setPreferredSize(new Dimension(94, 24));
        spinner.setMaximumSize(new Dimension(94, 24));
        spinner.addChangeListener(e -> setter.accept(((Number) spinner.getValue()).intValue()));
        return controlRow(name, spinner);
    }

    private <T> Component comboRow(String name, T[] values, T selected, java.util.function.Consumer<T> setter) {
        JComboBox<T> combo = new JComboBox<>(values);
        combo.setSelectedItem(selected);
        combo.setPreferredSize(new Dimension(112, 24));
        combo.setMaximumSize(new Dimension(112, 24));
        combo.addActionListener(e -> setter.accept((T) combo.getSelectedItem()));
        return controlRow(name, combo);
    }


    private Component colorRow(String name, Color color, java.util.function.Consumer<Color> setter) {
        JButton swatch = colorButton(color, "");
        swatch.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, name, swatch.getBackground());
            if (chosen != null) {
                setter.accept(chosen);
                swatch.setBackground(chosen);
            }
        });
        return controlRow(name, swatch);
    }

    private Component controlRow(String name, Component control) {
        JPanel row = panel(CARD);
        row.setLayout(new BorderLayout(0, 3));
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 57));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = label(name, TEXT, 12, Font.PLAIN);
        title.setLabelFor(control);
        control.setName(name);
        if (control instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) control).getAccessibleContext().setAccessibleName(name);
        }
        row.add(title, BorderLayout.NORTH);
        row.add(control, BorderLayout.CENTER);
        return row;
    }

    private Component messageTypesRow(OverlayConfig oc) {
        JButton b = button(oc.getMessageTypes().size() + " selected");
        b.setPreferredSize(new Dimension(102, 24));
        b.setMaximumSize(new Dimension(102, 24));
        b.addActionListener(e -> showMessageTypeMenu(oc, b));
        return controlRow("Message Types", b);
    }

    private void showMessageTypeMenu(OverlayConfig oc, JButton anchor) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem all = new JMenuItem("Select All");
        all.addActionListener(e -> {
            EnumSet<ChatMessageType> set = EnumSet.noneOf(ChatMessageType.class);
            for (MessageCategory cat : MessageCategory.values()) set.addAll(cat.getTypes());
            oc.setMessageTypes(set);
            saveOverlay();
            rebuild();
        });
        JMenuItem clear = new JMenuItem("Clear All");
        clear.addActionListener(e -> {
            oc.setMessageTypes(EnumSet.noneOf(ChatMessageType.class));
            saveOverlay();
            rebuild();
        });
        menu.add(all);
        menu.add(clear);
        menu.addSeparator();

        for (MessageCategory cat : MessageCategory.values()) {
            Set<ChatMessageType> selectedNow = oc.getMessageTypes();
            boolean allSelected = !cat.getTypes().isEmpty() && selectedNow.containsAll(cat.getTypes());
            boolean anySelected = false;
            for (ChatMessageType type : cat.getTypes()) {
                if (selectedNow.contains(type)) { anySelected = true; break; }
            }

            String prefix = allSelected ? "✓ " : (anySelected ? "• " : "");
            JMenu sub = new JMenu(prefix + cat.toString());
            JCheckBoxMenuItem wholeCategory = new JCheckBoxMenuItem("Entire " + cat.toString(), allSelected);
            wholeCategory.addActionListener(e -> {
                Set<ChatMessageType> current = oc.getMessageTypes();
                EnumSet<ChatMessageType> copy = current.isEmpty()
                        ? EnumSet.noneOf(ChatMessageType.class) : EnumSet.copyOf(current);
                if (wholeCategory.isSelected()) copy.addAll(cat.getTypes());
                else copy.removeAll(cat.getTypes());
                oc.setMessageTypes(copy);
                saveOverlay();
                rebuild();
            });
            sub.add(wholeCategory);
            sub.addSeparator();

            for (ChatMessageType type : cat.getTypes()) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(pretty(type.name()), oc.getMessageTypes().contains(type));
                item.addActionListener(e -> {
                    Set<ChatMessageType> current = oc.getMessageTypes();
                    EnumSet<ChatMessageType> copy = current.isEmpty()
                            ? EnumSet.noneOf(ChatMessageType.class) : EnumSet.copyOf(current);
                    if (item.isSelected()) copy.add(type); else copy.remove(type);
                    oc.setMessageTypes(copy);
                    saveOverlay();
                    rebuild();
                });
                sub.add(item);
            }
            menu.add(sub);
        }
        menu.show(anchor, 0, anchor.getHeight());
    }

    private void showCreateOverlayMenu(JButton anchor) {
        JPopupMenu menu = new JPopupMenu();
        addTemplate(menu, "Game Alerts", "game");
        addTemplate(menu, "Private Messages", "private");
        addTemplate(menu, "Clan Chat", "clan");
        addTemplate(menu, "All Messages", "all");
        menu.addSeparator();
        addTemplate(menu, "Custom Overlay", "custom");
        menu.show(anchor, 0, anchor.getHeight());
    }

    private void addTemplate(JPopupMenu menu, String name, String template) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(e -> createOverlay(name, template));
        menu.add(item);
    }

    private void createOverlay(String name, String template) {
        OverlayConfig oc = OverlayConfig.preset(name, template);
        oc.getTextSettings(); // New overlays have independent styling defaults.
        plugin.addConfiguredOverlay(oc);
        editingOverlayId = oc.getId();
        rebuild();
    }


    private JButton colorButton(Color color, String text) {
        Color c = color == null ? Color.WHITE : color;
        JButton b = new JButton(text);
        b.setBackground(c);
        b.setForeground(contrast(c));
        b.setOpaque(true);
        b.setFocusPainted(true);
        b.setMargin(new Insets(2, 5, 2, 5));
        b.setBorder(BorderFactory.createLineBorder(BORDER));
        b.setPreferredSize(new Dimension(54, 23));
        b.setMaximumSize(new Dimension(54, 23));
        return b;
    }


    private JLabel pageTitle(String title) {
        JLabel l = label(title, TEXT, 11, Font.BOLD);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel compactMeta(String text) {
        JLabel l = label(text, MUTED, 10, Font.PLAIN);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setToolTipText(text);
        return l;
    }

    private WidthTrackingPanel verticalPage() {
        WidthTrackingPanel p = new WidthTrackingPanel();
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(7, 7, 10, 7));
        return p;
    }

    private JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        pageScroll = sp;
        sp.setBorder(null);
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    private JPanel panel(Color color) {
        JPanel p = new JPanel();
        p.setBackground(color);
        p.setOpaque(true);
        return p;
    }

    private JLabel label(String text, Color color, int size, int style) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        Font base = UIManager.getFont("Label.font");
        l.setFont(base == null ? new Font(Font.SANS_SERIF, style, Math.max(12, size)) : base.deriveFont(style, Math.max(base.getSize2D(), size)));
        return l;
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(true);
        b.setMargin(new Insets(3, 6, 3, 6));
        b.setBackground(CARD_ALT);
        b.setForeground(TEXT);
        b.setFont(UIManager.getFont("Label.font"));
        return b;
    }

    private void styleField(JTextField field) {
        field.setBackground(CONTROL);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        field.setPreferredSize(new Dimension(102, 24));
        field.setMaximumSize(new Dimension(102, 24));
    }

    private void alignStackChildren(JPanel p) {
        for (Component c : p.getComponents()) {
            if (c instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }
    }

    private void saveOverlay() {
        plugin.onOverlayConfigChanged();
        for (OverlayConfig oc : plugin.getOverlayConfigs()) {
            JButton selector = overlaySelectors.get(oc.getId());
            if (selector != null) {
                selector.setText(oc.getName() + "  ›");
                selector.setToolTipText("Edit " + oc.getName());
            }
        }
    }

    private OverlayConfig findOverlay(String id) {
        for (OverlayConfig oc : plugin.getOverlayConfigs()) {
            if (oc.getId().equals(id)) return oc;
        }
        return null;
    }

    private String messageSummary(OverlayConfig oc) {
        int count = oc.getMessageTypes().size();
        if (count == 0) return "No types";
        if (count == 1) return "1 type";
        return count + " types";
    }

    private String fadeLabel(OverlayConfig oc) {
        return oc.getFadeOutDuration() == 0 ? "No fade" : oc.getFadeOutDuration() + "s fade";
    }


    private static Color contrast(Color c) {
        int lum = c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114;
        return lum > 150000 ? Color.BLACK : Color.WHITE;
    }

    private static String pretty(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).replace('_', ' ');
        StringBuilder b = new StringBuilder();
        boolean upper = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (upper && Character.isLetter(c)) {
                b.append(Character.toUpperCase(c));
                upper = false;
            } else {
                b.append(c);
            }
            if (c == ' ') upper = true;
        }
        return b.toString();
    }


    private static final class WidthTrackingPanel extends JPanel implements Scrollable {
        @Override public Component add(Component comp) {
            if (comp instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) comp).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            return super.add(comp);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return Math.max(32, visibleRect.height - 32); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
