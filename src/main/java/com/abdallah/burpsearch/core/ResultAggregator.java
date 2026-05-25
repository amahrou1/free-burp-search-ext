package com.abdallah.burpsearch.core;

import burp.api.montoya.http.message.HttpRequestResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class ResultAggregator {
    private final LinkedHashMap<String, SearchResult> resultMap = new LinkedHashMap<>();

    public synchronized SearchResult add(HttpRequestResponse item, SearchSource source) {
        HttpRequestResponse req = item;
        String key = buildKey(req);
        SearchResult existing = resultMap.get(key);
        if (existing != null) {
            existing.incrementMatch(source);
            return null; // updated existing, no new row needed
        }
        SearchResult result = new SearchResult(item, key, source);
        resultMap.put(key, result);
        return result;
    }

    public synchronized List<SearchResult> snapshot() {
        return new ArrayList<>(resultMap.values());
    }

    public synchronized void clear() {
        resultMap.clear();
    }

    public synchronized int size() {
        return resultMap.size();
    }

    public static String buildKey(HttpRequestResponse item) {
        String method = item.request() != null ? item.request().method() : "";
        String url = item.request() != null ? item.request().url() : "";
        return method + " " + url;
    }

    public static String buildItemDedupeKey(HttpRequestResponse item) {
        String method = item.request() != null ? item.request().method() : "";
        String url = item.request() != null ? item.request().url() : "";
        int length = item.response() != null ? item.response().body().length() : -1;
        return method + " " + url + " " + length;
    }
}
