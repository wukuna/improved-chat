# Improved Chat

Improved Chat is an all-in-one RuneLite chat customization plugin with custom overlays, per-overlay filtering, native chat resizing and collapse controls, cleanup tools, clan presentation, message styling, alerts, opacity controls, and dialogue text customization.

Most features are independent, so you can use only the parts you want.

## Highlights

- **Custom chat overlays** with independent message types, placement, size, fonts, colors, borders, timestamps, fade behavior, attention effects, and input preview.
- **Per-overlay chat filtering** with Off, Hide Matches, Show Only Matches, word, regex, and sender/name rules.
- **Clan presentation in overlays** with rank icons plus independently configurable offline status, icon, name color, and offline color.
- **Native chat resizing** in resizable and fixed layouts, including drag resizing, private-chat rewrapping, secondary sizes, and keybind control.
- **Collapsible native chat** with a compact single-button mode, unread indicators, hover text, and a collapse keybind.
- **Message color rules and alerts** with literal or regex matching, nine configurable rule colors, flash triggers, and overlay-only rainbow styling.
- **Native chat cleanup** for timestamps, channel labels, ranks, scrollbars, channel color bars, startup/reconnect messages, chat menus, and clan/friends/GIM presentation.
- **Offline clan status** for native chat and overlays.
- **Chatbox opacity controls** for the native chatbox, buttons, dialogue, and menus.
- **Dialogue text styling** for NPC, player, option-menu, and item/action dialogue.
- **Compatibility safeguards** for RuneLite Chat Color, Chat Filter, Emojis, Resource Packs, and Modern Chat.

## Custom overlays

Create and manage multiple overlays from the **Improved Chat** side panel.

Each overlay can independently control:

- enabled state, name, maximum messages, and fade duration;
- message types shown;
- free placement or placement above/below your character;
- X/Y offsets for player-attached overlays;
- text size, font, alignment, bold styling, and width;
- horizontal/vertical padding;
- background color and transparency;
- border color and thickness;
- timestamps;
- dynamic height and always-visible behavior;
- duplicate-count display;
- input preview and "only while typing" behavior;
- overlay-only text-color override;
- attention effects for the message, border, and background.

RuneLite's normal overlay movement system is used for free-position overlays.

> **[Example coming soon — Screenshot]** Overlay list and overlay editor.

> **[Example coming soon — Video]** Creating an overlay, selecting message types, moving it, and changing its appearance.

## Overlay filtering

Filtering can be global or completely independent per overlay.

- **Use Chat Filter Globally** applies the RuneLite global Chat Filter path to every overlay.
- **Use Global Chat Filter** applies that global filter only to the selected overlay.
- Otherwise, each overlay can use:
  - **Off**
  - **Hide Matches**
  - **Show Only Matches**
- Custom filters support:
  - filtered words;
  - case-insensitive regex rules;
  - sender/name rules;
  - literal sender matching;
  - sender regex rules using a `regex:` prefix.

Invalid custom regex lines are ignored safely.

Different overlays can therefore show different subsets of the same shared chat history without removing messages from the shared message pool.

> **[Example coming soon — Screenshot]** Two overlays using different filters on the same incoming chat.

> **[Example coming soon — Video]** Configuring Hide Matches, Show Only Matches, regex, and sender filters.

## Clan display and offline status

Clan presentation can be configured separately for every overlay.

Per-overlay controls include:

- **Show Clan Rank Icons**
- **Show Offline Status**
- **Show Offline Icon**
- **Color Offline Names**
- **Offline Color**

Clan, guest-clan, and Group Ironman chat are supported.

Overlay offline presentation is independent of the native-chat **Offline Clan Status** setting, so one can be enabled without the other.

The native-chat offline status feature can also add an offline icon and/or recolor offline clan-member names.

> **[Example coming soon — Screenshot]** The same clan messages with rank/offline indicators enabled in one overlay and disabled in another.

## Message colors, rules, and alerts

Improved Chat can use RuneLite Chat Color as its baseline color source or configured fallback colors for game, public, private, friends, clan, guest clan, Group Ironman, trade, challenge, broadcast, autochat, and other supported message types.

### Message Color Rules

Message Color Rules can affect the native chatbox and Improved Chat overlays.

Use one rule per line:

```text
is about to expire::1
has expired::1::flash
regex:^Your .* count is:.*::2
```

- `::1` through `::9` select one of nine configurable rule colors.
- Prefix a rule with `regex:` for a case-insensitive regular expression.
- Add `::flash` to trigger overlay attention behavior.
- Player-authored chat is excluded by default and can be enabled separately.

### Overlay Rainbow Rules

Rainbow rules affect Improved Chat overlays only and can color by word or visible character.

```text
achieved a new
has reached
regex:^Collection log.*
```

### Overlay attention controls

Each overlay can independently configure:

- whether attention flashing is enabled;
- what triggers it;
- whether it can flash while RuneLite is focused;
- stop behavior;
- duration;
- speed;
- message flashing;
- border flashing;
- background flashing.

> **[Example coming soon — Screenshot]** Message Color Rules, rule palette, and an in-game highlighted result.

> **[Example coming soon — Video]** Creating a color rule, regex rule, flash rule, and rainbow rule.

## Native chat resizing

Improved Chat can resize RuneLite's native chatbox in both **resizable** and **fixed** layouts.

Available controls include:

- resizable height and width changes;
- fixed-layout height changes;
- private split-chat width adjustment and live rewrapping;
- optional resizing of chat-tab buttons;
- interface-height growth when the chatbox grows;
- camera adjustment in fixed mode when growing chat;
- hideable fixed chat;
- automatic reversion for dialogue and interface/modal screens;
- Resource Pack compatibility controls to avoid drawing resize borders or stretching custom chatbox artwork.

### Drag resizing

Set a **Drag-Resize Modifier** and hold it while dragging a chat border.

Drag resizing also supports:

- live rewrapping;
- a configurable drag indicator color.

### Secondary chat size

Configure a second chat size and switch to it with a keybind.

The secondary size supports its own height/width changes and hold/toggle behavior.

> **[Example coming soon — Screenshot]** Normal, enlarged, reduced, and fixed-layout chat sizes.

> **[Example coming soon — Video]** Drag resizing, private-chat rewrap, and switching to the secondary chat size.

## Collapsible chat

The native chatbox can collapse to a single compact button.

Controls include:

- collapse/restore keybind;
- static button text or Report-button text;
- transparent button mode;
- configurable button and hover text;
- unread colors for public, private, friends chat, clan, and trade activity.

> **[Example coming soon — Screenshot]** Normal native chat and the single-button collapsed state.

> **[Example coming soon — Video]** Collapsing/restoring chat with the button and keybind.

## Native chat cleanup and presentation

Cleanup options work independently and can be enabled only where wanted.

### General cleanup

- remove the welcome message;
- wrapped-message indentation controls;
- hide the chat scrollbar;
- remove special-attack chat text;
- fixed-width timestamps with a reserved pixel column for consistent alignment.

### Channel color bar

Add an optional per-message channel color marker with configurable offset and width.

Separate colors can be configured for:

- no-channel messages;
- friends chat;
- clan;
- guest clan;
- Group Ironman.

### Clan cleanup

Controls include:

- remove clan startup messages;
- remove or replace the clan name;
- remove clan rank icons.

### Guest clan cleanup

Controls include:

- remove reconnecting messages;
- remove or replace the guest-clan name.

### Group Ironman cleanup

Controls include:

- remove or replace the Group Ironman name;
- move Group Ironman broadcasts.

### Friends Chat cleanup

Controls include:

- remove or replace the Friends Chat name;
- remove "Attempting to join" messages;
- remove "Now talking in" messages.

### Chat menu cleanup

**Remove Chat Options** simplifies right-click menus on chat messages.

An optional **Remove Lookup** setting can remove the player Lookup entry as well. Holding Control provides temporary access to the normal options.

> **[Example coming soon — Screenshot]** Before/after native chat with cleanup, fixed-width timestamps, channel color bars, and simplified channel labels.

> **[Example coming soon — Video]** Toggling cleanup options and showing the chat-menu Control bypass.

## Chatbox opacity

Native-chat opacity controls include:

- chatbox opacity;
- chat-button opacity;
- optional opacity for dialogue and menus shown in the chat area.

> **[Example coming soon — Screenshot]** Native chat at several opacity levels.

## Dialogue text styling

Customize supported RuneLite/OSRS dialogue text with:

- font family;
- font size;
- bold text;
- anti-aliasing;
- option-menu font size;
- line spacing;
- shadow color.

Styling can be enabled independently for:

- NPC dialogue;
- player dialogue;
- option menus;
- item/action dialogue.

> **[Example coming soon — Screenshot]** Default dialogue compared with customized dialogue text.

## Compatibility

### RuneLite Chat Color, Chat Filter, and Emojis

Improved Chat integrates with RuneLite's built-in chat systems rather than replacing them. Overlay rendering preserves supported emoji/icon tags, can use RuneLite Chat Color as a baseline, and can opt overlays into the RuneLite global Chat Filter path.

### Resource Packs

If a Resource Pack uses custom chatbox artwork, **Don't Draw Resize Borders** and **Don't Zoom Background** help prevent the resize system from framing or stretching that artwork.

### Modern Chat

Improved Chat can run alongside **Modern Chat** by **BenDol**. When Modern Chat owns conflicting native-chat geometry, visibility, or presentation, Improved Chat yields those conflicting native features while unrelated Improved Chat features remain available.

### Chat Widgets

Improved Chat declares a conflict with **Chat Widgets** because both plugins provide overlapping custom chat-overlay systems. Do not enable both at the same time.

## Focused alternatives and original projects

Improved Chat intentionally consolidates functionality that previously required several focused RuneLite plugins. It also contains BSD-2-Clause-derived work from several of those projects; full legal attribution is in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

If Improved Chat includes more than you need, the original focused plugins are still good choices for users who only want one feature:

| Plugin | Author | Best fit if you mainly want... |
| --- | --- | --- |
| [Chat Widgets](https://github.com/cnnnr/chat-widgets) | **Proven** | Custom game/private chat overlay widgets without the broader native-chat toolset. |
| [Chat Resizer / Better Resizable Chat](https://github.com/shanktank/better-resizable-chat) | **Shanktank** | Native chat resizing, private-chat rewrapping, drag resizing, and secondary chat sizes. |
| [Collapse Chat](https://github.com/stutify/runelite-plugins) | **stutify** | A focused way to collapse native chat to a single button. |
| [ChatboxOpacity](https://github.com/Trevor159/runelite-external-plugins) | **Trevor** | Native chatbox opacity control only. |
| [Dialogue Fonts](https://github.com/theOranguzang/osrs-dialogue-fonts-plugin) | **theOranguzang** | Dialogue font/readability customization only. |
| [Clean Chat](https://github.com/ldavid432/chat-cleanup) | **ldavid432** | Focused clan/friends/GIM channel cleanup and timestamp presentation. |
| [Remove Chat Options](https://github.com/96jonesa/remove-chat-options) | **McLovin1981 (Andrew Jones)** | Removing chatbox right-click options without the rest of Improved Chat. |
| [Offline Chat Icon](https://github.com/fatfingers23/RuneliteOfflineChatIcon) | **Bailey Townsend** | A focused offline clan-member indicator. |

### Compatible alternative: Modern Chat

[Modern Chat](https://github.com/BenDol/Modern-Chat) by **BenDol** is a broader modern-chat redesign rather than a plugin Improved Chat replaces. Improved Chat includes runtime safeguards so the two can coexist where their responsibilities do not overlap.

## Support

If something does not look or behave correctly, check whether another enabled plugin also changes RuneLite chat, chatbox geometry, opacity, gameframe artwork, or chat-message formatting.

When reporting a problem, include:

- the Improved Chat feature being used;
- fixed or resizable RuneLite layout;
- steps to reproduce;
- a screenshot or short video for visual problems;
- other enabled chat/gameframe plugins;
- whether Modern Chat or a Resource Pack is enabled.

Report issues at:

https://github.com/wukuna/improved-chat/issues

## License and attribution

Improved Chat is BSD 2-Clause licensed. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for license and attribution details.
