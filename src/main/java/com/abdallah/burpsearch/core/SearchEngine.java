package com.abdallah.burpsearch.core;

import burp.api.montoya.MontoyaApi;
import com.abdallah.burpsearch.ui.ResultsTableModel;
import com.abdallah.burpsearch.ui.StatusBar;
import com.abdallah.burpsearch.util.Cancellation;

import java.util.regex.PatternSyntaxException;

public final class SearchEngine {
    private final MontoyaApi api;
    private SearchWorker currentWorker;
    private final Cancellation cancellation = new Cancellation();
    private final ResultAggregator aggregator = new ResultAggregator();

    public SearchEngine(MontoyaApi api) {
        this.api = api;
    }

    /**
     * Starts a new search. Cancels any running search first.
     * Returns null on success, or an error message string if the query is invalid.
     */
    public String startSearch(
            SearchQuery query,
            ResultsTableModel tableModel,
            StatusBar statusBar,
            SearchDoneCallback onDone
    ) {
        Matcher matcher;
        try {
            matcher = new Matcher(query);
        } catch (PatternSyntaxException e) {
            return e.getMessage();
        }

        cancelCurrentSearch();

        aggregator.clear();
        tableModel.clear();
        cancellation.reset();

        statusBar.setSearching(0, 0, 0);

        currentWorker = new SearchWorker(
                api, query, cancellation, tableModel, aggregator, statusBar, onDone, matcher);
        currentWorker.execute();
        return null;
    }

    public void cancelCurrentSearch() {
        if (currentWorker != null && !currentWorker.isDone()) {
            cancellation.cancel();
            currentWorker.cancel(true);
            currentWorker = null;
        }
    }

    public ResultAggregator getAggregator() {
        return aggregator;
    }

    public void shutdown() {
        cancelCurrentSearch();
    }
}
