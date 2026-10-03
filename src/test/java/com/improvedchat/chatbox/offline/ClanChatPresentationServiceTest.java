package com.improvedchat.chatbox.offline;

import java.awt.image.BufferedImage;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ClanChatPresentationServiceTest
{
    @Test
    public void offlineMarkerUsesOnlyFullyOpaqueVisiblePixels()
    {
        BufferedImage image = ClanChatPresentationService.createOfflineIconImage();

        int visible = 0;
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int alpha = (image.getRGB(x, y) >>> 24) & 0xff;
                if (alpha != 0)
                {
                    visible++;
                    assertTrue("visible marker pixels must survive RuneLite indexed-sprite conversion",
                        alpha == 255);
                }
            }
        }

        assertTrue("offline marker must contain visible pixels", visible > 0);
    }
}
