package com.improvedchat.overlay;

import com.google.gson.Gson;
import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.MessageColorRuleEngine;
import com.improvedchat.RainbowStyle;
import com.improvedchat.model.AnimatedTextStyle;
import com.improvedchat.model.FontSize;
import com.improvedchat.model.OverlayMessage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayMenuMigrationTest {
    private final Gson gson = new Gson();
    private ImprovedChatConfig legacy() {
        return new ImprovedChatConfig() {
            public boolean enableMessageColorRules() { return true; }
            public String messageColorRules() { return "expire::9::flash\nregex:^Loot::3"; }
            public Color messageRuleColor9() { return Color.PINK; }
            public boolean includePlayerChatInColorRules() { return true; }
            public boolean enableOverlayRainbowRules() { return true; }
            public String overlayRainbowRules() { return "rainbow\nregex:^Congratulations"; }
            public RainbowStyle rainbowStyle() { return RainbowStyle.PER_LETTER; }
            public Color privateColour() { return Color.ORANGE; }
        };
    }

    @Test public void oldJsonMigratesOnceWithoutChangingIdentityGeometryOrAnimation() {
        OverlayConfig old = OverlayConfigStore.read(gson,
                "[{\"id\":\"saved-id\",\"name\":\"Saved\",\"widgetWidth\":730,\"offsetX\":42,\"show\":false,\"animatedTextStyle\":\"RGB_CYCLE\",\"rgbCycleSeconds\":17}]").get(0);
        assertFalse(old.hasTextSettings());
        assertTrue(old.migrateTextSettings(legacy()));
        assertEquals("saved-id", old.getId());
        assertEquals(730, old.getWidgetWidth());
        assertEquals(42, old.getOffsetX());
        assertFalse(old.isShow());
        assertEquals(AnimatedTextStyle.RGB_CYCLE, old.getAnimatedTextStyle());
        assertFalse(old.isCustomRgbCycle());
        assertEquals(17, old.getRgbCycleSeconds());
        OverlayTextSettings settings = old.getTextSettings();
        assertEquals(legacy().messageColorRules(), settings.messageColorRules());
        assertEquals(Color.PINK, settings.getPalette(8));
        assertEquals(RainbowStyle.PER_LETTER, settings.rainbowStyle());
        assertEquals(Color.ORANGE, settings.privateColour());
        assertFalse(settings.isHighlightsOverrideDefaults());
        settings.setSolidRules("my edit::2");
        assertFalse(old.migrateTextSettings(new ImprovedChatConfig() { }));
        assertEquals("my edit::2", settings.messageColorRules());
        OverlayConfig roundTrip = OverlayConfigStore.read(gson, "[" + gson.toJson(old) + "]").get(0);
        assertFalse(roundTrip.migrateTextSettings(legacy()));
        assertEquals(gson.toJson(old), gson.toJson(roundTrip));
    }

    @Test public void migrationAndDuplicateCopiesKeepRulePalettesIndependent() {
        OverlayConfig a = new OverlayConfig(), b = new OverlayConfig();
        a.migrateTextSettings(legacy()); b.migrateTextSettings(legacy());
        a.getTextSettings().setPalette(8, Color.BLUE);
        assertEquals(Color.PINK, b.getTextSettings().getPalette(8));
        OverlayConfig copy = gson.fromJson(gson.toJson(a), OverlayConfig.class);
        copy.renewId(); copy.getTextSettings().setPalette(8, Color.GREEN);
        assertNotEquals(a.getId(), copy.getId());
        assertEquals(Color.BLUE, a.getTextSettings().getPalette(8));
        assertEquals(Color.GREEN, copy.getTextSettings().getPalette(8));
        assertEquals(Color.BLUE, MessageColorRuleEngine.colorFor("will expire", ChatMessageType.GAMEMESSAGE, a.getTextSettings()));
    }

    @Test public void emptySavedListsStayEmptyAndDisabledDefinitionsArePreserved() {
        assertTrue(OverlayConfigStore.read(gson, "[]").isEmpty());
        ImprovedChatConfig disabled = new ImprovedChatConfig() {
            public String messageColorRules() { return "saved but disabled::7::flash"; }
            public String overlayRainbowRules() { return "saved rgb rule"; }
        };
        OverlayConfig c = new OverlayConfig(); c.migrateTextSettings(disabled);
        assertFalse(c.getTextSettings().enableMessageColorRules());
        assertEquals(disabled.messageColorRules(), c.getTextSettings().messageColorRules());
        assertFalse(c.getTextSettings().enableOverlayRainbowRules());
        assertEquals(disabled.overlayRainbowRules(), c.getTextSettings().overlayRainbowRules());
    }

    private List<RenderLine> lines(OverlayConfig c, String text, boolean wrap) {
        Graphics2D graphics = new BufferedImage(500, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        graphics.setFont(new Font("Dialog", Font.PLAIN, 12));
        try {
            return ChatRenderUtils.buildMessageLines(OverlayMessage.gameMessage(text, 0, ChatMessageType.GAMEMESSAGE, false),
                    graphics.getFontMetrics(), 90, 0, 0, wrap, Color.WHITE, c.isContextualColours(), false,
                    FontSize.REGULAR, null, false, "", false, null, Collections.emptyList(), null, c);
        } finally { graphics.dispose(); }
    }

    @Test public void matchedMessageOverridesAnimationWithoutStylingOtherMessages() {
        for (boolean wrap : new boolean[]{false, true}) {
            OverlayConfig c = new OverlayConfig(); c.migrateTextSettings(legacy());
            c.setAnimatedTextStyle(AnimatedTextStyle.RGB_WAVE);
            c.getTextSettings().setHighlightsOverrideDefaults(true);
            for (RenderLine line : lines(c, "expire expire expire", wrap)) for (TextSegment segment : line.segments) {
                if (segment.iconId < 0) {
                    assertEquals(AnimatedTextStyle.NORMAL, segment.textStyle);
                    assertEquals(Color.PINK, segment.color);
                }
            }
            for (RenderLine line : lines(c, "unmatched", wrap)) for (TextSegment segment : line.segments) assertNull(segment.textStyle);
            c.getTextSettings().setHighlightStyle(AnimatedTextStyle.RGB_CYCLE);
            for (RenderLine line : lines(c, "rainbow", wrap)) for (TextSegment segment : line.segments) assertEquals(AnimatedTextStyle.RGB_CYCLE, segment.textStyle);
            c.getTextSettings().setHighlightsOverrideDefaults(false);
            for (RenderLine line : lines(c, "expire", wrap)) for (TextSegment segment : line.segments) assertNull(segment.textStyle);
        }
    }

    @Test public void migratedRainbowKeepsCharacterSpreadAndIcons() {
        OverlayConfig c = new OverlayConfig(); c.migrateTextSettings(legacy());
        List<TextSegment> segments = lines(c, "rainbow <img=2>", true).get(0).segments;
        assertEquals(1, segments.get(0).text.length());
        assertTrue(segments.stream().anyMatch(s -> s.iconId == 2));
        assertEquals(AnimatedTextStyle.RGB_STATIC, segments.get(0).textStyle);
    }

    @Test public void customCycleHitsBothEndpointsRestoresAlphaAndKeepsFullSpectrumDefault() {
        OverlayConfig c = new OverlayConfig();
        c.setRgbCycleSeconds(8); c.setCustomRgbCycle(true);
        c.setRgbColor1(Color.RED); c.setRgbColor2(Color.BLUE);
        assertEquals(new Color(255,0,0,91), RgbAnimation.cycleColor(c, 0, 91));
        assertEquals(new Color(0,0,255,91), RgbAnimation.cycleColor(c, 4000, 91));
        assertEquals(new Color(255,0,0,91), RgbAnimation.cycleColor(c, 8000, 91));
        c.setCustomRgbCycle(false);
        assertNotEquals(RgbAnimation.cycleColor(c, 0, 255), RgbAnimation.cycleColor(c, 2000, 255));
    }
}
