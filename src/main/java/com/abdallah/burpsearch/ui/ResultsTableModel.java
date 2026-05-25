package com.abdallah.burpsearch.ui;

import com.abdallah.burpsearch.core.SearchResult;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;

import javax.swing.table.AbstractTableModel;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ResultsTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"#", "Host", "Method", "URL", "Status", "Length", "Time", "Source", "Matches"};
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final List<SearchResult> rows = new ArrayList<>();

    public void addResult(SearchResult result) {
        int idx = rows.size();
        rows.add(result);
        fireTableRowsInserted(idx, idx);
    }

    public void updateResult(SearchResult result) {
        int idx = rows.indexOf(result);
        if (idx >= 0) {
            fireTableRowsUpdated(idx, idx);
        }
    }

    public void clear() {
        int size = rows.size();
        rows.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public void removeRow(int modelIndex) {
        if (modelIndex >= 0 && modelIndex < rows.size()) {
            rows.remove(modelIndex);
            // fireTableDataChanged refreshes # column for all rows after the deleted one
            fireTableDataChanged();
        }
    }

    public void refreshAll() {
        if (!rows.isEmpty()) {
            fireTableDataChanged();
        }
    }

    public SearchResult getResult(int modelIndex) {
        return rows.get(modelIndex);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMNS[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 0, 4, 5, 8 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int row, int col) {
        SearchResult result = rows.get(row);
        HttpRequestResponse item = result.requestResponse();

        return switch (col) {
            case 0 -> row + 1;
            case 1 -> extractHost(item);
            case 2 -> item.request() != null ? item.request().method() : "";
            case 3 -> extractPath(item);
            case 4 -> item.response() != null ? (int) item.response().statusCode() : null;
            case 5 -> item.response() != null ? item.response().body().length() : null;
            case 6 -> TIME_FMT.format(result.timestamp());
            case 7 -> result.sourceDisplay();
            case 8 -> result.matchCount();
            default -> "";
        };
    }

    private String extractHost(HttpRequestResponse item) {
        if (item.request() == null) return "";
        try {
            URL url = new URL(item.request().url());
            String host = url.getHost();
            int port = url.getPort();
            if (port > 0 && port != 80 && port != 443) {
                return host + ":" + port;
            }
            return host;
        } catch (MalformedURLException e) {
            return item.request().url();
        }
    }

    private String extractPath(HttpRequestResponse item) {
        if (item.request() == null) return "";
        try {
            URL url = new URL(item.request().url());
            String path = url.getPath();
            String query = url.getQuery();
            return query != null ? path + "?" + query : path;
        } catch (MalformedURLException e) {
            return item.request().url();
        }
    }
}
