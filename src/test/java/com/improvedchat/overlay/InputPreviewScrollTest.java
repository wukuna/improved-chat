package com.improvedchat.overlay;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.ImprovedChatPlugin;
import com.improvedchat.chatbox.offline.ClanChatPresentationService;
import com.improvedchat.model.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InputPreviewScrollTest {
    @Test public void longInputShowsTheSameTailAndKeepsTheNameAcrossFontsAndAlignments() {
        Fixture f = new Fixture();
        String tail = " trailing words".repeat(16) + " LATEST END";
        for (String font : new String[]{"RuneScape", "Arial", "Courier New"}) {
            f.config.setFontFamily(font);
            for (FontSize size : FontSize.values()) for (TextAlignment alignment : TextAlignment.values()) {
                f.config.setFontSize(size); f.config.setTextAlignment(alignment);
                BufferedImage first = f.render("z".repeat(40) + tail);
                BufferedImage second = f.render("q".repeat(60) + tail);
                samePixels(first, second, 0, 320);
                assertTrue("Typing marker must be visible near the right edge", painted(first, 260, 276));
                assertFalse("Input must stay inside right padding", painted(first, 276, 320));
            }
        }
        f.config.setFontFamily("RuneScape"); f.config.setFontSize(FontSize.REGULAR);
        f.config.setTextAlignment(TextAlignment.LEFT);
        samePixels(f.render("Hello"), f.render(tail), 4, 34);
    }

    @Test public void deletingClearingAndResizingRecomputeTheVisibleInputWithoutRetainedScroll() {
        Fixture f = new Fixture(); f.render("Long sentence ".repeat(20));
        samePixels(new Fixture().render("Hi"), f.render("Hi"), 0, 320);
        f.config.setPreviewOnlyWhenTyping(true);
        assertFalse(painted(f.render(""), 0, 320));
        f.config.setPreviewOnlyWhenTyping(false);
        assertTrue(painted(f.render(""), 0, 320));
        f.overlay.setWidth(150); BufferedImage narrow = f.render("Long sentence ".repeat(20));
        assertTrue(painted(narrow, 130, 146)); assertFalse(painted(narrow, 146, 320));
        f.overlay.setWidth(280);
        samePixels(new Fixture().render("Hi"), f.render("Hi"), 0, 320);
        f.config.setShowInputPreview(false); assertFalse(painted(f.render("Hi"), 0, 320));
        f.config.setShowInputPreview(true); when(f.client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
        assertFalse(painted(f.render("Hi"), 0, 320));
    }

    @Test public void nativeWidgetMarkupAndFallbackRenderTheSameInputAndLeaveItUntouched() {
        Fixture f = new Fixture(); String input = "Some typed text ".repeat(15);
        BufferedImage fallback = f.render(input);
        Widget widget = mock(Widget.class);
        when(f.client.getWidget(InterfaceID.Chatbox.INPUT)).thenReturn(widget);
        when(widget.getText()).thenReturn("<col=ffffff>Alice:</col> " + input + "*");
        samePixels(fallback, f.render(input), 0, 320);
        verify(widget, never()).setText(anyString());
        verify(f.client, never()).setVarcStrValue(anyInt(), anyString());
    }

    @Test public void animatedStylesStayClippedAndPreviewIllustratesShortAndLongInput() throws Exception {
        Fixture f = new Fixture();
        for (AnimatedTextStyle style : AnimatedTextStyle.values()) {
            f.config.setAnimatedTextStyle(style);
            BufferedImage image = f.render("Long sentence ".repeat(20));
            assertTrue(style.toString(), painted(image, 260, 276));
            assertFalse(painted(image, 276, 320));
        }
        f.config.setAnimatedTextStyle(AnimatedTextStyle.NORMAL);
        BufferedImage preview = new BufferedImage(380, 160, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = preview.createGraphics();
        g.setColor(new Color(35,35,35)); g.fillRect(0,0,380,160);
        g.setColor(Color.WHITE); g.drawString("Short input",12,20); g.drawImage(f.render("Hello there"),12,25,null);
        g.setColor(Color.WHITE); g.drawString("Long input: username stays, newest text remains visible",12,85);
        g.drawImage(f.render("The beginning of this sentence scrolls away while I keep typing the latest words"),12,90,null);
        g.dispose(); ImageIO.write(preview,"png",new java.io.File("build/input-preview-scroll.png"));
    }

    private static boolean painted(BufferedImage image, int left, int right) {
        for (int y=0;y<image.getHeight();y++) for(int x=left;x<right;x++) if((image.getRGB(x,y)>>>24)!=0)return true;
        return false;
    }
    private static void samePixels(BufferedImage a, BufferedImage b, int left, int right) {
        for(int y=0;y<a.getHeight();y++)for(int x=left;x<right;x++)assertEquals("Pixel "+x+","+y,a.getRGB(x,y),b.getRGB(x,y));
    }
    private static final class Fixture {
        final Client client=mock(Client.class);
        final OverlayConfig config=new OverlayConfig();
        final DynamicChatOverlay overlay;
        Fixture() {
            when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
            Player player=mock(Player.class);when(player.getName()).thenReturn("Alice");when(client.getLocalPlayer()).thenReturn(player);
            config.setShowInputPreview(true);config.setAlwaysVisible(true);config.setDynamicHeight(true);config.setWidgetWidth(280);
            config.getTextSettings().setChannelColor(1,Color.CYAN);
            ImprovedChatPlugin plugin=mock(ImprovedChatPlugin.class);when(plugin.getMessagesForOverlay(config)).thenReturn(Collections.emptyList());
            overlay=new DynamicChatOverlay(plugin,new ImprovedChatConfig(){},client,null,new ClanChatPresentationService(),config);
        }
        BufferedImage render(String typed) {
            when(client.getVarcStrValue(VarClientID.CHATINPUT)).thenReturn(typed);
            BufferedImage image=new BufferedImage(320,48,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
            Rectangle clip=new Rectangle(0,0,320,48);g.setClip(clip);
            try{overlay.render(g);assertEquals(clip,g.getClip().getBounds());}finally{g.dispose();}
            return image;
        }
    }
}
