package com.improvedchat.chatbox.clean.data;

import com.improvedchat.ImprovedChatConfig;
import static com.improvedchat.chatbox.clean.util.CleanChatUtil.CLAN_INSTRUCTION_MESSAGE;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import net.runelite.client.util.Text;

@AllArgsConstructor
public enum ChatBlock
{
	CLAN_INSTRUCTION(
		ImprovedChatConfig::removeClanInstruction,
		CLAN_INSTRUCTION_MESSAGE
	),
	GUEST_CLAN_INSTRUCTION(
		ImprovedChatConfig::removeGuestClanInstruction,
		// "You are now a guest of x" is also included in this message, they are separated by a <br>
		"To talk, start each line of chat with /// or /gc"
	),
	GUEST_CLAN_RECONNECTING(
		ImprovedChatConfig::removeGuestClanReconnecting,
		"Attempting to reconnect to guest channel automatically..."
	),
	GROUP_IRON_INSTRUCTION(
		ImprovedChatConfig::removeGroupIronInstruction,
		"To talk in your Ironman Group's channel"
	),
	FRIENDS_CHAT_INSTRUCTION(
		ImprovedChatConfig::removeFriendsChatStartup,
		"To talk, start each line of chat with the / symbol."
	),
	FRIENDS_CHAT_ATTEMPTING(
		ImprovedChatConfig::removeFriendsAttempting,
		"Attempting to join chat-channel..."
	),
	FRIENDS_CHAT_NOW_TALKING(
		ImprovedChatConfig::removeFriendsNowTalking,
		"Now talking in chat-channel"
	),
	WELCOME(
		ImprovedChatConfig::removeWelcome,
		"Welcome to Old School RuneScape."
	),
	DRAGON_HARPOON(
		ImprovedChatConfig::hideSpecs,
		"Here fishy fishies!"
	),
	DRAGON_PICK(
		ImprovedChatConfig::hideSpecs,
		"Smashing!"
	),
	DRAGON_AXE(
		ImprovedChatConfig::hideSpecs,
		"Chop chop!"
	),
	DRAGON_BATTLEAXE(
		ImprovedChatConfig::hideSpecs,
		"Raarrrrrgggggghhhhhhh!"
	),
	;

	public boolean isEnabled(ImprovedChatConfig config)
	{
		return isEnabled.apply(config);
	}

	public boolean appliesTo(ImprovedChatConfig config, String message)
	{
		return isEnabled(config) && Text.removeTags(message).contains(this.message);
	}

	private final Function<ImprovedChatConfig, Boolean> isEnabled;
	private final String message;
}
