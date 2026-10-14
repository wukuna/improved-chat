package com.improvedchat;
import java.awt.Color;
import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.*;
public class NativeColorPayloadTest {
 @Test public void forceColorRetainsClanTaskAndBroadcastMetadata(){
  assertEquals("CA_ID:565|<col=ff0000>Achievement</col>",MessageColorRuleEngine.forceColor("CA_ID:565|Achievement",Color.RED,ChatMessageType.GAMEMESSAGE));
  assertEquals("|CA_ID:1|<col=ff0000>Group</col>",MessageColorRuleEngine.forceColor("|CA_ID:1|Group",Color.RED,ChatMessageType.CLAN_GIM_MESSAGE));
  assertEquals("p|<col=ff0000>Arena</col>",MessageColorRuleEngine.forceColor("p|Arena",Color.RED,ChatMessageType.CLAN_MESSAGE));
  assertEquals("<col=ff0000>News</col>|0",MessageColorRuleEngine.forceColor("News|0",Color.RED,ChatMessageType.BROADCAST));
 }
 @Test public void playerPipeAndGimChatStayInTheirNativeForms(){
  assertEquals("<col=ff0000>hello|friend</col>",MessageColorRuleEngine.forceColor("hello|friend",Color.RED,ChatMessageType.PUBLICCHAT));
  assertEquals("|<col=ff0000>hello|friend</col>",MessageColorRuleEngine.forceColor("|hello|friend",Color.RED,ChatMessageType.CLAN_GIM_CHAT));
 }
}
