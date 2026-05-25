package com.abdallah.burpsearch.core;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import com.abdallah.burpsearch.ui.ResultsTableModel;
import com.abdallah.burpsearch.ui.StatusBar;
import com.abdallah.burpsearch.util.Cancellation;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SearchWorker extends SwingWorker<Void, SearchResult> {
    private static final int BATCH_SIZE = 100;

    private final MontoyaApi api;
    private final SearchQuery query;
    private final Cancellation cancellation;
    private final ResultsTableModel tableModel;
    private final ResultAggregator aggregator;
    private final StatusBar statusBar;
    private final Runnable onDone;
    private final Matcher matcher;

    private int totalItems = 0;
    private int scanned = 0;
    private long startTime;

    public SearchWorker(
            MontoyaApi api,
            SearchQuery query,
            Cancellation cancellation,
            ResultsTableModel tableModel,
            ResultAggregator aggregator,
            StatusBar statusBar,
            Runnable onDone,
            Matcher matcher
    ) {
        this.api = api;
        this.query = query;
        this.cancellation = cancellation;
        this.tableModel = tableModel;
        this.aggregator = aggregator;
        this.statusBar = statusBar;
        this.onDone = onDone;
        this.matcher = matcher;
    }

    private record TaggedItem(HttpRequestResponse item, SearchSource source) {}

    @Override
    protected Void doInBackground() {
        startTime = System.currentTimeMillis();

        List<TaggedItem> items = new ArrayList<>();
        Set<String> seenItemKeys = new HashSet<>();

        if (query.options().sources().contains(SearchSource.PROXY_HISTORY)) {
            for (ProxyHttpRequestResponse proxy : api.proxy().history()) {
                HttpRequestResponse item = HttpRequestResponse.httpRequestResponse(
                        proxy.request(), proxy.response());
                String key = ResultAggregator.buildItemDedupeKey(item);
                if (seenItemKeys.add(key)) {
                    items.add(new TaggedItem(item, SearchSource.PROXY_HISTORY));
                }
            }
        }

        if (query.options().sources().contains(SearchSource.SITE_MAP)) {
            for (HttpRequestResponse item : api.siteMap().requestResponses()) {
                String key = ResultAggregator.buildItemDedupeKey(item);
                if (seenItemKeys.add(key)) {
                    items.add(new TaggedItem(item, SearchSource.SITE_MAP));
                }
            }
        }

        totalItems = items.size();

        for (TaggedItem tagged : items) {
            if (cancellation.isCancelled() || isCancelled()) break;

            scanned++;
            HttpRequestResponse item = tagged.item();

            if (query.options().inScopeOnly() && item.request() != null) {
                if (!api.scope().isInScope(item.request().url())) continue;
            }

            if (!matcher.test(item)) continue;

            SearchResult result = aggregator.add(item, tagged.source());
            if (result != null) {
                publish(result);
            }

            if (scanned % 250 == 0) {
                final int s = scanned;
                final int t = totalItems;
                final int r = aggregator.size();
                SwingUtilities.invokeLater(() -> statusBar.setSearching(s, t, r));
            }
        }

        return null;
    }

    @Override
    protected void process(List<SearchResult> chunks) {
        int i = 0;
        while (i < chunks.size()) {
            int end = Math.min(i + BATCH_SIZE, chunks.size());
            chunks.subList(i, end).forEach(tableModel::addResult);
            i = end;
        }
        statusBar.setSearching(scanned, totalItems, aggregator.size());
    }

    @Override
    protected void done() {
        long elapsed = System.currentTimeMillis() - startTime;
        statusBar.setDone(aggregator.size(), scanned, elapsed);
        if (onDone != null) onDone.run();
    }
}
