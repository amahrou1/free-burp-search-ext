package com.abdallah.burpsearch.core;

public enum SearchLocation {
    REQ_URL("Request URL"),
    REQ_HEADERS("Request Headers"),
    REQ_BODY("Request Body"),
    RESP_HEADERS("Response Headers"),
    RESP_BODY("Response Body");

    private final String displayName;

    SearchLocation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
