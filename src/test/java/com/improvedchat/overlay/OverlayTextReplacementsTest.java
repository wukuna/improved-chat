package com.improvedchat.overlay;

import com.google.gson.Gson;
import com.improvedchat.MessageColorRuleEngine;
import com.improvedchat.model.FontSize;
import com.improvedchat.model.OverlayMessage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayTextReplacementsTest {
    private static final String ORIGINAL = "Your thrall returns to the grave.";
    private static final String REPLACEMENT = "You need to summon a thrall!";
    private OverlayConfig configured() {
        OverlayConfig c = new OverlayConfig(); c.setMessageReplacerEnabled(true);
        c.setTextReplacements(Arrays.asList(new OverlayTextReplacement(ORIGINAL,REPLACEMENT,true),
                new OverlayTextReplacement("Your boost expired.","Drink another potion!",true)));
        return c;
    }
    @Test public void multipleMessagesMatchIndependentlyIgnoringChatColorsAndCase() {
        OverlayConfig c = configured();
        assertEquals(REPLACEMENT, OverlayTextReplacements.displayText(c,"<col=ff0000>YOUR THRALL RETURNS TO THE GRAVE.</col>"));
        assertEquals("Drink another potion!",OverlayTextReplacements.displayText(c,"Your boost expired."));
        assertEquals("unmatched",OverlayTextReplacements.displayText(c,"unmatched"));
        assertEquals(ORIGINAL + " Again.",OverlayTextReplacements.displayText(c,ORIGINAL + " Again."));
    }
    @Test public void disabledIncompleteAndNullRulesLeaveMessagesUntouched() {
        OverlayConfig c = configured(); c.setMessageReplacerEnabled(false);
        assertEquals(ORIGINAL,OverlayTextReplacements.displayText(c,ORIGINAL));
        c.setMessageReplacerEnabled(true);
        c.setTextReplacements(Arrays.asList(null,new OverlayTextReplacement(ORIGINAL,REPLACEMENT,false),new OverlayTextReplacement(ORIGINAL,"",true)));
        assertEquals(ORIGINAL,OverlayTextReplacements.displayText(c,ORIGINAL));
        assertNull(OverlayTextReplacements.displayText(c,null));
    }
    @Test public void firstMatchWinsAndReplacementsNeverChain() {
        OverlayConfig c = configured();
        c.setTextReplacements(Arrays.asList(new OverlayTextReplacement(ORIGINAL,REPLACEMENT,true),
                new OverlayTextReplacement(ORIGINAL,"duplicate",true),new OverlayTextReplacement(REPLACEMENT,"recursive",true)));
        assertEquals(REPLACEMENT,OverlayTextReplacements.displayText(c,ORIGINAL));
    }
    @Test public void outputIsLiteralTextAndSettingsRoundTripWithoutSharedCopies() {
        OverlayConfig c = configured(); Gson gson = new Gson();
        c.normalize();
        c.setTextReplacements(Collections.singletonList(new OverlayTextReplacement(ORIGINAL,"Use <ruby> & <img=2>",true)));
        String value=OverlayTextReplacements.displayText(c,ORIGINAL);
        assertEquals("Use <ruby> & <img=2>",MessageColorRuleEngine.plainText(value));
        assertFalse(value.contains("<img=2>"));
        OverlayConfig copy=OverlayConfigStore.read(gson,"["+gson.toJson(c)+"]").get(0);
        assertEquals(gson.toJson(c),gson.toJson(copy));
        copy.setTextReplacements(Collections.emptyList());
        assertEquals(1,c.getTextReplacements().size());
    }
    @Test public void oldSavedSettingsDefaultToOff() {
        OverlayConfig old = OverlayConfigStore.read(new Gson(),"[{\"id\":\"old\",\"name\":\"Old overlay\"}]").get(0);
        assertFalse(old.isMessageReplacerEnabled()); assertTrue(old.getTextReplacements().isEmpty());
        assertEquals(ORIGINAL,OverlayTextReplacements.displayText(old,ORIGINAL));
    }
    @Test public void renderedReplacementKeepsOriginalHistoryHighlightFilterAndDuplicateCount() {
        OverlayConfig c=configured(); c.getTextSettings().setSolidEnabled(true);
        c.getTextSettings().setSolidRules("thrall returns::1::flash"); c.getTextSettings().setPalette(0,Color.BLUE);
        c.setFilterMode(OverlayFilterMode.SHOW_ONLY_MATCHES); c.setFilteredWords("thrall returns");
        OverlayMessage m=OverlayMessage.gameMessage(ORIGINAL,0,ChatMessageType.GAMEMESSAGE,false);
        m.incrementCount();
        assertFalse(new OverlayMessageFilter().shouldExclude(c,m));
        assertTrue(MessageColorRuleEngine.flashFor(m.getMessage(),m.getType(),c.getTextSettings()));
        Graphics2D g=new BufferedImage(500,150,BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            for(boolean wrap:new boolean[]{true,false}) {
                List<RenderLine> lines=ChatRenderUtils.buildMessageLines(m,g.getFontMetrics(),90,0,0,wrap,Color.WHITE,true,false,
                        FontSize.REGULAR,null,false,"",false,null,Collections.emptyList(),null,c);
                String visible=lines.stream().map(line -> line.segments.stream().map(segment->segment.text).collect(Collectors.joining())).collect(Collectors.joining(wrap ? " " : ""));
                assertEquals(REPLACEMENT + " (2)",visible);
                assertTrue(lines.stream().flatMap(line->line.segments.stream()).allMatch(segment->Color.BLUE.equals(segment.color)));
                assertEquals(ORIGINAL,m.getMessage());
            }
        } finally {g.dispose();}
        assertEquals(ORIGINAL,OverlayTextReplacements.displayText(new OverlayConfig(),m.getMessage()));
    }
}
