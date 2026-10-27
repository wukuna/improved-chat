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
package com.improvedchat.chatbox.resize;

import com.improvedchat.ImprovedChatConfig;

import net.runelite.client.config.Keybind;
import lombok.Getter;
import javax.inject.Inject;
import javax.inject.Singleton;

// Which size set is live: the primary config values, or the secondary set while the swap keybind holds/toggles it on
@Singleton
public class SecondarySize {
    private final ImprovedChatConfig config;

    // Written on the client thread only, since the flip must share a pass with the chat rebuild; see setSwapActive
    @Getter private volatile boolean active; // Volatile for the AWT-side readers (drag capture)

    @Inject
    SecondarySize(ImprovedChatConfig config) {
        this.config = config;
    }

    // Returns whether the state changed, so the caller can refresh the chat exactly once per flip
    boolean setActive(boolean value) {
        if (value && config.secondaryKeybind().equals(Keybind.NOT_SET)) return false; // Unlikely, but just in case
        if (active == value) return false;
        active = value;
        return true;
    }

    void reset() {
        active = false;
    }

    // Size changes for the live set; the secondary height is shared by both layouts
    int effectiveWidthChange() { return active ? config.secondaryWidthChange() : config.widthChange(); }
    int effectiveHeightChange() { return active ? config.secondaryHeightChange() : config.heightChange(); }
    int effectiveFixedHeightChange() { return active ? config.secondaryHeightChange() : config.fixedHeightChange(); }
}