package com.improvedchat.overlay;

import com.improvedchat.model.FontSize;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChatRenderUtilsTest {
    private static FontMetrics metrics;

    @BeforeClass
    public static void setUp() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        metrics = graphics.getFontMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        graphics.dispose();
    }

    @Test
    public void senderTextDecodesAtEscape() {
        assertEquals("Bob@Home", textOf(ChatRenderUtils.parseTextWithIcons(
                "Bob<at>Home", metrics, null, Color.WHITE, FontSize.REGULAR)));
    }

    @Test
    public void messageTextDecodesRuneLiteEscapes() {
        assertEquals("a@<b>@", textOf(ChatRenderUtils.parseTextWithColoursAndIcons(
                "a<at><lt>b<gt><at>", metrics, null, true, Color.WHITE,
                FontSize.REGULAR, null)));
    }

    private static String textOf(List<TextSegment> segments) {
        StringBuilder text = new StringBuilder();
        for (TextSegment segment : segments) {
            if (segment.iconId == -1) {
                text.append(segment.text);
            }
        }
        return text.toString();
    }
}
