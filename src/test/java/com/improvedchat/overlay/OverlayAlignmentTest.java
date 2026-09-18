package com.improvedchat.overlay;

import com.improvedchat.model.PlacementMode;
import com.improvedchat.model.TextAlignment;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OverlayAlignmentTest {
    @Test
    public void calculatesLeftCenterAndRightPositions() {
        assertEquals(4, DynamicChatOverlay.calculateAlignedX(TextAlignment.LEFT, 4, 100, 40));
        assertEquals(34, DynamicChatOverlay.calculateAlignedX(TextAlignment.CENTER, 4, 100, 40));
        assertEquals(64, DynamicChatOverlay.calculateAlignedX(TextAlignment.RIGHT, 4, 100, 40));
    }

    @Test
    public void clampsAlignmentWhenLineExceedsContentWidth() {
        assertEquals(4, DynamicChatOverlay.calculateAlignedX(TextAlignment.LEFT, 4, 100, 140));
        assertEquals(4, DynamicChatOverlay.calculateAlignedX(TextAlignment.CENTER, 4, 100, 140));
        assertEquals(4, DynamicChatOverlay.calculateAlignedX(TextAlignment.RIGHT, 4, 100, 140));
    }

    @Test
    public void preservesLegacyPlacementDefaultsUntilAlignmentIsExplicitlySet() {
        OverlayConfig config = new OverlayConfig();
        assertEquals(TextAlignment.LEFT, config.getTextAlignment());

        config.setPlacementMode(PlacementMode.ABOVE_PLAYER);
        assertEquals(TextAlignment.CENTER, config.getTextAlignment());

        config.setTextAlignment(TextAlignment.RIGHT);
        config.setPlacementMode(PlacementMode.FREE);
        assertEquals(TextAlignment.RIGHT, config.getTextAlignment());
    }
}
