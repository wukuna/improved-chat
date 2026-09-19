/* Derived from Collapse Chat by stutify under BSD 2-Clause. See THIRD_PARTY_NOTICES.md. */
package com.improvedchat.chatbox.collapse;

public class ChatState {
    public ChatCollapseState collapseState = ChatCollapseState.UNKNOWN;
    public boolean isMouseOverAllButton = false;
    public boolean hasUnseenMessages = false;
    ChatButton selectedChatButton = null;

    public void reset() {
        this.collapseState = ChatCollapseState.UNKNOWN;
        this.isMouseOverAllButton = false;
        this.hasUnseenMessages = false;
        this.selectedChatButton = null;
    }

    public boolean isCollapsed() {
        return collapseState == ChatCollapseState.COLLAPSED;
    }
}