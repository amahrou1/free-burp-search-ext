package com.abdallah.burpsearch.core;

@FunctionalInterface
public interface SearchDoneCallback {
    void onDone(int uniqueResults, int scanned, long elapsedMs);
}
