package com.improvedchat;
import com.improvedchat.chatbox.opacity.ChatboxOpacityModule;
import com.improvedchat.chatbox.menu.RemoveChatOptionsModule;
import java.lang.reflect.*;
import java.util.*;
import net.runelite.api.*;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import org.junit.Test;
import static org.junit.Assert.*;
public class NativeUserCombinationAuditTest {
 static class W {
  final Map<String,Object> p=new HashMap<>();final Widget w;
  W(int sprite,int opacity,int type){p.put("SpriteId",sprite);p.put("Opacity",opacity);p.put("Type",type);p.put("Filled",false);
   w=(Widget)Proxy.newProxyInstance(Widget.class.getClassLoader(),new Class[]{Widget.class},(o,m,a)->{
    if(m.getName().equals("hashCode"))return System.identityHashCode(o);if(m.getName().equals("equals"))return o==a[0];
    if(m.getName().startsWith("set")){p.put(m.getName().substring(3),a[0]);return m.getReturnType()==Widget.class?o:null;}
    String k=m.getName().startsWith("is")?m.getName().substring(2):m.getName().startsWith("get")?m.getName().substring(3):m.getName();return p.getOrDefault(k,UserCombinationAuditTest.def(m.getReturnType()));});
  }
 }
 static class F {
  W button=new W(100,42,5),surface=new W(-1,37,3),background=new W(-1,0,3);int[] opacity={150,220};boolean[] context={true,true,true};Client client;ChatboxOpacityModule module=new ChatboxOpacityModule();
  F()throws Exception {
   background.p.put("DynamicChildren",new Widget[]{surface.w});
   client=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(o,m,a)->{
    switch(m.getName()){case "getGameState":return GameState.LOGGED_IN;case "isResized":return context[0];case "getVarbitValue":return context[1]?1:0;case "getWidget":int id=a.length==1?(Integer)a[0]:((Integer)a[0]<<16)|(Integer)a[1];if(id==InterfaceID.Chatbox.CHAT_BACKGROUND)return background.w;if(id==((162<<16)|3))return button.w;return null;}return UserCombinationAuditTest.def(m.getReturnType());});
   ImprovedChatConfig c=new ImprovedChatConfig(){@Override public boolean enableChatboxOpacity(){return true;}@Override public int chatboxOpacity(){return opacity[0];}@Override public int buttonOpacity(){return opacity[1];}@Override public boolean opacityDialogueMenus(){return context[2];}};
   UserCombinationAuditTest.set(module,ChatboxOpacityModule.class,"client",client);UserCombinationAuditTest.set(module,ChatboxOpacityModule.class,"config",c);UserCombinationAuditTest.set(module,ChatboxOpacityModule.class,"eventBus",new EventBus());UserCombinationAuditTest.set(module,ChatboxOpacityModule.class,"clientThread",new ClientThread(){@Override public void invoke(Runnable r){r.run();}@Override public void invokeLater(Runnable r){r.run();}});
  }
  void changed(String key){ConfigChanged e=new ConfigChanged();e.setGroup(ImprovedChatConfig.GROUP);e.setKey(key);module.onConfigChanged(e);}
 }
 @Test public void opacityToggleMatrixRestoresOwnedBackgroundAndButton()throws Exception{
  int cases=0;for(int bg:new int[]{-1,0,150,255})for(int button:new int[]{-1,0,255})for(int bits=0;bits<16;bits++){
   F f=new F();f.opacity[0]=bg;f.opacity[1]=button;f.context[0]=(bits&1)!=0;f.context[1]=(bits&2)!=0;f.context[2]=(bits&4)!=0;f.surface.p.put("Type",(bits&8)!=0?5:3);
   for(int cycle=0;cycle<2;cycle++){f.module.startUp();f.module.reapplyAfterChatMutation();boolean live=f.context[0]&&f.context[1];boolean applyBg=live&&(f.context[2]||(bits&8)==0)&&bg!=-1;assertEquals(applyBg?bg:37,f.surface.p.get("Opacity"));assertEquals(live&&button!=-1?button:42,f.button.p.get("Opacity"));f.opacity[0]=-1;f.opacity[1]=-1;f.changed("chatboxOpacity");assertEquals(37,f.surface.p.get("Opacity"));assertEquals(42,f.button.p.get("Opacity"));assertEquals(100,f.button.p.get("SpriteId"));f.module.shutDown();f.opacity[0]=bg;f.opacity[1]=button;cases++;}
  }System.out.println("AUDIT native opacity enable/default/disable cycles="+cases);
 }
 @Test public void replacedButtonRestoresItsOwnOriginalStyle()throws Exception{
  F f=new F();f.module.startUp();f.button=new W(456,13,5);f.module.reapplyAfterChatMutation();f.opacity[1]=-1;f.changed("buttonOpacity");assertEquals("A rebuilt chat button must not inherit the old button sprite",456,f.button.p.get("SpriteId"));assertEquals(13,f.button.p.get("Opacity"));
 }
 @Test public void originallySpriteLessButtonStillRestores()throws Exception{
  F f=new F();f.button=new W(-1,13,5);f.module.startUp();f.opacity[1]=-1;f.changed("buttonOpacity");assertEquals("Returning to RuneLite default should restore opacity even without a sprite",13,f.button.p.get("Opacity"));assertEquals(5,f.button.p.get("Type"));assertEquals(false,f.button.p.get("Filled"));
 }
 @Test public void nonChatClipboardActionsSurviveRemoveChatOptions()throws Exception{
  for(boolean control:new boolean[]{false,true}){
   MenuEntry entry=(MenuEntry)Proxy.newProxyInstance(MenuEntry.class.getClassLoader(),new Class[]{MenuEntry.class},(o,m,a)->{switch(m.getName()){case "getParam1":return InterfaceID.INVENTORY<<16;case "getOption":return "Copy to clipboard";case "getType":return MenuAction.RUNELITE;}return UserCombinationAuditTest.def(m.getReturnType());});
   MenuEntry[][] menu={{entry}};Client client=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(o,m,a)->{switch(m.getName()){case "getMenuEntries":return menu[0];case "setMenuEntries":menu[0]=(MenuEntry[])a[0];return null;case "isKeyPressed":return control;case "getMouseCanvasPosition":return new Point(800,300);}return UserCombinationAuditTest.def(m.getReturnType());});
   RemoveChatOptionsModule module=new RemoveChatOptionsModule();UserCombinationAuditTest.set(module,RemoveChatOptionsModule.class,"client",client);UserCombinationAuditTest.set(module,RemoveChatOptionsModule.class,"config",new ImprovedChatConfig(){@Override public boolean enableRemoveChatOptions(){return true;}});UserCombinationAuditTest.set(module,RemoveChatOptionsModule.class,"eventBus",new EventBus());module.startUp();module.onMenuEntryAdded(new MenuEntryAdded(entry));assertEquals("Chat cleanup must not remove another plugin's copy action outside chat",1,menu[0].length);module.shutDown();
  }
 }
 @Test public void repeatedSpriteLessApplyDoesNotRecaptureOurOwnStyle()throws Exception{
  F f=new F();f.button=new W(-1,13,5);f.module.startUp();f.module.reapplyAfterChatMutation();f.module.reapplyAfterChatMutation();f.opacity[1]=-1;f.changed("buttonOpacity");assertEquals(13,f.button.p.get("Opacity"));assertEquals(5,f.button.p.get("Type"));assertEquals(false,f.button.p.get("Filled"));f.module.shutDown();
 }
 @Test public void laterStyleChangesRemainOwnedByTheirWriter()throws Exception{
  F f=new F();f.module.startUp();f.button.p.put("SpriteId",456);f.button.p.put("Opacity",13);f.opacity[1]=-1;f.changed("buttonOpacity");assertEquals(456,f.button.p.get("SpriteId"));assertEquals(13,f.button.p.get("Opacity"));assertEquals(5,f.button.p.get("Type"));assertEquals(false,f.button.p.get("Filled"));f.module.shutDown();
 }
 @Test public void sameWidgetNativeRebuildCapturesFreshStyle()throws Exception{
  F f=new F();f.module.startUp();f.button.p.put("SpriteId",456);f.button.p.put("Opacity",13);f.button.p.put("Type",5);f.button.p.put("Filled",false);f.module.reapplyAfterChatMutation();f.opacity[1]=-1;f.changed("buttonOpacity");assertEquals(456,f.button.p.get("SpriteId"));assertEquals(13,f.button.p.get("Opacity"));f.module.shutDown();
 }
 @Test public void clipboardCleanupIsScopedAndControlStillReveals()throws Exception{
  for(boolean inside:new boolean[]{false,true})for(boolean control:new boolean[]{false,true}){
   W lines=new W(-1,0,0);lines.p.put("Bounds",new java.awt.Rectangle(0,0,400,150));lines.p.put("Hidden",false);
   MenuEntry copy=(MenuEntry)Proxy.newProxyInstance(MenuEntry.class.getClassLoader(),new Class[]{MenuEntry.class},(o,m,a)->{switch(m.getName()){case "getParam1":return 0;case "getOption":return "<col=ff0000>Copy to clipboard</col>";case "getType":return MenuAction.RUNELITE;}return UserCombinationAuditTest.def(m.getReturnType());});
   MenuEntry[][] menu={{copy}};Client client=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(o,m,a)->{switch(m.getName()){case "getMenuEntries":return menu[0];case "setMenuEntries":menu[0]=(MenuEntry[])a[0];return null;case "isKeyPressed":return control;case "getMouseCanvasPosition":return new Point(inside?100:800,inside?50:300);case "getWidget":return a.length==1&&(Integer)a[0]==ComponentID.CHATBOX_MESSAGE_LINES?lines.w:null;}return UserCombinationAuditTest.def(m.getReturnType());});
   RemoveChatOptionsModule module=new RemoveChatOptionsModule();UserCombinationAuditTest.set(module,RemoveChatOptionsModule.class,"client",client);UserCombinationAuditTest.set(module,RemoveChatOptionsModule.class,"config",new ImprovedChatConfig(){@Override public boolean enableRemoveChatOptions(){return true;}});UserCombinationAuditTest.set(module,RemoveChatOptionsModule.class,"eventBus",new EventBus());module.startUp();module.onMenuEntryAdded(new MenuEntryAdded(copy));assertEquals(inside&&!control?0:1,menu[0].length);module.shutDown();
  }
 }
}
