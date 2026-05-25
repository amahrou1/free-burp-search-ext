package com.abdallah.burpsearch.core;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

public final class LocationExtractor {
    private LocationExtractor() {}

    public static String extract(HttpRequestResponse item, SearchLocation location) {
        HttpRequest request = item.request();
        HttpResponse response = item.response();

        return switch (location) {
            case REQ_URL -> request != null ? request.url() : "";
            case REQ_HEADERS -> request != null ? extractRequestHeaders(request) : "";
            case REQ_BODY -> request != null ? request.bodyToString() : "";
            case RESP_HEADERS -> response != null ? extractResponseHeaders(response) : null;
            case RESP_BODY -> response != null ? response.bodyToString() : null;
        };
    }

    private static String extractRequestHeaders(HttpRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.method()).append(" ").append(request.path());
        String httpVersion = request.httpVersion();
        if (httpVersion != null && !httpVersion.isBlank()) {
            sb.append(" ").append(httpVersion);
        }
        sb.append("\r\n");
        request.headers().forEach(h -> sb.append(h.name()).append(": ").append(h.value()).append("\r\n"));
        return sb.toString();
    }

    private static String extractResponseHeaders(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        String httpVersion = response.httpVersion();
        if (httpVersion != null && !httpVersion.isBlank()) {
            sb.append(httpVersion).append(" ");
        }
        sb.append(response.statusCode()).append(" ").append(response.reasonPhrase());
        sb.append("\r\n");
        response.headers().forEach(h -> sb.append(h.name()).append(": ").append(h.value()).append("\r\n"));
        return sb.toString();
    }
}
