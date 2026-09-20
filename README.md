# Improved Chat

Improved Chat is a RuneLite Plugin Hub plugin for configurable chat overlays, native chatbox controls, message styling, cleanup, and accessibility features.

## Features

- Create multiple independent chat overlays and choose the message categories shown by each one.
- Use RuneLite's normal movable/resizable overlay placement, or attach an overlay above/below the local player.
- Configure width, padding, player-relative offsets, message count, fade time, dynamic height, timestamps, and input preview per overlay.
- Customize fonts, bold text, left/center/right text alignment, background colors, borders, and per-overlay text color overrides.
- Use **Message Color Rules** to recolor matching messages in both the RuneLite chatbox and Improved Chat overlays.
- Append `::flash` to a Message Color Rule to mark matching overlay messages for attention effects.
- Use **Overlay Rainbow Rules** for overlay-only rainbow rendering by word or visible character.
- Configure attention behavior for message text, border, and background flashes.
- Respect current RuneLite chat colors, filtering preferences, and emoji rendering.
- Collapse duplicate messages, preserve contextual colors, show channel names, and optionally hide RuneLite's default split private-chat widget.
- Optionally collapse RuneLite's native chat to a single customizable button, including hover text, transparency, and per-channel unread highlighting.
- Resize native chat in resizable and fixed layouts, including private-chat rewrapping, tab resizing, interface growth, dialog/interface reversion, drag-resizing, and a keybound secondary size.

## Resource Packs compatibility

Resizable Chat includes **Don't Draw Resize Borders** and **Don't Zoom Background** controls for custom chatbox artwork that should not be stretched or framed by Improved Chat.

Collapsible Chat uses RuneLite's native chat-tab states rather than installing replacement assets.

## Rule syntax

### Message Color Rules

One rule per line:

```text
is about to expire::1
has expired::1::flash
regex:^Your .* count is:.*::2
```

`::1` through `::9` select one of the nine configured rule colors. Prefix a rule with `regex:` to use a case-insensitive regular expression. Add `::flash` after the color number to trigger the overlay attention system for matching messages.

Player-authored chat is excluded from Message Color Rules by default and can be enabled in the RuneLite config panel.

### Overlay Rainbow Rules

One literal or `regex:` expression per line:

```text
achieved a new
has reached
regex:^Collection log.*
```

These rules affect Improved Chat overlays only.

## Development

The project follows the RuneLite external-plugin template and targets Java 11. RuneLite is resolved with `latest.release`.

Run the development client with:

```text
./gradlew run
```

Run unit tests with:

```text
./gradlew test
```

For Jagex-account login in the development client, follow RuneLite's **Using Jagex Accounts** development guide.

## Plugin Hub

`runelite-plugin.properties` uses `build=standard`; the plugin has no additional third-party runtime dependencies beyond RuneLite's dependency graph. The root `icon.png` is the Plugin Hub icon and `src/main/resources/panelicon.png` is used by the RuneLite sidebar button.

Improved Chat uses the independent RuneLite configuration group `improvedchat` and the Java package `com.improvedchat`.

This repository is BSD 2-Clause licensed. See `LICENSE` and `THIRD_PARTY_NOTICES.md` for attribution.
