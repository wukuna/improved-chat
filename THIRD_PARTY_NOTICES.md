# Third-party notices

Improved Chat is derived in part from the open-source **Chat Widgets** RuneLite plugin:

- Repository: `https://github.com/cnnnr/chat-widgets`
- Original copyright: Copyright (c) 2021, 117
- License: BSD 2-Clause

The original BSD 2-Clause notice and disclaimer are retained in `LICENSE`.

Improved Chat also interoperates with RuneLite's built-in Chat Color, Chat Filter, and Emojis functionality through the public RuneLite client API.

The `text::1` through `text::9` rule syntax is intentionally compatible with the familiar rule style used by Force Recolor. Improved Chat's rule engine and overlay behavior are implemented as part of this project.


## Collapse Chat

Improved Chat's single-button native chat collapse behavior is derived from the BSD-2-Clause **Collapse Chat** plugin:

- Repository: `https://github.com/stutify/runelite-plugins`
- Copyright: Copyright (c) 2025, stutify
- License: BSD 2-Clause

The implementation is integrated into Improved Chat's own configuration and lifecycle and adds compatibility with the consolidated resize and modernization systems.

## Chat Resizer

Improved Chat's advanced native chat resizing module is derived from the BSD-2-Clause **Chat Resizer / Better Resizable Chat** plugin:

- Repository: `https://github.com/shanktank/better-resizable-chat`
- Copyright: Copyright (c) 2026, shanktank
- License: BSD 2-Clause

The integrated module retains resizable/fixed layout sizing, private-chat rewrapping, tab resizing, interface growth, dialog/interface reversion, drag resizing, secondary sizes, and compatibility controls.

## Modern Chat design reference

The optional Modernize Chat presentation was designed independently for Improved Chat after reviewing the UX of **Modern Chat**:

- Repository: `https://github.com/BenDol/Modern-Chat`
- Copyright: Copyright 2023 BenDol
- License: BSD 2-Clause

Improved Chat does not import Modern Chat's feature framework; the modernization layer styles RuneLite's native chatbox while preserving native behavior.


## Clean Chat

Improved Chat's optional native-chat cleanup module is derived from the BSD-2-Clause **Clean Chat** plugin:

- Repository: `https://github.com/ldavid432/chat-cleanup`
- Copyright: Copyright (c) 2025, ldavid432, with additional notices retained in derived source
- License: BSD 2-Clause

The integrated module retains channel-name cleanup, startup-message filtering, indentation, scrollbar control, fixed-width timestamp support, channel color bars, and clan/friends/GIM cleanup behavior.

## Chatbox Opacity

Improved Chat's optional native transparent-chat opacity controls are derived from **Chatbox Opacity**:

- Repository: `https://github.com/Trevor159/runelite-chatbox-opacity`
- Copyright: Copyright (c) 2019, Trevor
- License: BSD 2-Clause

The implementation is adapted to coexist with Improved Chat's Modernize Chat surfaces and restores native widget state when disabled.

## Remove Chat Options

Improved Chat's optional chat context-menu cleanup is derived from **Remove Chat Options**:

- Repository: `https://github.com/96jonesa/remove-chat-options`
- Copyright: Copyright (c) 2025, Andrew Jones
- License: BSD 2-Clause

The Control-key bypass is retained.

## Offline Chat Icon

Improved Chat's optional offline-clan status formatting is derived from **Offline Chat Icon**:

- Repository: `https://github.com/fatfingers23/RuneliteOfflineChatIcon`
- Copyright: Copyright (c) 2022, Bailey Townsend
- License: BSD 2-Clause

Improved Chat draws its own status glyph instead of copying the standalone plugin's image asset and preserves each message node's original name for restoration.

## Dialogue Fonts

Improved Chat's optional dialogue-font replacement is derived from **Dialogue Fonts**:

- Repository: `https://github.com/theOranguzang/osrs-dialogue-fonts-plugin`
- Copyright: Copyright (c) 2026, theOranguzang
- License: BSD 2-Clause

NPC, player, option-menu, and item/action dialogue replacement behavior is integrated under Improved Chat's lifecycle and configuration. Improved Chat additionally adds separate option sizing, line spacing, optional shadow styling, and safer live restoration when dialogue-type toggles change.
