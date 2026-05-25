package com.abdallah.burpsearch.core;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class Matcher {
    private final SearchQuery query;
    private final Pattern compiledPattern;
    private final String loweredExpression;

    public Matcher(SearchQuery query) throws PatternSyntaxException {
        this.query = query;
        SearchOptions opts = query.options();

        if (opts.regex()) {
            int flags = opts.caseSensitive() ? 0 : Pattern.CASE_INSENSITIVE;
            this.compiledPattern = Pattern.compile(query.expression(), flags);
            this.loweredExpression = null;
        } else {
            this.compiledPattern = null;
            this.loweredExpression = opts.caseSensitive() ? null : query.expression().toLowerCase();
        }
    }

    /** Returns true if the expression matches the given text. */
    public boolean matches(String text) {
        if (text == null) return false;
        SearchOptions opts = query.options();

        if (opts.regex()) {
            return compiledPattern.matcher(text).find();
        } else {
            if (opts.caseSensitive()) {
                return text.contains(query.expression());
            } else {
                return text.toLowerCase().contains(loweredExpression);
            }
        }
    }

    /**
     * Tests an item across all selected locations.
     * Returns true if the item is a "hit" (accounting for negative match).
     */
    public boolean test(burp.api.montoya.http.message.HttpRequestResponse item) {
        boolean anyLocationMatched = false;

        for (SearchLocation loc : query.options().locations()) {
            String text = LocationExtractor.extract(item, loc);
            if (text != null && matches(text)) {
                anyLocationMatched = true;
                break;
            }
        }

        return query.options().negativeMatch() ? !anyLocationMatched : anyLocationMatched;
    }
}
