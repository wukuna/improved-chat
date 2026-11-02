package com.improvedchat.overlay;

import com.improvedchat.model.OverlayMessage;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayScrollbackTest {
    private OverlayMessage message(String text) {
        return OverlayMessage.gameMessage(text, 1, ChatMessageType.GAMEMESSAGE, false);
    }
    private List<RenderLine> layout(OverlayMessage m) {
        return Arrays.stream(m.getMessage().split("\\|"))
                .map(s -> new RenderLine(Collections.singletonList(new TextSegment(s, -1, 10, Color.WHITE)),255))
                .collect(Collectors.toList());
    }
    private String page(OverlayScrollback s, List<OverlayMessage> messages, int rows) {
        return s.select(messages,rows,this::layout).stream().map(l -> l.segments.get(0).text).collect(Collectors.joining("|"));
    }

    @Test public void scrollsWrappedLinesAndAnchorsWhileNewMessagesArrive() {
        OverlayScrollback s = new OverlayScrollback();
        List<OverlayMessage> messages = new ArrayList<>(Arrays.asList(message("a|b|c|d"), message("e|f|g")));
        assertEquals("e|f|g",page(s,messages,3));
        s.scroll(2); assertEquals("c|d|e",page(s,messages,3));
        messages.add(message("h|i"));
        assertEquals("c|d|e",page(s,messages,3));
        assertTrue(s.isBrowsing()); assertEquals(1,s.getNewerMessages());
        s.scroll(-300); assertEquals("g|h|i",page(s,messages,3)); assertFalse(s.isBrowsing());
        messages.add(message("j")); assertEquals("h|i|j",page(s,messages,3));
    }

    @Test public void oldestPageEvictionClearAndResizeStayInRange() {
        OverlayScrollback s = new OverlayScrollback();
        List<OverlayMessage> messages = new ArrayList<>(Arrays.asList(message("a|b"),message("c|d"),message("e|f")));
        s.scroll(300); assertEquals("a|b|c",page(s,messages,3));
        s.scroll(300); assertEquals("a|b|c",page(s,messages,3));
        messages.remove(0); assertEquals("c|d|e",page(s,messages,3));
        assertEquals("c|d|e|f",page(s,messages,20)); assertFalse(s.isBrowsing());
        s.scroll(2); page(s,messages,2); s.latest(); assertEquals("e|f",page(s,messages,2));
        s.scroll(2); page(s,messages,2); assertEquals("",page(s,Collections.emptyList(),2)); assertFalse(s.isBrowsing());
        messages=Collections.singletonList(message("new")); assertEquals("new",page(s,messages,2));
    }

    @Test public void collapsedDuplicatesKeepStableIdentityAndNewestNotificationSequence() {
        OverlayMessage a=message("one|two|three|four"), b=message("one|two|three|four");
        List<OverlayMessage> raw=new ArrayList<>(Arrays.asList(a,b));
        OverlayScrollback s=new OverlayScrollback(); s.scroll(1);
        List<OverlayMessage> grouped=OverlayHistory.select(raw,0,100,true);
        assertEquals(b.getSequence(),grouped.get(0).getSequence());
        assertEquals(a.getSequence(),grouped.get(0).getFirstSequence());
        assertEquals("two|three",page(s,grouped,2));
        raw.add(message("one|two|three|four"));
        assertEquals("two|three",page(s,OverlayHistory.select(raw,0,100,true),2));
    }

    @Test public void largeHistoryOnlyFormatsNeededMessages() {
        List<OverlayMessage> messages=new ArrayList<>();
        for(int i=0;i<5000;i++)messages.add(message("message "+i));
        AtomicInteger layouts=new AtomicInteger(); OverlayScrollback s=new OverlayScrollback();
        assertEquals(10,s.select(messages,10,m->{layouts.incrementAndGet();return layout(m);}).size());
        assertEquals(10,layouts.get());
        s.scroll(3); layouts.set(0);
        s.select(messages,10,m->{layouts.incrementAndGet();return layout(m);});
        assertTrue(layouts.get()<=13);
    }
}
