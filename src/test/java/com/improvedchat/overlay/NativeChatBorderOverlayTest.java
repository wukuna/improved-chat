package com.improvedchat.overlay;

import com.improvedchat.ImprovedChatConfig;
import com.improvedchat.model.BorderThickness;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NativeChatBorderOverlayTest {
    @Test public void followsLiveBoundsInBothLayoutsWithoutChangingWidgetsOrGraphics() {
        Fixture f=new Fixture();
        for(boolean resized:new boolean[]{false,true}) {
            when(f.client.isResized()).thenReturn(resized);
            BufferedImage image=f.render();assertTrue(count(image)>0);
            assertInside(image,new Rectangle(30,45,240,100));
            assertEquals(0,image.getRGB(100,80));
        }
        when(f.parent.getRelativeX()).thenReturn(50);when(f.area.getWidth()).thenReturn(180);
        BufferedImage moved=f.render();assertInside(moved,new Rectangle(70,45,180,100));
        assertEquals(0,moved.getRGB(30,80));assertTrue(count(moved)>0);
        for(Widget widget:new Widget[]{f.area,f.parent,f.messages})
            assertFalse(mockingDetails(widget).getInvocations().stream().anyMatch(i->i.getMethod().getName().startsWith("set")));
    }

    @Test public void hiddenCollapsedDialogueAndMissingChatNeverDrawAndCanReturn() {
        Fixture f=new Fixture();assertFalse(new ImprovedChatConfig(){}.chatboxBorder());
        when(f.config.chatboxBorder()).thenReturn(false);assertEquals(0,count(f.render()));
        when(f.config.chatboxBorder()).thenReturn(true);when(f.area.isHidden()).thenReturn(true);assertEquals(0,count(f.render()));
        when(f.area.isHidden()).thenReturn(false);when(f.messages.isHidden()).thenReturn(true);assertEquals(0,count(f.render()));
        when(f.messages.isHidden()).thenReturn(false);when(f.client.getVarcIntValue(VarClientID.CHAT_VIEW)).thenReturn(1337);assertEquals(0,count(f.render()));
        when(f.client.getVarcIntValue(VarClientID.CHAT_VIEW)).thenReturn(0);when(f.client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);assertEquals(0,count(f.render()));
        when(f.client.getGameState()).thenReturn(GameState.LOGGED_IN);when(f.client.getWidget(InterfaceID.Chatbox.CHATAREA)).thenReturn(null);assertEquals(0,count(f.render()));
        when(f.client.getWidget(InterfaceID.Chatbox.CHATAREA)).thenReturn(f.area);assertTrue(count(f.render())>0);
        assertTrue(f.overlay.getMenuEntries().isEmpty());
    }

    @Test public void thicknessRoundedRgbAndOpacityAreIndependent() throws Exception {
        Fixture f=new Fixture();
        when(f.config.chatboxBorderThickness()).thenReturn(BorderThickness.THIN);int thin=count(f.render());
        when(f.config.chatboxBorderThickness()).thenReturn(BorderThickness.EXTRA_THICK);assertTrue(count(f.render())>thin);
        BufferedImage square=f.render();assertNotEquals(0,square.getRGB(31,46));
        when(f.config.chatboxBorderRounded()).thenReturn(true);BufferedImage rounded=f.render();assertEquals(0,rounded.getRGB(31,46));
        when(f.config.chatboxBorderRgb()).thenReturn(true);BufferedImage rgb=f.render();Set<Integer> colors=new HashSet<>();
        for(int y=0;y<rgb.getHeight();y++)for(int x=0;x<rgb.getWidth();x++)if((rgb.getRGB(x,y)>>>24)>0)colors.add(rgb.getRGB(x,y)&0xffffff);
        assertTrue(colors.size()>20);assertInside(rgb,new Rectangle(30,45,240,100));
        when(f.config.chatboxBorderColor()).thenReturn(new Color(1,2,3,0));assertEquals(0,count(f.render()));
        when(f.config.chatboxBorderColor()).thenReturn(new Color(1,2,3,70));BufferedImage translucent=f.render();
        assertTrue(count(translucent)>0);
        assertTrue((translucent.getRGB(150,46)>>>24)<=70);
        BufferedImage preview=new BufferedImage(600,190,BufferedImage.TYPE_INT_RGB);Graphics2D g=preview.createGraphics();
        g.setColor(new Color(35,35,35));g.fillRect(0,0,600,190);g.setColor(Color.WHITE);
        g.drawString("Regular chatbox border — solid",30,25);g.drawString("Rounded + RGB",330,25);
        g.drawImage(square,0,0,null);g.drawImage(rgb,300,0,null);g.dispose();ImageIO.write(preview,"png",new java.io.File("build/native-chat-border-preview.png"));
    }

    private static int count(BufferedImage image){int n=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>0)n++;return n;}
    private static void assertInside(BufferedImage image,Rectangle bounds){for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if(!bounds.contains(x,y))assertEquals("Outside chat at "+x+","+y,0,image.getRGB(x,y));}
    private static final class Fixture {
        final Client client=mock(Client.class);final ImprovedChatConfig config=mock(ImprovedChatConfig.class);
        final Widget area=mock(Widget.class),parent=mock(Widget.class),messages=mock(Widget.class);
        final NativeChatBorderOverlay overlay=new NativeChatBorderOverlay(client,config);
        Fixture(){
            when(client.getGameState()).thenReturn(GameState.LOGGED_IN);when(client.getWidget(InterfaceID.Chatbox.CHATAREA)).thenReturn(area);when(client.getWidget(InterfaceID.Chatbox.SCROLLAREA)).thenReturn(messages);
            when(area.getParent()).thenReturn(parent);when(parent.getRelativeX()).thenReturn(10);when(parent.getRelativeY()).thenReturn(15);
            when(area.getRelativeX()).thenReturn(20);when(area.getRelativeY()).thenReturn(30);when(area.getWidth()).thenReturn(240);when(area.getHeight()).thenReturn(100);
            when(config.chatboxBorder()).thenReturn(true);when(config.chatboxBorderColor()).thenReturn(Color.CYAN);when(config.chatboxBorderThickness()).thenReturn(BorderThickness.NORMAL);when(config.chatboxBorderRgbSeconds()).thenReturn(8);
        }
        BufferedImage render(){
            BufferedImage image=new BufferedImage(300,180,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
            Rectangle clip=new Rectangle(0,0,300,180);g.setClip(clip);g.setColor(Color.MAGENTA);Stroke stroke=g.getStroke();java.awt.geom.AffineTransform transform=g.getTransform();
            try{assertNull(overlay.render(g));assertEquals(clip,g.getClip().getBounds());assertEquals(Color.MAGENTA,g.getColor());assertEquals(stroke,g.getStroke());assertEquals(transform,g.getTransform());}finally{g.dispose();}return image;
        }
    }
}
