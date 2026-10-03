package com.improvedchat.chatbox.clean.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FormatterExtractorTest
{
    @Test
    public void variableWidthHourDoesNotConsumeSeparator()
    {
        FormatterExtractor.ExtractionResult template =
            FormatterExtractor.createFromFormatString("H:mm");
        FormatterExtractor.ExtractionResult result =
            FormatterExtractor.extractFromText(template, "9:05 message");

        assertNotNull(result);
        assertEquals("9:05", result.getFormattedOutput());
        assertEquals("9", result.getSegments().get(0).getValue());
        assertEquals("05", result.getSegments().get(1).getValue());
        assertEquals(" message", result.getRemainingText());
    }

    @Test
    public void variableWidthMonthKeepsFollowingLiteralAndToken()
    {
        FormatterExtractor.ExtractionResult template =
            FormatterExtractor.createFromFormatString("M/d HH:mm");
        FormatterExtractor.ExtractionResult result =
            FormatterExtractor.extractFromText(template, "1/2 03:04 hello");

        assertNotNull(result);
        assertEquals("1", result.getSegments().get(0).getValue());
        assertEquals("2", result.getSegments().get(1).getValue());
        assertEquals("03", result.getSegments().get(2).getValue());
        assertEquals("04", result.getSegments().get(3).getValue());
        assertEquals(" hello", result.getRemainingText());
    }
}
