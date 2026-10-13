package com.improvedchat;
import com.improvedchat.overlay.*;
import com.improvedchat.model.*;
import com.improvedchat.chatbox.offline.ClanChatPresentationService;
import com.improvedchat.chatbox.resize.internal.SizeClamps;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.util.*;
import net.runelite.api.*;
import net.runelite.client.config.ChatColorConfig;
import org.junit.Test;
import static org.junit.Assert.*;
public class UserRenderAuditTest {
 static java.util.List<Map<String,Boolean>> pairs(java.util.List<String> keys){
  java.util.List<Map<String,Boolean>> profiles=new ArrayList<>();Set<String> remaining=new HashSet<>();
  for(int i=0;i<keys.size();i++)for(int j=i+1;j<keys.size();j++)for(int b=0;b<4;b++)remaining.add(i+":"+j+":"+b);
  Random random=new Random(20261013);int n=0;while(!remaining.isEmpty()){
   Map<String,Boolean> p=new HashMap<>();for(String k:keys)p.put(k,n<2?n==1:random.nextBoolean());boolean useful=n<2;
   for(int i=0;i<keys.size();i++)for(int j=i+1;j<keys.size();j++){int bits=(p.get(keys.get(i))?1:0)+(p.get(keys.get(j))?2:0);useful|=remaining.remove(i+":"+j+":"+bits);}if(useful)profiles.add(p);assertTrue("Pair generator did not converge",++n<1000);
  }return profiles;
 }
 @Test public void globalAndOverlayPairsRenderAtNumericBoundaries()throws Exception{
  ImprovedChatConfig defaults=new ImprovedChatConfig(){};Map<String,Boolean> global=new HashMap<>();ImprovedChatConfig config=(ImprovedChatConfig)Proxy.newProxyInstance(ImprovedChatConfig.class.getClassLoader(),new Class[]{ImprovedChatConfig.class},(o,m,a)->global.containsKey(m.getName())?global.get(m.getName()):m.invoke(defaults,a));
  java.util.List<String> keys=new ArrayList<>();for(Method m:ImprovedChatConfig.class.getDeclaredMethods())if(m.getReturnType()==boolean.class)keys.add(m.getName());Collections.sort(keys);java.util.List<Map<String,Boolean>> profiles=pairs(keys);
  java.util.List<Method> setters=new ArrayList<>();for(Method m:OverlayConfig.class.getDeclaredMethods())if(m.getName().startsWith("set")&&m.getParameterCount()==1&&m.getParameterTypes()[0]==boolean.class)setters.add(m);setters.sort(Comparator.comparing(Method::getName));java.util.List<String> names=new ArrayList<>();for(Method m:setters)names.add(m.getName());java.util.List<Map<String,Boolean>> overlays=pairs(names);
  Client client=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(o,m,a)->{switch(m.getName()){case "getGameState":return GameState.LOGGED_IN;case "getModIcons":return new IndexedSprite[0];case "getVarcStrValue":return "typed message";case "getMouseCanvasPosition":return new net.runelite.api.Point(0,0);case "getVarcIntValue":return 1337;case "isResized":return true;}return UserCombinationAuditTest.def(m.getReturnType());});
  ChatColorConfig colors=(ChatColorConfig)Proxy.newProxyInstance(ChatColorConfig.class.getClassLoader(),new Class[]{ChatColorConfig.class},(o,m,a)->m.getReturnType()==Color.class?Color.WHITE:UserCombinationAuditTest.def(m.getReturnType()));ClanChatPresentationService clan=new ClanChatPresentationService();UserCombinationAuditTest.set(clan,ClanChatPresentationService.class,"client",client);
  ImprovedChatPlugin plugin=new ImprovedChatPlugin(){@Override public java.util.List<OverlayMessage> getMessagesForOverlay(OverlayConfig c){return Arrays.asList(OverlayMessage.senderMessage("<col=00ff00>Alice</col>","Channel","<col=ff0000>A long message <img=999> with a link and repeated repeated repeated repeated words.</col>",System.currentTimeMillis(),ChatMessageType.PUBLICCHAT,false));}@Override public boolean isChatboxHidden(){return true;}};
  BufferedImage image=new BufferedImage(1200,1000,BufferedImage.TYPE_INT_ARGB);int cases=0;
  for(int n=0;n<Math.max(profiles.size(),overlays.size());n++)for(int boundary=0;boundary<4;boundary++)for(PlacementMode placement:PlacementMode.values()){
   global.clear();global.putAll(profiles.get(n%profiles.size()));MessageColorRuleEngine.configure(config);OverlayColorRuleEngine.configure(config);AttentionEngine.setClientFocused(n%2==0);OverlayConfig c=OverlayConfig.preset("Render","all");Map<String,Boolean> p=overlays.get(n%overlays.size());for(Method m:setters)m.invoke(c,p.get(m.getName()));c.setWidgetWidth(boundary%2==0?150:1024);c.setMaxMessages(boundary<2?1:20);c.setPaddingHorizontal(boundary%2==0?0:100);c.setPaddingVertical(boundary%2==0?0:100);c.setPlacementMode(placement);c.setTextAlignment(TextAlignment.values()[n%TextAlignment.values().length]);c.setFontSize(FontSize.values()[n%FontSize.values().length]);c.setAttentionTrigger(AttentionTrigger.ALL_NEW_MESSAGES);c.setAttentionStopMode(AttentionStopMode.values()[n%AttentionStopMode.values().length]);c.setAttentionSpeed(AttentionSpeed.values()[n%AttentionSpeed.values().length]);c.normalize();DynamicChatOverlay overlay=new DynamicChatOverlay(plugin,config,client,colors,clan,c);Graphics2D g=image.createGraphics();try{Dimension size=overlay.render(g);if(c.isShow()&&placement==PlacementMode.FREE){assertNotNull("profile="+n+" boundary="+boundary,size);assertTrue(size.width>=150&&size.width<=1024);assertTrue(size.height>0);}}finally{g.dispose();}cases++;
  }MessageColorRuleEngine.configure(null);OverlayColorRuleEngine.configure(null);AttentionEngine.setClientFocused(true);System.out.println("AUDIT render global boolean keys="+keys.size()+" pair profiles="+profiles.size()+" overlay pair profiles="+overlays.size()+" rendered contexts="+cases);
 }
 @Test public void modalDialogueAndSecondarySizeClampMatrix(){
  int cases=0;for(ImprovedChatConfig.Revert modal:ImprovedChatConfig.Revert.values())for(ImprovedChatConfig.Revert dialog:ImprovedChatConfig.Revert.values())for(int bits=0;bits<8;bits++)for(int delta:new int[]{-10000,-1,0,1,10000}){
   ImprovedChatConfig c=new ImprovedChatConfig(){@Override public Revert revertForModals(){return modal;}@Override public Revert revertForDialogs(){return dialog;}};boolean fixed=(bits&1)!=0,modalOpen=(bits&2)!=0,dialogOpen=(bits&4)!=0;boolean zero=modalOpen&&(delta>0&&(fixed||modal.ungrows())||delta<0&&modal.unshrinks())||dialogOpen&&(delta>0&&dialog.ungrows()||delta<0&&dialog.unshrinks());assertEquals(zero?0:delta,SizeClamps.clamp(delta,fixed,modalOpen,dialogOpen,c));cases++;
  }System.out.println("AUDIT resize modal/dialog/fixed combinations="+cases);
 }
}
