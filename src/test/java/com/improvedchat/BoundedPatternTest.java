package com.improvedchat;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;

public class BoundedPatternTest
{
    @Test public void preservesOrdinaryJavaRegexFeatures()
    {
        String[] expressions = {"(?i)dragon.*drop", "(?<=item: )\\w+", "(foo) \\1", "^\\p{L}+$", "a{2,4}", "(?s)a.b"};
        String[] messages = {"Dragon platebody drop", "item: shark", "foo foo", "RuneScape", "baaa", "a\nb", "nothing"};
        for (String regex : expressions) for (String text : messages)
        {
            Pattern pattern = BoundedPattern.compile(regex, 0);
            assertEquals(regex + " / " + text, pattern.matcher(text).find(), BoundedPattern.find(pattern, text));
        }
    }

    @Test(timeout=3000) public void pathologicalRulesAreStoppedAndStayDisabled()
    {
        Pattern pattern = BoundedPattern.compile("((a|aa)+)+$", 0);
        assertFalse(BoundedPattern.find(pattern, "a".repeat(32) + "!"));
        assertTrue(BoundedPattern.disabled(pattern));
        assertFalse(BoundedPattern.find(pattern, "a"));
        assertNotNull(BoundedPattern.warning());
        assertTrue(BoundedPattern.find(BoundedPattern.compile("a+$", 0), "aaaa"));
    }

    @Test(expected=PatternSyntaxException.class) public void oversizedExpressionIsRejected()
    { BoundedPattern.compile("a".repeat(1025), 0); }

    @Test public void ruleInputIsBoundedWithoutTruncatingStoredText()
    {
        String source = "a".repeat(BoundedPattern.MAX_SOURCE + 1);
        assertEquals("", BoundedPattern.source(source));
        assertEquals(BoundedPattern.MAX_SOURCE + 1, source.length());
        assertEquals(BoundedPattern.MAX_RULES, BoundedPattern.lines("rule\n".repeat(200)).length);
    }
}
