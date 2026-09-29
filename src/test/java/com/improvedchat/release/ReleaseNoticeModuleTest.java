package com.improvedchat.release;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReleaseNoticeModuleTest
{
    @Test
    public void newReleaseStartsAtReminderOne()
    {
        assertEquals(1, ReleaseNoticeModule.nextReminderIndex(null, null));
        assertEquals(1, ReleaseNoticeModule.nextReminderIndex("older-release", "2"));
    }

    @Test
    public void currentReleaseAdvancesExactlyTwice()
    {
        String id = ReleaseNoticeModule.CURRENT_NOTICE_ID;

        assertEquals(1, ReleaseNoticeModule.nextReminderIndex(id, null));
        assertEquals(1, ReleaseNoticeModule.nextReminderIndex(id, "0"));
        assertEquals(2, ReleaseNoticeModule.nextReminderIndex(id, "1"));
        assertEquals(0, ReleaseNoticeModule.nextReminderIndex(id, "2"));
        assertEquals(0, ReleaseNoticeModule.nextReminderIndex(id, "3"));
    }

    @Test
    public void malformedStoredCountRecoversAtReminderOne()
    {
        String id = ReleaseNoticeModule.CURRENT_NOTICE_ID;

        assertEquals(1, ReleaseNoticeModule.nextReminderIndex(id, "not-a-number"));
        assertEquals(1, ReleaseNoticeModule.nextReminderIndex(id, "-5"));
    }

    @Test
    public void messagesUseRequestedReminderLabels()
    {
        String first = ReleaseNoticeModule.buildMessage(1);
        String second = ReleaseNoticeModule.buildMessage(2);

        assertTrue(first.startsWith("Improved Chat has been updated:"));
        assertTrue(first.endsWith("Reminder 1/2"));
        assertTrue(second.endsWith("Reminder 2/2"));
    }
}
