package com.improvedchat.chatbox.clean.data;

import com.improvedchat.chatbox.clean.ChannelNameManager;
import com.improvedchat.ImprovedChatConfig;
import static com.improvedchat.ImprovedChatConfig.DEFAULT_CUSTOM_CHANNEL_NAME;
import com.improvedchat.chatbox.clean.util.CleanChatUtil;
import static com.improvedchat.chatbox.clean.util.CleanChatUtil.CURRENT_CLAN_REPLACER;
import static com.improvedchat.chatbox.clean.util.CleanChatUtil.sanitizeName;
import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@AllArgsConstructor
public enum ChatChannel
{
	// TODO: Cache booleans & indent mode config values
	CLAN(
		ImprovedChatConfig::removeClanName,
		ChannelNameManager::getClanNames,
		(c, t) -> false,
		ChannelNameManager::getShortClanName,
		ImprovedChatConfig::removeClanRank,
		ImprovedChatConfig::clanChannelColor
	),
	GUEST_CLAN(
		ImprovedChatConfig::removeGuestClanName,
		ChannelNameManager::getGuestClanNames,
		(c, t) -> false,
		ChannelNameManager::getShortGuestClanName,
		c -> false,
		ImprovedChatConfig::guestClanChannelColor
	),
	FRIENDS_CHAT(
		ImprovedChatConfig::removeFriendsChatName,
		ChannelNameManager::getFriendsChatNames,
		(c, t) -> false,
		ChannelNameManager::getShortFriendsChatName,
		c -> false,
		ImprovedChatConfig::friendsChannelColor
	),
	GROUP_IRON(
		ImprovedChatConfig::removeGroupIronName,
		ChannelNameManager::getGroupIronNames,
		(config, tab) -> config.removeGroupIronFromClan() && tab == ChatTab.CLAN,
		ChannelNameManager::getShortGroupIronName,
		c -> false,
		ImprovedChatConfig::groupIronChannelColor
	);

	public List<String> getNames(ChannelNameManager channelNameManager)
	{
		return getNames.apply(channelNameManager);
	}

	public boolean isChannelNameRemovalEnabled(ImprovedChatConfig config)
	{
		return isEnabled.apply(config);
	}

	public boolean isTabBlocked(ImprovedChatConfig config, ChatTab tab)
	{
		return isTabBlocked.apply(config, tab);
	}

	public boolean isShortNameDefault(ChannelNameManager channelNameManager)
	{
		// Compares *un-substituted* shortName
		return Objects.equals(getShortName.apply(channelNameManager), DEFAULT_CUSTOM_CHANNEL_NAME);
	}

	public String getShortName(ChannelNameManager channelNameManager, String matchedName)
	{
		final String shortName = getShortName.apply(channelNameManager);

		if (shortName.contains(CURRENT_CLAN_REPLACER))
		{
			return shortName.replace(CURRENT_CLAN_REPLACER, matchedName);
		}
		else
		{
			return shortName;
		}
	}

	public boolean isRemoveRankEnabled(ImprovedChatConfig config)
	{
		return isRemoveRank.apply(config);
	}

	public Color getColor(ImprovedChatConfig config)
	{
		return getColor.apply(config);
	}

	private final Function<ImprovedChatConfig, Boolean> isEnabled;
	private final Function<ChannelNameManager, List<String>> getNames;
	private final BiFunction<ImprovedChatConfig, ChatTab, Boolean> isTabBlocked;
	private final Function<ChannelNameManager, String> getShortName;
	private final Function<ImprovedChatConfig, Boolean> isRemoveRank;
	private final Function<ImprovedChatConfig, Color> getColor;

	public static Pair<ChatChannel, String> findChannelMatch(String channel, ChannelNameManager channelNameManager)
	{
		for (ChatChannel channelRemoval : ChatChannel.values())
		{
			if (channel == null)
			{
				break;
			}
			String widgetChannelName = sanitizeName(channel);
			String matchedChannelName = channelRemoval.getNames(channelNameManager).stream()
				.map(CleanChatUtil::sanitizeName)
				.filter(widgetChannelName::contains)
				.findFirst()
				.orElse(null);

			if (matchedChannelName != null)
			{
				return Pair.of(channelRemoval, matchedChannelName);
			}
		}

		return null;
	}
}
