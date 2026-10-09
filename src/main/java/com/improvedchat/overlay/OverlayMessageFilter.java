package com.improvedchat.overlay;

import com.improvedchat.model.OverlayMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.runelite.client.util.Text;

/**
 * Cached per-overlay content filter. Rules are recompiled only when that overlay's filter settings
 * change, never once per render line.
 */
public final class OverlayMessageFilter
{
    private String wordsSnapshot = "";
    private String regexSnapshot = "";
    private String namesSnapshot = "";
    private List<Pattern> messagePatterns = new ArrayList<>();
    private List<Pattern> namePatterns = new ArrayList<>();

    public static boolean usesGlobalChatFilter(boolean globalOverride, OverlayConfig config)
    {
        return globalOverride || config.isUseGlobalChatFilter();
    }

    public boolean shouldExclude(OverlayConfig config, OverlayMessage message)
    {
        OverlayFilterMode mode = config.getFilterMode();
        if (mode == OverlayFilterMode.OFF)
        {
            return false;
        }

        refreshIfNeeded(config);
        boolean matched = matches(message);
        return mode == OverlayFilterMode.HIDE_MATCHES ? matched : !matched;
    }

    /** Editor feedback only; rendering still tolerates invalid saved expressions. */
    public List<String> validationMessages(OverlayConfig config)
    {
        List<String> errors = new ArrayList<>();
        validateLines(config.getFilteredRegex(), false, errors);
        validateLines(config.getFilteredNames(), true, errors);
        refreshIfNeeded(config);
        if (config.getFilterMode() == OverlayFilterMode.SHOW_ONLY_MATCHES
                && messagePatterns.isEmpty() && namePatterns.isEmpty())
        {
            errors.add("No usable rules: Show Only Matches will hide every message.");
        }
        return errors;
    }

    private static void validateLines(String text, boolean names, List<String> errors)
    {
        String[] lines = text.split("\\R", -1);
        for (int i = 0; i < lines.length; i++)
        {
            String expression = lines[i].trim();
            if (expression.isEmpty()) continue;
            if (names)
            {
                if (!expression.regionMatches(true, 0, "regex:", 0, 6)) continue;
                expression = expression.substring(6).trim();
            }
            try
            {
                if (expression.isEmpty()) throw new PatternSyntaxException("Empty expression", expression, 0);
                Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            }
            catch (PatternSyntaxException ex)
            {
                errors.add((names ? "Names" : "Regex") + " line " + (i + 1) + ": " + ex.getDescription());
            }
        }
    }

    private void refreshIfNeeded(OverlayConfig config)
    {
        String words = safe(config.getFilteredWords());
        String regex = safe(config.getFilteredRegex());
        String names = safe(config.getFilteredNames());
        if (Objects.equals(words, wordsSnapshot)
            && Objects.equals(regex, regexSnapshot)
            && Objects.equals(names, namesSnapshot))
        {
            return;
        }

        wordsSnapshot = words;
        regexSnapshot = regex;
        namesSnapshot = names;

        List<Pattern> compiledMessages = new ArrayList<>();
        for (String word : Text.fromCSV(words))
        {
            if (!word.trim().isEmpty())
            {
                compiledMessages.add(Pattern.compile(Pattern.quote(word.trim()), Pattern.CASE_INSENSITIVE));
            }
        }

        for (String line : regex.split("\\R"))
        {
            String trimmed = line.trim();
            if (!trimmed.isEmpty())
            {
                Pattern pattern = compile(trimmed);
                if (pattern != null)
                {
                    compiledMessages.add(pattern);
                }
            }
        }

        List<Pattern> compiledNames = new ArrayList<>();
        for (String line : names.split("\\R"))
        {
            String trimmed = line.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }

            Pattern pattern;
            if (trimmed.regionMatches(true, 0, "regex:", 0, 6))
            {
                pattern = compile(trimmed.substring(6).trim());
            }
            else
            {
                pattern = Pattern.compile(Pattern.quote(trimmed), Pattern.CASE_INSENSITIVE);
            }

            if (pattern != null)
            {
                compiledNames.add(pattern);
            }
        }

        messagePatterns = compiledMessages;
        namePatterns = compiledNames;
    }

    private boolean matches(OverlayMessage message)
    {
        String body = normalize(message.getMessage());
        for (Pattern pattern : messagePatterns)
        {
            if (pattern.matcher(body).find())
            {
                return true;
            }
        }

        String sender = normalize(message.getSender());
        if (!sender.isEmpty())
        {
            for (Pattern pattern : namePatterns)
            {
                if (pattern.matcher(sender).find())
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static String normalize(String value)
    {
        if (value == null)
        {
            return "";
        }
        return Text.removeTags(value)
            .replace('\u00A0', ' ')
            .replace("<lt>", "<")
            .replace("<gt>", ">")
            .replace("<at>", "@");
    }

    private static Pattern compile(String expression)
    {
        if (expression.isEmpty())
        {
            return null;
        }

        try
        {
            return Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        }
        catch (PatternSyntaxException ex)
        {
            return null;
        }
    }

    private static String safe(String value)
    {
        return value == null ? "" : value;
    }
}
