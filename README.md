# Improved Chat and Overlays

Keep the conversations and game messages you care about easy to read. Create multiple chat overlays with their own channels, filters, appearance and alerts, then adjust the native chatbox with resizing, collapse controls, opacity and message cleanup. Optional clan status, message colors and dialogue styling let you refine the details.

Use RuneLite's Improved Chat and Overlays settings for native chatbox features. Open the Improved Chat and Overlays side panel to create overlays and edit their Messages, Appearance and Notifications tabs. Most features are independent, so you can enable just the changes you want.

Previously **Improved Chat**. The name change keeps your existing settings and overlay positions. A one-time chatbox notice explains the rename after login.

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

## Settings layout and saved settings

The native plugin menu has five collapsible sections: **Chatbox**, **Chat cleanup**, **Overlay setup: side panel**, **Dialogue text**, and **Advanced**. Secondary sizing and its hotkey remain in Chatbox. Clan, guest-clan, friends-chat and GIM name controls remain independent of their instruction controls.

Use the side panel for individual overlays, custom text colors, RGB effects, notifications and Message highlights. Existing setting keys, overlay IDs, placements, filters and notification settings are retained. Former global color rules, their nine palette colors, RGB patterns, spread and fallback colors are copied into each existing overlay once. Old cycling remains full spectrum. The old overlay list is backed up under `overlayConfigsBeforeMenuCleanup`; legacy global values remain stored. New overlays have independent defaults. Unreadable configurations keep the existing recovery backup and are not overwritten merely by startup.

A one-time chatbox notice explains the new name and confirms that saved settings and overlay positions are retained. Collection Log is excluded from generic modal refitting and script replays; its native window handlers retain responsibility for resizing. The reported live Collection Log symptom still needs confirmation in-game.

## Custom overlays

Create and manage multiple overlays from the **Improved Chat and Overlays** side panel.

Start with a preset, then choose **Show overlay** and **Message Types** in the **Messages** tab. New custom overlays have no message types selected until you choose them. An overlay appears only when it has messages or an input preview; choosing “With chatbox open or hidden” does not draw an empty frame.

Use **Preview (10 seconds)** while logged in to check appearance without waiting for chat. Preview bypasses content filters and normal visibility conditions; its sample messages never enter chat history. Use **Open Plugin Settings** for native chat features and global defaults.

The compact overlay list stays above the editor. Select an overlay, then use **Messages**, **Appearance**, or **Notifications**. Each overlay has a direct enable switch. Changes save automatically as you edit. The editor supports duplicating an overlay, confirms deletion, preserves your scroll position when dependent controls change, and provides a background opacity percentage. Deleting every overlay stays empty after restart. Creating Game Alerts uses the same game-message selection as the first-run preset; existing saved selections are preserved.

**Clear [overlay] history** clears only that overlay's view. Native chat-tab history clearing still applies to all overlays showing those message types. **Collapse Duplicates** groups consecutive matching messages within each overlay's filtered view and keeps channels, senders, and incoming/outgoing messages separate. Captured history remains intact so clearing or filtering one overlay cannot remove another overlay's messages. History is held for the current plugin session.

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

### RGB animation (Beta)

In each overlay's **Appearance** tab, enable **Border** and **RGB Clockwise Border (Beta)** for a rainbow that travels around the perimeter. **Text effect** offers solid/channel colors, **RGB Static** by word or character, **RGB Cycling (Beta)**, and **RGB Wave (Beta)**. Cycling can fade between **Color 1** and **Color 2**, or use the full spectrum. Wave uses all colors. **RGB cycle (seconds)** sets the duration; higher values are slower. Border and text effects are independent and off by default.

Animation affects only the custom overlay, including its names, timestamps and input preview. Migrated overlays preserve their former precedence. **Messages → Message highlights → Highlights override defaults** lets a matching rule override the message body while unmatched messages retain the default effect. Icons retain their original colors. Wrapping, shadows, underline/strikethrough styling, message fading and attention flashes remain available. Border Color retains its transparency setting when RGB is enabled. Use **Preview (10 seconds)** while logged in to try the effects. These Beta effects still need live gameplay feedback.

>  Overlay list and overlay editor.
<img width="276" height="1305" alt="Screenshot 2026-10-03 185108" src="https://github.com/user-attachments/assets/39583041-3ec1-4c62-849f-cceadeef0761" />

>  Creating an overlay, selecting message types, moving it, and changing its appearance.

https://github.com/user-attachments/assets/95c6ba06-64a6-435d-8fb8-ad3857b9e662

## Overlay filtering

### Message replacer (Beta)

In an overlay's **Messages → Message replacer (Beta)** section, enable the feature and choose **Add replacement** for each message you want to customize. Each rule has **Original message**, **Show instead**, and its own enable checkbox. Edits save automatically. For example, match `Your thrall returns to the grave.` and show `You need to summon a thrall!`.

Rules match the complete message, ignoring case and chat colors. The first enabled, complete rule wins. Replacement text is displayed literally and never triggers another replacement. Blank or disabled rules leave messages unchanged. **Test replacements** previews the result without logging in or adding anything to chat.

Replacement affects only that overlay's displayed message body. The real chatbox, captured history, filters, message highlights and notification triggers still use the original message. Sender labels, timestamps, duplicate counts, wrapping and fading remain available. The feature is off by default and remains Beta pending live feedback.

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

The editor reports invalid filter line numbers as you edit and warns when Show Only Matches has no usable rules. Use the sample player-name and message fields to test custom content rules. Literal names match part of a name; use `regex:^Alice$` for an exact name. The tester does not test channel selection or visibility.

When the global override is active, custom controls are disabled but their saved values are retained. The editor also reports when RuneLite's Chat Filter plugin is disabled, in which case selecting the global filter does not apply content filtering.

Different overlays can therefore show different subsets of the same shared chat history without removing messages from the shared message pool.

## Clan display and offline status

Clan presentation can be configured separately for every overlay.

Per-overlay controls include:

- **Show Clan Rank Icons**
- **Show Offline Status**
- **Show Offline Icon**
- **Color Offline Names**
- **Offline Color**

Clan, guest-clan, and Group Ironman chat are supported.

Overlay offline presentation is independent of the native-chat **Grey offline clan names** setting, so one can be enabled without the other.

In native chat, **Grey offline clan names** greys out offline clan member names. **Show offline icon** optionally adds an icon beside those names; turning the icon off keeps the grey styling. Online names return to their original colors. Custom offline name colors remain available in each overlay.

## Message colors, rules, and alerts

Improved Chat and Overlays can use RuneLite Chat Color as its baseline color source or configured fallback colors for game, public, private, friends, clan, guest clan, Group Ironman, trade, challenge, broadcast, autochat, and other supported message types.

### Message Color Rules

Rules affect only the selected overlay. Open **Messages → Message highlights** to edit its definitions and nine-color palette. Native chat text uses RuneLite Chat Color.

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

RGB highlight patterns affect the selected overlay only. Choose Use overlay default, RGB Static (by word or character), RGB Cycling, or RGB Wave. Existing rainbow patterns retain their static style and word/character selection.

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

## Native chat resizing

Improved Chat and Overlays can resize RuneLite's native chatbox in both **resizable** and **fixed** layouts.

RuneLite keeps ownership of the chatbox position. A freely moved chatbox keeps its saved top-left position when its size changes; the default bottom-anchored chatbox still uses the native bottom anchor. Improved Chat and Overlays does not shift the saved location during resizing, logout/login or shutdown.

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

## Collapsible chat

The native chatbox can collapse to a single compact button.

Controls include:

- collapse/restore keybind;
- static button text or Report-button text;
- transparent button mode;
- configurable button and hover text;
- unread colors for public, private, friends chat, clan, and trade activity.

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

## Chatbox opacity

Native-chat opacity controls include:

- chatbox opacity;
- chat-button opacity;
- optional opacity for dialogue and menus shown in the chat area.

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

## Compatibility

### RuneLite Chat Color, Chat Filter, and Emojis

Improved Chat and Overlays integrates with RuneLite's built-in chat systems rather than replacing them. Overlay rendering preserves supported emoji/icon tags, can use RuneLite Chat Color as a baseline, and can opt overlays into the RuneLite global Chat Filter path.

### Resource Packs

If a Resource Pack uses custom chatbox artwork, **Don't Draw Resize Borders** and **Don't Zoom Background** help prevent the resize system from framing or stretching that artwork.

### Modern Chat

Improved Chat and Overlays can run alongside **Modern Chat** by **BenDol**. When Modern Chat owns conflicting native-chat geometry, visibility, or presentation, Improved Chat and Overlays yields those conflicting native features while unrelated Improved Chat and Overlays features remain available.

### Chat Widgets

Improved Chat and Overlays declares a conflict with **Chat Widgets** because both plugins provide overlapping custom chat-overlay systems. Do not enable both at the same time.

## Focused alternatives and original projects

Improved Chat and Overlays intentionally consolidates functionality that previously required several focused RuneLite plugins. It also contains BSD-2-Clause-derived work from several of those projects; full legal attribution is in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

If Improved Chat and Overlays includes more than you need, the original focused plugins are still good choices for users who only want one feature:

| Plugin | Author | Best fit if you mainly want... |
| --- | --- | --- |
| [Chat Widgets](https://github.com/cnnnr/chat-widgets) | **Proven** | Custom game/private chat overlay widgets without the broader native-chat toolset. |
| [Chat Resizer / Better Resizable Chat](https://github.com/shanktank/better-resizable-chat) | **Shanktank** | Native chat resizing, private-chat rewrapping, drag resizing, and secondary chat sizes. |
| [Collapse Chat](https://github.com/stutify/runelite-plugins) | **stutify** | A focused way to collapse native chat to a single button. |
| [ChatboxOpacity](https://github.com/Trevor159/runelite-external-plugins) | **Trevor** | Native chatbox opacity control only. |
| [Dialogue Fonts](https://github.com/theOranguzang/osrs-dialogue-fonts-plugin) | **theOranguzang** | Dialogue font/readability customization only. |
| [Clean Chat](https://github.com/ldavid432/chat-cleanup) | **ldavid432** | Focused clan/friends/GIM channel cleanup and timestamp presentation. |
| [Remove Chat Options](https://github.com/96jonesa/remove-chat-options) | **McLovin1981 (Andrew Jones)** | Removing chatbox right-click options without the rest of Improved Chat and Overlays. |
| [Offline Chat Icon](https://github.com/fatfingers23/RuneliteOfflineChatIcon) | **Bailey Townsend** | A focused offline clan-member indicator. |

### Compatible alternative: Modern Chat

[Modern Chat](https://github.com/BenDol/Modern-Chat) by **BenDol** is a broader modern-chat redesign rather than a plugin Improved Chat and Overlays replaces. Improved Chat and Overlays includes runtime safeguards so the two can coexist where their responsibilities do not overlap.

## Support

If something does not look or behave correctly, check whether another enabled plugin also changes RuneLite chat, chatbox geometry, opacity, gameframe artwork, or chat-message formatting.

When reporting a problem, include:

- the Improved Chat and Overlays feature being used;
- fixed or resizable RuneLite layout;
- steps to reproduce;
- a screenshot or short video for visual problems;
- other enabled chat/gameframe plugins;
- whether Modern Chat or a Resource Pack is enabled.

Report issues at:

https://github.com/wukuna/improved-chat/issues

## License and attribution

Improved Chat and Overlays is BSD 2-Clause licensed. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for license and attribution details.

## Per-overlay notifications

Each overlay has a **Notifications** section in its editor. Notifications start disabled. Enable them for all new matching messages or only messages matching a `::flash` color rule. Overlays disabled with **Show overlay**, filtered messages, your own messages and RuneLite console notifications do not trigger alerts. Notifications follow the Show overlay switch; an enabled overlay can still notify while its chatbox visibility condition temporarily hides it. Repainting, loading history and delayed message formatting do not replay notifications. Each enabled matching overlay sends its own alert.

Use RuneLite defaults, or enable **Customize Notification** for independent desktop notifications, Native/Custom/Off sound, volume, focus behavior, window flashing and color, timeout, and chatbox notification. Custom sounds use a WAV filename in RuneLite's notifications folder; blank uses its default custom sound. Playback and operating-system notifications are handled by RuneLite's shared Notifier, as with Idle Notifier. These controls are independent of the existing overlay Attention flashing.

Native private-chat hiding keeps the system-update timer visible. Overlay login/logout messages respect the game's Off setting. Game and clan payload prefixes are removed only for their native message types; literal pipes in player chat stay intact. Overlay text supports native underline, strike, shadow, line-break and quest-name template formatting.
