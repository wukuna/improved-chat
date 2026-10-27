/*
 * Adapted portions from Chat Resizer by shanktank.
 *
 * BSD 2-Clause License
 * 
 * Copyright (c) 2026, shanktank
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/* Derived from Chat Resizer by shanktank under BSD 2-Clause. See THIRD_PARTY_NOTICES.md. */
package com.improvedchat.chatbox.resize.internal;

// Raw client script IDs, args, sentinels, etc, that lack an associated RuneLite constant
public final class RawScripts {
    private RawScripts() {}

    // cs2 [proc,on_mobile]: a helper the layout scripts call mid-refit, so its pre-fire is a relayout tripwire
    public static final int TOPLEVEL_RELAYOUT = 1972;

    // Re-wraps chat lines and cleans up the background sprite at the current width, resizable layout only
    public static final int RESIZES_CHAT = 924;

    // Re-wraps chat text at the current width
    public static final int REWRAPS_CHAT = 663;

    // onResize handler of a toplevel CONTROL widget: full in-engine UI re-fit without a real canvas resize
    public static final int TOPLEVEL_ONRESIZE = 904;

    // Component-layout enums (cs2 toplevel_getcomponents) TOPLEVEL_ONRESIZE expects per toplevel
    public static final int LAYOUT_ENUM_FIXED = 1129; // Toplevel (548)
    public static final int LAYOUT_ENUM_OSRS_STRETCH = 1130; // ToplevelOsrsStretch (161)
    public static final int LAYOUT_ENUM_PRE_EOC = 1131; // ToplevelPreEoc (164)

    // Ontimer wrappers of self-refitting windows' size polls
    public static final int BANKMAIN_SIZE_CHECK_TIMER = 839; // cs2 bankmain_size_check
    public static final int SETTINGS_SIZE_CHECK_TIMER = 3831; // cs2 settings_window_resize
    // Collection Log refitting is intentionally left to its native window handlers.
    public static final int SEED_VAULT_SIZE_CHECK_TIMER = 742; // Ontimer wrapper of cs2 2852

    // cs2 chat_button_onop, the tab stones' click handler; (1, tabIdx) switches tab or collapses chat if already active
    public static final int CHAT_TAB_CLICKED = 175;

    // Repaints the tab stones from varc CHAT_VIEW; run after parking the collapse sentinel so open tab draws unselected
    public static final int REDRAW_CHAT_BUTTONS = 178;

    // Not a script: varc CHAT_VIEW sentinel the engine parks on while resizable chat is collapsed
    public static final int COLLAPSED_TAB = 1337;
}
