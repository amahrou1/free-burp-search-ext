package com.abdallah.burpsearch.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import com.abdallah.burpsearch.core.*;

import javax.swing.*;
import java.awt.*;

public final class SearchTab extends JPanel {
    private final MontoyaApi api;
    private final SearchEngine searchEngine;
    private final LiveUpdateHandler liveUpdateHandler;
    private final Registration liveHandlerRegistration;

    private final SearchControlPanel controlPanel;
    private final ResultsTablePanel tablePanel;
    private final PreviewPanel previewPanel;
    private final StatusBar statusBar;

    private String currentExpression = "";

    public SearchTab(MontoyaApi api) {
        this.api = api;
        this.searchEngine = new SearchEngine(api);
        this.liveUpdateHandler = new LiveUpdateHandler(api);

        setLayout(new BorderLayout());

        controlPanel = new SearchControlPanel();
        tablePanel = new ResultsTablePanel(api);
        previewPanel = new PreviewPanel(api);
        statusBar = new StatusBar();

        controlPanel.setSearchAction(this::runSearch);
        controlPanel.setCancelAction(this::cancelSearch);
        controlPanel.setDynamicUpdateChangeAction(this::onDynamicUpdateToggled);

        tablePanel.setSelectionListener(result ->
                previewPanel.show(result.requestResponse(), currentExpression));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, previewPanel);
        mainSplit.setResizeWeight(0.6);
        mainSplit.setDividerLocation(0.6);

        add(controlPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        liveHandlerRegistration = api.proxy().registerResponseHandler(liveUpdateHandler);
    }

    private void runSearch() {
        SearchQuery query = controlPanel.buildQuery();
        currentExpression = query.expression();

        controlPanel.setSearching(true);

        String error = searchEngine.startSearch(
                query,
                tablePanel.getModel(),
                statusBar,
                (results, scanned, elapsedMs) -> onSearchDone(query, results, scanned, elapsedMs)
        );

        if (error != null) {
            statusBar.setError(error);
            controlPanel.setSearching(false);
        }
    }

    private void onSearchDone(SearchQuery query, int results, int scanned, long elapsedMs) {
        controlPanel.setSearching(false);

        if (controlPanel.isDynamicUpdateEnabled()) {
            liveUpdateHandler.activate(
                    query,
                    tablePanel.getModel(),
                    searchEngine.getAggregator(),
                    () -> statusBar.setLiveDropped(liveUpdateHandler.getDroppedCount())
            );
            // Keep the "Done..." text and append · Live
            statusBar.setDoneLive(results, scanned, elapsedMs);
        }
    }

    private void cancelSearch() {
        searchEngine.cancelCurrentSearch();
        liveUpdateHandler.deactivate();
        controlPanel.setSearching(false);
        statusBar.setReady();
    }

    private void onDynamicUpdateToggled() {
        if (!controlPanel.isDynamicUpdateEnabled()) {
            liveUpdateHandler.deactivate();
            statusBar.setReady();
        }
    }

    public void onExtensionUnloaded() {
        searchEngine.shutdown();
        liveUpdateHandler.deactivate();
        if (liveHandlerRegistration != null) {
            liveHandlerRegistration.deregister();
        }
    }
}
