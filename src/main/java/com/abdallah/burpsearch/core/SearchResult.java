package com.abdallah.burpsearch.core;

import burp.api.montoya.http.message.HttpRequestResponse;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

public final class SearchResult {
    private final HttpRequestResponse requestResponse;
    private final String dedupeKey;
    private int matchCount;
    private final Set<SearchSource> sources;
    private final Instant timestamp;

    public SearchResult(HttpRequestResponse requestResponse, String dedupeKey, SearchSource source) {
        this.requestResponse = requestResponse;
        this.dedupeKey = dedupeKey;
        this.matchCount = 1;
        this.sources = EnumSet.of(source);
        this.timestamp = Instant.now();
    }

    public void incrementMatch(SearchSource source) {
        matchCount++;
        sources.add(source);
    }

    public HttpRequestResponse requestResponse() {
        return requestResponse;
    }

    public String dedupeKey() {
        return dedupeKey;
    }

    public int matchCount() {
        return matchCount;
    }

    public Set<SearchSource> sources() {
        return sources;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public String sourceDisplay() {
        if (sources.size() == 2) return "Both";
        return sources.iterator().next().getDisplayName();
    }
}
