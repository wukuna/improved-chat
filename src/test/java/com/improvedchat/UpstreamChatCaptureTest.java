package com.improvedchat;
import java.lang.reflect.*;
import java.util.*;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.gameval.*;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import static org.junit.Assert.*;
public class UpstreamChatCaptureTest {
 private static void set(Object o,String n,Object v)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 @Test public void disabledLoginNotificationsNeverEnterOverlayHistory()throws Exception{
  ImprovedChatPlugin p=new ImprovedChatPlugin();set(p,"config",new ImprovedChatConfig(){});
  Client c=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(o,m,a)->{if(m.getName().equals("getVarbitValue"))return ((Integer)a[0])==VarbitID.LOGINLOGOUT_SETTING?2:0;return UserCombinationAuditTest.def(m.getReturnType());});set(p,"client",c);
  p.onChatMessage(new ChatMessage(null,ChatMessageType.LOGINLOGOUTNOTIFICATION,"Alice","Alice has logged in.","",0));
  Field f=ImprovedChatPlugin.class.getDeclaredField("messages");f.setAccessible(true);assertTrue(((List<?>)f.get(p)).isEmpty());
 }
 @Test public void privateRowsRestoreWithoutHidingTimerOrRevealingNativeHiddenRows()throws Exception{
  ImprovedChatPlugin p=new ImprovedChatPlugin();NativeUserCombinationAuditTest.W root=new NativeUserCombinationAuditTest.W(-1,0,0),timer=new NativeUserCombinationAuditTest.W(-1,0,4),pm=new NativeUserCombinationAuditTest.W(-1,0,4),nativeHidden=new NativeUserCombinationAuditTest.W(-1,0,4);
  timer.p.put("Text","System update in: 2:30");timer.p.put("Hidden",false);pm.p.put("Text","From Alice: hello");pm.p.put("Hidden",false);nativeHidden.p.put("Text","");nativeHidden.p.put("Hidden",true);
  root.p.put("Hidden",false);root.p.put("DynamicChildren",new Widget[]{timer.w,null,null,null,pm.w,nativeHidden.w});
  Client c=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class[]{Client.class},(o,m,a)->m.getName().equals("getWidget")&&a.length==2&&((Integer)a[0])==InterfaceID.PM_CHAT?root.w:UserCombinationAuditTest.def(m.getReturnType()));set(p,"client",c);
  Method apply=ImprovedChatPlugin.class.getDeclaredMethod("setPmWidgetsHidden",boolean.class);apply.setAccessible(true);apply.invoke(p,true);
  assertEquals(false,root.p.get("Hidden"));assertEquals(false,timer.p.get("Hidden"));assertEquals(true,pm.p.get("Hidden"));
  apply.invoke(p,false);assertEquals(false,pm.p.get("Hidden"));assertEquals(true,nativeHidden.p.get("Hidden"));
 }
}
