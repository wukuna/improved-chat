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

import com.improvedchat.ImprovedChatConfig;

import com.improvedchat.ImprovedChatConfig.Revert;

// The single encoding of the size clamp policy: the per-axis overlay clamp both layouts' apply paths call,
// plus the range a size delta may be written in at all, which drag-resize has to enforce for itself
public final class SizeClamps {
    private SizeClamps() {}

    // Max height/width change (for config panel @Range only)
    public static final int MAX_DIMENSION_CHANGE = 10000;

    // Floors: clamp drag-resizer and config @Range mins
    public static final int MIN_HEIGHT_CHANGE = -ChatGeometry.CHATBOX_SLOT_H;
    public static final int MIN_WIDTH_CHANGE = -ChatGeometry.CHATBOX_SPRITE_W;

    // Ceilings: the bottom edge is pinned, so the chat may grow until its top reaches the top of the space it lives in
    public static int maxHeightChange(boolean fixedLayout, int canvasH) {
        return fixedLayout ? ChatGeometry.FIXED_CHAT_Y : Math.max(0, canvasH - ChatGeometry.CHATBOX_SLOT_H);
    }

    // Width is locked in fixed layout, so only the resizable ceiling exists
    public static int maxWidthChange(int canvasW) {
        return Math.max(0, canvasW - ChatGeometry.CHATBOX_SPRITE_W);
    }

    // Clamp a configured size delta while overlays are open, per that overlay's revert gate. The gates are read here
    // rather than passed in so every caller shares one encoding. Modal clamp is height-only: width passes false.
    public static int clamp(int delta, boolean fixedLayout, boolean modalClamps, boolean dialogOpen, ImprovedChatConfig config) {
        if (modalClamps) {
            Revert revert = config.revertForModals();
            // Fixed layout ungrows whatever the gate says: its modal slot is a hard size that can't
            // re-fit above a grown chat, so leaving the chat grown would only cover the modal up
            if (delta > 0 && (fixedLayout || revert.ungrows())) delta = 0; // Ungrow so the modal gets the band
            if (delta < 0 && revert.unshrinks()) delta = 0;
        }

        if (dialogOpen) {
            Revert revert = config.revertForDialogs();
            if (delta > 0 && revert.ungrows()) delta = 0;
            if (delta < 0 && revert.unshrinks()) delta = 0; // Unshrink so the dialog gets the full stock box
        }

        return delta;
    }
}