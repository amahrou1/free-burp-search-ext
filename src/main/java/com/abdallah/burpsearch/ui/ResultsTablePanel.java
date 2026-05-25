package com.abdallah.burpsearch.ui;

import burp.api.montoya.MontoyaApi;
import com.abdallah.burpsearch.core.SearchResult;
import com.abdallah.burpsearch.util.ClipboardUtil;
import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

public final class ResultsTablePanel extends JPanel {
    private final ResultsTableModel model;
    private final JTable table;
    private final TableRowSorter<ResultsTableModel> sorter;
    private final MontoyaApi api;
    private Consumer<SearchResult> selectionListener;

    public ResultsTablePanel(MontoyaApi api) {
        this.api = api;
        setLayout(new BorderLayout());

        model = new ResultsTableModel();
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Column sizing hints
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(60);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectionListener != null) {
                int viewRow = table.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    selectionListener.accept(model.getResult(modelRow));
                }
            }
        });

        installContextMenu();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setSelectionListener(Consumer<SearchResult> listener) {
        this.selectionListener = listener;
    }

    public ResultsTableModel getModel() {
        return model;
    }

    private void installContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem sendToRepeater = new JMenuItem("Send to Repeater");
        sendToRepeater.addActionListener(e -> withSelectedResult(result -> {
            HttpRequestResponse item = result.requestResponse();
            if (item.request() != null) {
                String host = extractHost(item);
                api.repeater().sendToRepeater(item.request(), host);
            }
        }));

        JMenuItem copyUrl = new JMenuItem("Copy URL");
        copyUrl.addActionListener(e -> withSelectedResult(result -> {
            if (result.requestResponse().request() != null) {
                ClipboardUtil.copyToClipboard(result.requestResponse().request().url());
            }
        }));

        JMenuItem copyAsCurl = new JMenuItem("Copy as cURL");
        copyAsCurl.addActionListener(e -> withSelectedResult(result -> {
            String curl = buildCurl(result.requestResponse());
            ClipboardUtil.copyToClipboard(curl);
        }));

        JMenuItem addToScope = new JMenuItem("Add host to scope");
        addToScope.addActionListener(e -> withSelectedResult(result -> {
            if (result.requestResponse().request() != null) {
                try {
                    URL parsed = new URL(result.requestResponse().request().url());
                    String hostUrl = parsed.getProtocol() + "://" + parsed.getHost();
                    int port = parsed.getPort();
                    if (port > 0) hostUrl += ":" + port;
                    api.scope().includeInScope(hostUrl);
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(this, "Could not parse URL: " + ex.getMessage());
                }
            }
        }));

        menu.add(sendToRepeater);
        menu.add(copyUrl);
        menu.add(copyAsCurl);
        menu.add(addToScope);
        menu.addSeparator();

        JMenuItem removeRow = new JMenuItem("Remove from results");
        removeRow.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                model.removeRow(modelRow);
            }
        });
        menu.add(removeRow);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopup(e);
            }

            private void handlePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                    }
                    menu.show(table, e.getX(), e.getY());
                }
            }
        });
    }

    private void withSelectedResult(Consumer<SearchResult> action) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        action.accept(model.getResult(modelRow));
    }

    private String extractHost(HttpRequestResponse item) {
        try {
            URL url = new URL(item.request().url());
            return url.getHost();
        } catch (MalformedURLException e) {
            return item.request().url();
        }
    }

    private String buildCurl(HttpRequestResponse item) {
        if (item.request() == null) return "";
        StringBuilder sb = new StringBuilder("curl");
        sb.append(" -X ").append(item.request().method());
        item.request().headers().forEach(h -> {
            String name = h.name();
            // Skip connection management headers that curl handles
            if (!name.equalsIgnoreCase("Content-Length") && !name.equalsIgnoreCase("Host")) {
                sb.append(" -H '").append(name).append(": ").append(h.value().replace("'", "'\\''")).append("'");
            }
        });
        String body = item.request().bodyToString();
        if (body != null && !body.isBlank()) {
            sb.append(" --data-binary '").append(body.replace("'", "'\\''")).append("'");
        }
        sb.append(" '").append(item.request().url().replace("'", "'\\''")).append("'");
        return sb.toString();
    }
}
