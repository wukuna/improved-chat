# Improved Chat

Improved Chat combines customizable chat overlays with native RuneLite chat controls, cleanup tools, and message styling in one plugin.

## What it does

- **Custom chat overlays** — Create multiple independent overlays, choose what message types each one shows, place them freely or above/below your character, and customize their size, font, alignment, background, borders, timestamps, fade behavior, and more.
- **Native chat controls** — Resize RuneLite chat in fixed or resizable mode, drag-resize it, switch to a secondary chat size with a keybind, collapse the chatbox to a compact button, and control chatbox/button opacity.
- **Message highlighting** — Recolor important messages with simple text or regex rules, trigger attention flashes, and apply overlay-only rainbow effects.
- **Chat cleanup** — Remove unwanted native chat labels/messages, simplify chat context menus, improve timestamp/channel presentation, and optionally show offline clan-member status.
- **Dialogue styling** — Customize supported NPC, player, option-menu, and item/action dialogue text.
- **Compatibility** — Works with RuneLite Chat Color, Chat Filter, and Emojis. Improved Chat also includes safeguards for Resource Packs and the standalone Modern Chat plugin.

## Overlay setup

Open the **Improved Chat** sidebar panel to create and edit overlays.

Use **Message Types** to choose what belongs in each overlay. Overlays can be positioned through RuneLite's normal overlay system or attached above/below your character.

<!-- IMAGE PLACEHOLDER: Show the Improved Chat overlay editor with Message Types and positioning controls. -->

<!-- VIDEO PLACEHOLDER: Short clip showing how to create, position, resize, and configure an overlay. -->

## Resizing and collapsing chat

Under **Resizable Chat**, you can change the native chatbox size in both fixed and resizable layouts.

For drag resizing, set a **Drag-Resize Modifier** and hold that key while dragging a chat border. A separate **Secondary Chat Size** can be configured and activated with its own keybind.

**Collapsible Chat** reduces the native chatbox to a compact button and can also be controlled with the **Collapse Chat Keybind**.

<!-- IMAGE PLACEHOLDER: Show normal, resized, and collapsed native chat states. -->

<!-- VIDEO PLACEHOLDER: Demonstrate drag resizing, secondary size switching, and collapse/restore. -->

## Message rules

### Message Color Rules

Use one rule per line:

```text
is about to expire::1
has expired::1::flash
regex:^Your .* count is:.*::2
```

- `::1` through `::9` select one of the configured rule colors.
- Prefix a rule with `regex:` to use a case-insensitive regular expression.
- Add `::flash` to trigger the overlay attention system when that message appears.
- Player-authored chat is excluded by default and can be enabled separately.

### Overlay Rainbow Rules

Rainbow rules affect Improved Chat overlays only:

```text
achieved a new
has reached
regex:^Collection log.*
```

<!-- IMAGE PLACEHOLDER: Show Message Color Rules and Overlay Rainbow Rules with an in-game result. -->

<!-- VIDEO PLACEHOLDER: Demonstrate creating a color/flash rule and seeing it trigger in chat. -->

## Chat cleanup and presentation

The cleanup sections can simplify RuneLite's native chat without replacing it. Available controls include:

- startup and channel-label cleanup;
- wrapped-message indentation;
- scrollbar and special-attack text cleanup;
- fixed-width timestamps;
- optional channel color bars;
- clan, guest clan, Group Ironman, and friends-chat cleanup;
- simplified chat-message context menus;
- optional offline clan-member icon/name styling.

Most cleanup options are independent, so you can enable only the pieces you want.

<!-- IMAGE PLACEHOLDER: Before/after screenshot of native chat with selected cleanup options enabled. -->

## Resource Packs and Modern Chat

If a Resource Pack uses custom chatbox artwork, **Don't Draw Resize Borders** and **Don't Zoom Background** can prevent Improved Chat's resize system from stretching or framing that artwork.

Improved Chat can also run alongside the standalone **Modern Chat** plugin. When Modern Chat's redesign takes control of native chat geometry or visibility, Improved Chat automatically yields the conflicting native-chat features while leaving unrelated Improved Chat features available.

<!-- IMAGE PLACEHOLDER: Example Resource Pack compatibility settings or Modern Chat coexistence. -->

## Compatibility notes

- **Chat Widgets:** Improved Chat declares a conflict with Chat Widgets because both plugins provide overlapping chat-overlay systems.
- **Modern Chat:** Supported as a separate standalone plugin through runtime ownership safeguards.
- **RuneLite Chat Filter / Chat Color / Emojis:** Improved Chat integrates with these RuneLite features rather than replacing them.

## Support

If something does not look or behave correctly, first check whether another enabled plugin also modifies RuneLite chat, chatbox geometry, opacity, or gameframe artwork.

When reporting a problem, please include:

- what feature you were using;
- fixed or resizable RuneLite layout;
- steps to reproduce the issue;
- a screenshot or short video if the problem is visual;
- any other enabled chat/gameframe plugins;
- whether Modern Chat or a Resource Pack is enabled.

Report issues here:

https://github.com/wukuna/improved-chat/issues

Improved Chat is BSD 2-Clause licensed. See `LICENSE` and `THIRD_PARTY_NOTICES.md` for attribution.
