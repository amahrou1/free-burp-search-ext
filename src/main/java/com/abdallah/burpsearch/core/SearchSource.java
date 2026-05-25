package com.abdallah.burpsearch.core;

public enum SearchSource {
    PROXY_HISTORY("Proxy"),
    SITE_MAP("Site map");

    private final String displayName;

    SearchSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
