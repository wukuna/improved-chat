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

import javax.inject.Inject;
import javax.inject.Singleton;

// Overlay edge detectors for chat dialog and toplevel modal, all consumers poll through here to ensure simultaneity
@Singleton
public class OverlayTransitions {
    private final ChatDialogBoxes dialogBoxes;
    private final TopLevelModals mainModals;

    @Inject
    OverlayTransitions(ChatDialogBoxes dialogBoxes, TopLevelModals mainModals) {
        this.dialogBoxes = dialogBoxes;
        this.mainModals = mainModals;
    }

    // Re-prime both caches on enable; the singletons survive disable -> enable
    void reset() {
        dialogBoxes.reset();
        mainModals.reset();
    }

    // Cycle-end route (PostClientTick): advance and report both edges in one call, so neither can defer the other
    Edges poll() {
        boolean dialogChanged = dialogBoxes.dialogOpenStateChanged();
        boolean modalChanged = mainModals.topLevelModalOpenStateChanged();
        return new Edges(dialogChanged, modalChanged);
    }

    // Fixed pre-apply route: consume only the modal edge, so the cycle-end poll won't double-fire on it.
    // The dialog cache is deliberately left for the next poll() rather than dropped.
    boolean pollModalEdge() {
        return mainModals.topLevelModalOpenStateChanged();
    }

    static final class Edges {
        final boolean dialogChanged;
        final boolean modalChanged;

        Edges(boolean dialogChanged, boolean modalChanged) {
            this.dialogChanged = dialogChanged;
            this.modalChanged = modalChanged;
        }
    }
}