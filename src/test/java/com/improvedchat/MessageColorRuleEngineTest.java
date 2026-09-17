package com.improvedchat;

import java.awt.Color;
import net.runelite.api.ChatMessageType;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MessageColorRuleEngineTest {
    private final ImprovedChatConfig config = new ImprovedChatConfig() {
        @Override
        public boolean enableMessageColorRules() {
            return true;
        }

        @Override
        public String messageColorRules() {
            return "expire::1::flash\nregex:^Boss .* down$::2";
        }

        @Override
        public boolean includePlayerChatInColorRules() {
            return false;
        }

        @Override
        public Color messageRuleColor1() {
            return Color.RED;
        }

        @Override
        public Color messageRuleColor2() {
            return Color.ORANGE;
        }
    };

    @After
    public void tearDown() {
        MessageColorRuleEngine.configure(null);
    }

    @Test
    public void literalRuleReturnsConfiguredColorAndFlash() {
        MessageColorRuleEngine.configure(config);
        assertEquals(Color.RED,
                MessageColorRuleEngine.colorFor("Your boost will expire soon", ChatMessageType.GAMEMESSAGE));
        assertTrue(MessageColorRuleEngine.flashFor(
                "Your boost will expire soon", ChatMessageType.GAMEMESSAGE));
    }

    @Test
    public void regexRuleIsCaseInsensitive() {
        MessageColorRuleEngine.configure(config);
        assertEquals(Color.ORANGE,
                MessageColorRuleEngine.colorFor("boss Vorkath down", ChatMessageType.GAMEMESSAGE));
        assertFalse(MessageColorRuleEngine.flashFor("boss Vorkath down", ChatMessageType.GAMEMESSAGE));
    }

    @Test
    public void playerChatIsExcludedByDefault() {
        MessageColorRuleEngine.configure(config);
        assertNull(MessageColorRuleEngine.colorFor("expire", ChatMessageType.PUBLICCHAT));
        assertFalse(MessageColorRuleEngine.flashFor("expire", ChatMessageType.PUBLICCHAT));
    }

    @Test
    public void plainTextDecodesRuneLiteEscapesAndTags() {
        String encoded = "<col=ff0000>Gzzz<at><at><at> <lt>ok<gt></col>";
        assertEquals("Gzzz@@@ <ok>", MessageColorRuleEngine.plainText(encoded));
        assertEquals("Gzzz@@@ <ok>", OverlayColorRuleEngine.plainText(encoded));
    }
}
