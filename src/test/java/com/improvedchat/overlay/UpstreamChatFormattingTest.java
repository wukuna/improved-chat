package com.improvedchat.overlay;
import com.improvedchat.ChatDisplayText;
import com.improvedchat.model.FontSize;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;
public class UpstreamChatFormattingTest {
 @Test public void nativePipePayloadsAreTypeSpecific(){
  assertEquals("Achievement",ChatDisplayText.normalize(ChatMessageType.GAMEMESSAGE,"CA_ID:565|Achievement"));
  assertEquals("Arena",ChatDisplayText.normalize(ChatMessageType.CLAN_MESSAGE,"p|Arena"));
  assertEquals("Group",ChatDisplayText.normalize(ChatMessageType.CLAN_GIM_MESSAGE,"|CA_ID:1|Group"));
  assertEquals("chat",ChatDisplayText.normalize(ChatMessageType.CLAN_GIM_CHAT,"|chat"));
  assertEquals("hello|friend",ChatDisplayText.normalize(ChatMessageType.PUBLICCHAT,"hello|friend"));
  assertEquals("hello|friend",ChatDisplayText.normalize(ChatMessageType.CLAN_CHAT,"hello|friend"));
  assertEquals("News",ChatDisplayText.normalize(ChatMessageType.BROADCAST,"News|0"));
  assertEquals("",ChatDisplayText.normalize(ChatMessageType.GAMEMESSAGE,"CA_ID:1|"));
 }
 @Test public void templatesExpandInDefinitionOrderWithoutActivatingStrikethrough(){
  assertEquals("Alice: Crab Quest",ChatDisplayText.expandTemplates("<str_quest_name_0=Crab Quest>Alice: <str_quest_name_0>"));
  assertEquals("literal $ \\ end",ChatDisplayText.expandTemplates("<str_a=literal $ \\><str_a> end"));
 }
 @Test public void formattingSurvivesWrappingAndEscapedAnglesStayVisible(){
  Graphics2D g=new BufferedImage(300,100,BufferedImage.TYPE_INT_ARGB).createGraphics();try{
   FontMetrics fm=g.getFontMetrics();java.util.List<TextSegment> segments=ChatRenderUtils.parseTextWithColoursAndIcons("<html><u=ff0000><str><shad=00ff00>long words</shad></str></u><lt>x<gt><nbh><n>tail",fm,null,true,Color.WHITE,FontSize.REGULAR,null);
   assertEquals(Color.RED,segments.get(0).underline);assertEquals(Color.GREEN,segments.get(0).shadow);assertEquals(new Color(0x800000),segments.get(0).strikethrough);
   java.util.List<java.util.List<TextSegment>> lines=ChatRenderUtils.wrapSegments(segments,fm,40,40,Color.WHITE);
   assertTrue(lines.size()>1);assertEquals(Color.RED,lines.get(0).get(0).underline);
   StringBuilder text=new StringBuilder();for(TextSegment seg:segments)if(seg.iconId==-1)text.append(seg.text);
   assertEquals("long words<x>-tail",text.toString());
  }finally{g.dispose();}
 }
 @Test public void malformedColorsAndBreaksDoNotLeakOrCarryFormatting(){
  Graphics2D g=new BufferedImage(300,100,BufferedImage.TYPE_INT_ARGB).createGraphics();try{
   java.util.List<TextSegment> seg=ChatRenderUtils.parseTextWithColoursAndIcons("<u>A<br>B<col=zz>C",g.getFontMetrics(),null,true,Color.WHITE,FontSize.REGULAR,null);
   assertEquals(Color.BLACK,seg.get(0).underline);assertNull(seg.get(2).underline);assertEquals("B",seg.get(2).text);assertEquals(Color.WHITE,seg.get(3).color);
  }finally{g.dispose();}
 }
 @Test public void friendsChatInfoHighlightUsesRuneLiteConfiguredColor(){
  Graphics2D g=new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB).createGraphics();try{
   net.runelite.client.config.ChatColorConfig colors=(net.runelite.client.config.ChatColorConfig)java.lang.reflect.Proxy.newProxyInstance(net.runelite.client.config.ChatColorConfig.class.getClassLoader(),new Class[]{net.runelite.client.config.ChatColorConfig.class},(o,m,a)->m.getName().equals("opaqueFriendsChatInfoHighlight")?Color.RED:m.getReturnType()==Color.class?Color.BLUE:null);
   com.improvedchat.OverlayColorRuleEngine.configure(new com.improvedchat.ImprovedChatConfig(){@Override public com.improvedchat.ChatColorSource chatColorSource(){return com.improvedchat.ChatColorSource.OPAQUE;}});
   java.util.List<TextSegment> seg=ChatRenderUtils.parseTextWithColoursAndIcons("<colHIGHLIGHT>Alice joined</col>",g.getFontMetrics(),null,true,Color.BLUE,FontSize.REGULAR,colors,ChatMessageType.FRIENDSCHATNOTIFICATION);
   assertEquals(Color.RED,seg.get(0).color);
  }finally{g.dispose();com.improvedchat.OverlayColorRuleEngine.configure(null);}
 }

}
