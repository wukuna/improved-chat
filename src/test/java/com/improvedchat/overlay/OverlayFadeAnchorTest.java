package com.improvedchat.overlay;
import com.improvedchat.*;
import com.improvedchat.model.*;
import com.improvedchat.chatbox.offline.ClanChatPresentationService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.util.*;
import net.runelite.api.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class OverlayFadeAnchorTest {
 @Test public void disappearingRowKeepsRemainingBottomLineAtAllocatedAnchor(){
  java.util.List<OverlayMessage> messages=new ArrayList<>();long now=System.currentTimeMillis();messages.add(OverlayMessage.gameMessage("OLDER",now,ChatMessageType.GAMEMESSAGE,false));messages.add(OverlayMessage.gameMessage("NEWEST",now,ChatMessageType.GAMEMESSAGE,false));
  Client client=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(o,m,a)->{switch(m.getName()){case "getGameState":return GameState.LOGGED_IN;case "isResized":return true;case "getModIcons":return new IndexedSprite[0];case "getVarcIntValue":return 1337;}return m.getReturnType()==boolean.class?false:m.getReturnType()==int.class?0:null;});
  ImprovedChatPlugin p=new ImprovedChatPlugin(){@Override public java.util.List<OverlayMessage> getMessagesForOverlay(OverlayConfig c){return new ArrayList<>(messages);}@Override public boolean isChatboxHidden(){return true;}};
  OverlayConfig cfg=OverlayConfig.preset("Test","all");cfg.setShow(true);cfg.setDynamicHeight(true);cfg.setShowTimestamp(false);cfg.setBackgroundEnabled(false);cfg.setBorderEnabled(false);cfg.setFadeOutDuration(0);
  DynamicChatOverlay overlay=new DynamicChatOverlay(p,new ImprovedChatConfig(){},client,null,new ClanChatPresentationService(),cfg);
  BufferedImage before=new BufferedImage(600,300,BufferedImage.TYPE_INT_ARGB);Graphics2D g=before.createGraphics();Dimension size;try{size=overlay.render(g);}finally{g.dispose();}assertNotNull(size);overlay.getBounds().setSize(size);
  messages.remove(0);BufferedImage after=new BufferedImage(600,300,BufferedImage.TYPE_INT_ARGB);g=after.createGraphics();try{Dimension next=overlay.render(g);assertTrue(next.height<size.height);}finally{g.dispose();}
  assertEquals("Newest row must stay in its allocated bottom slot while bounds shrink",lastPaintedRow(before),lastPaintedRow(after));
 }
 private int lastPaintedRow(BufferedImage im){for(int y=im.getHeight()-1;y>=0;y--)for(int x=0;x<im.getWidth();x++)if((im.getRGB(x,y)>>>24)!=0)return y;return -1;}
}
