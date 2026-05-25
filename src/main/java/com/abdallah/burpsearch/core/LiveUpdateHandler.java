package com.abdallah.burpsearch.core;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;
import burp.api.montoya.MontoyaApi;
import com.abdallah.burpsearch.ui.ResultsTableModel;

import javax.swing.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public final class LiveUpdateHandler implements ProxyResponseHandler {
    private static final int QUEUE_CAPACITY = 1000;

    private final MontoyaApi api;
    private volatile SearchQuery activeQuery;
    private volatile ResultsTableModel tableModel;
    private volatile ResultAggregator aggregator;
    private volatile Runnable droppedCallback;

    private final LinkedBlockingDeque<HttpRequestResponse> queue = new LinkedBlockingDeque<>(QUEUE_CAPACITY);
    private final AtomicInteger droppedCount = new AtomicInteger(0);
    private volatile boolean active = false;
    private Thread processingThread;

    public LiveUpdateHandler(MontoyaApi api) {
        this.api = api;
    }

    public void activate(SearchQuery query, ResultsTableModel model, ResultAggregator aggregator, Runnable onDropped) {
        this.activeQuery = query;
        this.tableModel = model;
        this.aggregator = aggregator;
        this.droppedCallback = onDropped;
        this.droppedCount.set(0);
        queue.clear();
        this.active = true;

        processingThread = new Thread(this::processLoop, "burp-search-live-update");
        processingThread.setDaemon(true);
        processingThread.start();
    }

    public void deactivate() {
        active = false;
        if (processingThread != null) {
            processingThread.interrupt();
            processingThread = null;
        }
        queue.clear();
    }

    public int getDroppedCount() {
        return droppedCount.get();
    }

    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        if (active && activeQuery != null) {
            HttpRequestResponse item = HttpRequestResponse.httpRequestResponse(
                    interceptedResponse.initiatingRequest(),
                    interceptedResponse
            );
            enqueue(item);
        }
        return ProxyResponseReceivedAction.continueWith(interceptedResponse);
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
    }

    private void enqueue(HttpRequestResponse item) {
        boolean offered = queue.offer(item);
        if (!offered) {
            queue.poll();
            queue.offer(item);
            droppedCount.incrementAndGet();
            if (droppedCallback != null) {
                SwingUtilities.invokeLater(droppedCallback);
            }
        }
    }

    private void processLoop() {
        while (active && !Thread.currentThread().isInterrupted()) {
            try {
                HttpRequestResponse item = queue.take();
                if (!active) break;

                SearchQuery query = activeQuery;
                ResultsTableModel model = tableModel;
                ResultAggregator agg = aggregator;
                if (query == null || model == null || agg == null) continue;

                if (query.options().inScopeOnly() && item.request() != null) {
                    if (!api.scope().isInScope(item.request().url())) continue;
                }

                Matcher matcher;
                try {
                    matcher = new Matcher(query);
                } catch (java.util.regex.PatternSyntaxException e) {
                    continue;
                }

                if (!matcher.test(item)) continue;

                SearchResult result = agg.add(item, SearchSource.PROXY_HISTORY);
                if (result != null) {
                    SwingUtilities.invokeLater(() -> model.addResult(result));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
