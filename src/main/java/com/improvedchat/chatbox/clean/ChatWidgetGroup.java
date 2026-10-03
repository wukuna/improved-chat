package com.improvedchat.chatbox.clean;

import com.improvedchat.ImprovedChatConfig;
import static com.improvedchat.chatbox.clean.util.CleanChatUtil.getTextLength;
import static com.improvedchat.chatbox.clean.util.CleanChatUtil.getTextLineCount;
import static com.improvedchat.chatbox.clean.util.CleanChatUtil.wrapWithBrackets;
import static com.improvedchat.chatbox.clean.util.CleanChatUtil.wrapWithChannelNameRegex;
import com.improvedchat.chatbox.clean.data.ChatChannel;
import com.improvedchat.chatbox.clean.util.FormatterExtractor;
import java.awt.Color;
import static java.lang.Math.max;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

@Slf4j
@RequiredArgsConstructor
public class ChatWidgetGroup
{
	@NonNull
	private final Widget channel;
	@NonNull
	private final Widget rank;
	@NonNull
	private final Widget name;
	@NonNull
	private final Widget message;
	@NonNull
	private final Widget clickBox;

	@Setter
	@Nullable
	private ChatChannel channelType = null;

	private int messageIndentSpaces = 0;
	private int timestampX = -1;

	@Getter
	@Nullable
	private FormatterExtractor.ExtractionResult timestamp = null;

	public String getChannelText()
	{
		return channel.getText();
	}

	public int getHeight()
	{
		return message.getHeight();
	}

	public int getY()
	{
		if (channel.isHidden())
		{
			return message.getCanvasLocation().getY();
		}
		return channel.getCanvasLocation().getY();
	}

	public int getX()
	{
		if (channel.isHidden())
		{
			return message.getCanvasLocation().getX();
		}
		return channel.getCanvasLocation().getX();
	}

	public Color getColor(ImprovedChatConfig config)
	{
		return channelType != null ? channelType.getColor(config) : config.noChannelColor();
	}

	public int getTimestampX()
	{
		return timestampX >= 0 ? timestampX : getX();
	}

	public void place(final int y)
	{
		place(channel, y);
		place(rank, y);
		place(name, y);
		place(message, y);

		clickBox.setOriginalY(y);
		clickBox.setHidden(false);
		clickBox.revalidate();
	}

	public void calculateHeight(Client client)
	{
		if (!message.getText().isEmpty() && message.getWidth() > 0)
		{
			int numLines = getTextLineCount(message.getText(), message.getWidth(), messageIndentSpaces, client);
			int height = numLines * 14; // Height of each line is always 14
			message.setOriginalHeight(height);
			message.revalidate();

			clickBox.setOriginalHeight(height);
			clickBox.revalidate();
		}
	}

	public void calculateChannelIndent(ImprovedChatConfig config, String matchedChannelName, String widgetChannelText,
									   int timestampWidth, boolean isFixedWidthTimestampEnabled, Client client)
	{
		if (channelType == ChatChannel.FRIENDS_CHAT)
		{
			// For some reason the fc channel width is the entire length of the chatbox so to make things easier we adjust that here
			channel.setOriginalWidth(message.getOriginalX() - channel.getOriginalX());
			channel.revalidate();
		}

		int startOfChannel = widgetChannelText.indexOf(matchedChannelName);
		int endOfChannel = startOfChannel + matchedChannelName.length();

		int indentWidth = 0;

		int channelWidth = 0;
		int prefixWidth = 0;

		// TODO: See if there's something that we are missing when measuring so we can avoid adding all these hardcoded offsets
		if (channelType != null)
		{
			switch (config.indentationMode())
			{
				// Intentionally fallthrough
				case START:
					// extractTimestamp handles indents for start already
					if (isFixedWidthTimestampEnabled)
					{
						// Fixed-width timestamps reserve an exact pixel slot. Wrapped-line
						// indentation must use that slot width rather than the variable width of
						// the timestamp text that happened to be rendered on this message.
						prefixWidth = timestampWidth;
						indentWidth += prefixWidth;

						if (channelType.isChannelNameRemovalEnabled(config))
						{
							if (channelType == ChatChannel.FRIENDS_CHAT)
							{
								indentWidth += 1;
							}
							else
							{
								indentWidth -= 2;
							}
						}
					}
				case CHANNEL:
					if (!channelType.isChannelNameRemovalEnabled(config))
					{
						String channel = widgetChannelText.substring(startOfChannel, endOfChannel);
						channelWidth = getTextLength(channel, client);
						indentWidth += channelWidth;

						if (channelType != ChatChannel.FRIENDS_CHAT)
						{
							indentWidth += 1;
						}
						else
						{
							indentWidth += 4;
						}
					}
				case NAME:
					int nameWidth = 0;
					// FC puts name + channel into the channel widget
					if (channelType == ChatChannel.FRIENDS_CHAT)
					{
						if (isFixedWidthTimestampEnabled)
						{
							nameWidth = (channel.getWidth() - timestampWidth) - channelWidth;
						}
						else
						{
							nameWidth = channel.getWidth() - prefixWidth - channelWidth;
						}
					}
					else
					{
						if (!name.getText().isEmpty() && !name.isHidden())
						{
							nameWidth = name.getWidth();
						}
					}

					if (!rank.isHidden())
					{
						nameWidth += rank.getWidth();
					}

					indentWidth += nameWidth;

					if (indentWidth > 0 && channelType != ChatChannel.FRIENDS_CHAT)
					{
						indentWidth += 4;
					}
					else if (channelType == ChatChannel.FRIENDS_CHAT)
					{
						indentWidth -= 4;
					}
				case MESSAGE:
					// Already set by default
			}
		}

		if (indentWidth > 0)
		{
			messageIndentSpaces += max(0, indentWidth / 3);

			if (messageIndentSpaces > 0)
			{
				message.setOriginalX(message.getOriginalX() - indentWidth);
				message.setOriginalWidth(message.getOriginalWidth() + indentWidth);
				message.revalidate();
			}
		}
	}

	public void applyIndent()
	{
		if (messageIndentSpaces > 0)
		{
			// Indentation mode still uses native text spacing for wrapped-message indentation.
			// Fixed-width timestamps no longer participate in this approximation; they own a real
			// pixel slot in extractTimestamp/applyFixedTimestampSlot.
			message.setText(" ".repeat(messageIndentSpaces) + message.getText());
			message.revalidate();
		}
	}

	public void removeFromChannel(String text, Client client)
	{
		replaceChannelName(text, "", client);
	}

	public String replaceChannelName(String text, String newChannelName, Client client)
	{
		int currentWidth = getTextLength(wrapWithBrackets(text), client);
		int newWidth = getTextLength(newChannelName, client);
		int removedWidth = currentWidth - newWidth;

		String newText = channel.getText()
			// TODO: Target the channel name more precisely, this should do for now to avoid targeting timestamps in brackets
			.replaceFirst(wrapWithChannelNameRegex(text), newChannelName);

		// Remove trailing spaces - probably only happens with timestamps turned on
		if (newText.endsWith(" "))
		{
			newText = newText.substring(0, newText.length() - 1);
			removedWidth += getTextLength(" ", client);
		}

		// Remove double spaces - mainly found in friends chat since it has sender + username
		if (newText.contains("  "))
		{
			newText = newText.replaceFirst(" {2}", " ");
			removedWidth += getTextLength(" ", client);
		}

		channel.setText(newText);

		// Shift widgets X left if channel was removed
		shiftLeft(rank, removedWidth);
		shiftLeft(name, removedWidth);
		shiftLeft(message, removedWidth);

		// Expand the width of messages if channel was removed
		message.setOriginalWidth(message.getOriginalWidth() + removedWidth);
		message.revalidate();

		// Reduce channel width if it was removed
		channel.setOriginalWidth(channel.getOriginalWidth() - removedWidth);
		channel.revalidate();

		return newText;
	}

	public void removeRank()
	{
		if (!rank.isHidden())
		{
			rank.setHidden(true);

			int removedWidth = rank.getWidth();

			shiftLeft(name, removedWidth);
			shiftLeft(message, removedWidth);

			// Expand the width of messages if rank was removed
			expand(message, removedWidth);
		}
	}

	private void shiftLeft(Widget widget, int width)
	{
		widget.setOriginalX(widget.getOriginalX() - width);
		widget.revalidate();
	}

	private void expand(Widget widget, int width)
	{
		widget.setOriginalWidth(widget.getOriginalWidth() + width);
		widget.revalidate();
	}

	private void place(Widget widget, int y)
	{
		widget.setOriginalY(y);
		widget.revalidate();
	}

	public void extractTimestamp(FormatterExtractor.ExtractionResult template, int timestampWidth, Client client)
	{
		if (template == null)
		{
			timestamp = null;
			timestampX = -1;
			return;
		}

		Widget widget;
		Widget oppositeWidget;

		if (!message.getText().isEmpty())
		{
			widget = message;
			oppositeWidget = channel;
		}
		else
		{
			widget = channel;
			oppositeWidget = message;
		}

		timestamp = FormatterExtractor.extractFromText(template, widget.getText());

		if (timestamp == null)
		{
			timestamp = FormatterExtractor.extractFromText(template, oppositeWidget.getText());

			if (timestamp == null)
			{
				timestampX = -1;
				log.debug("Timestamp could not be extracted from template: `{}`, widget:`{}`, or opposite widget:`{}`", template, widget.getText(), oppositeWidget.getText());
				return;
			}
			widget = oppositeWidget;
		}

		timestampX = widget.getCanvasLocation().getX();
		int actualTimestampWidth = getTextLength(timestamp.getFormattedOutput(), client);
		widget.setText(timestamp.getRemainingText());

		applyFixedTimestampSlot(widget, actualTimestampWidth, timestampWidth);
	}

	private void applyFixedTimestampSlot(Widget timestampWidget, int actualTimestampWidth, int targetTimestampWidth)
	{
		int targetWidth = max(0, targetTimestampWidth);
		int actualWidth = max(0, actualTimestampWidth);
		int delta = targetWidth - actualWidth;

		if (timestampWidget == channel)
		{
			// The channel widget originally contains the timestamp text. Move its remaining text
			// behind the fixed slot, then shift every downstream prefix/message widget by exactly
			// the difference between the native timestamp width and the fixed slot width.
			channel.setOriginalX(channel.getOriginalX() + targetWidth);
			channel.setOriginalWidth(max(0, channel.getOriginalWidth() - actualWidth));
			channel.revalidate();

			shiftX(rank, delta);
			shiftX(name, delta);
			shiftXPreserveRight(message, delta);
		}
		else
		{
			// Game/system lines carry the timestamp in the message widget itself. Reserve the slot
			// directly in the widget geometry instead of approximating it with leading spaces.
			message.setOriginalX(message.getOriginalX() + targetWidth);
			message.setOriginalWidth(max(0, message.getOriginalWidth() - targetWidth));
			message.revalidate();
		}
	}

	private void shiftX(Widget widget, int amount)
	{
		if (amount == 0)
		{
			return;
		}
		widget.setOriginalX(widget.getOriginalX() + amount);
		widget.revalidate();
	}

	private void shiftXPreserveRight(Widget widget, int amount)
	{
		if (amount == 0)
		{
			return;
		}
		widget.setOriginalX(widget.getOriginalX() + amount);
		widget.setOriginalWidth(max(0, widget.getOriginalWidth() - amount));
		widget.revalidate();
	}

	@Override
	public String toString()
	{
		return "ChatWidgetGroup{" + ",\n" +
			"channel=" + channel.getText() + ",\n" +
			"rank=" + rank.getSpriteId() + ",\n" +
			"name=" + name.getText() + ",\n" +
			"message=" + message.getText() + ",\n" +
			"channelType=" + (channelType != null ? channel.getName() : null) + ",\n" +
			"timestamp=" + (timestamp != null ? timestamp.getFormattedOutput() : null) + "\n" +
			'}';
	}
}
