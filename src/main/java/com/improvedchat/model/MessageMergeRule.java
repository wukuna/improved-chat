package com.improvedchat.model;

import java.util.regex.Pattern;

public final class MessageMergeRule {
    private final String previousPrefix;
    private final String nextPattern;
    private final boolean exactMatch;
    private final Pattern regexPattern;

    public MessageMergeRule(String previousPrefix, String nextPattern, boolean exactMatch) {
        this.previousPrefix = previousPrefix;
        this.nextPattern = nextPattern;
        this.exactMatch = exactMatch;
        this.regexPattern = null;
    }

    public MessageMergeRule(String previousPrefix, Pattern regexPattern) {
        this.previousPrefix = previousPrefix;
        this.nextPattern = null;
        this.exactMatch = false;
        this.regexPattern = regexPattern;
    }

    public boolean matches(String previous, String next) {
        if (!previous.startsWith(previousPrefix)) {
            return false;
        }
        if (regexPattern != null) {
            return regexPattern.matcher(next).matches();
        }
        return exactMatch ? next.equals(nextPattern) : next.startsWith(nextPattern);
    }

    public String merge(String previous, String next) {
        return previous + " " + next;
    }

    public boolean matchesPreviousPrefix(String message) {
        return message.startsWith(previousPrefix);
    }
}
