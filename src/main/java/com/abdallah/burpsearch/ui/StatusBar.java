package com.abdallah.burpsearch.ui;

import javax.swing.*;
import java.awt.*;

public final class StatusBar extends JPanel {
    private final JLabel label;
    private final JProgressBar progressBar;

    public StatusBar() {
        setLayout(new BorderLayout(6, 0));
        setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        label = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 14));

        add(progressBar, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
    }

    public void setReady() {
        label.setText("Ready");
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }

    public void setReadyLive() {
        label.setText("Ready · Live");
        progressBar.setVisible(false);
    }

    public void setSearching(int scanned, int total, int results) {
        label.setText(String.format("Searching: %,d / %,d items scanned, %,d unique results", scanned, total, results));
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
    }

    public void setDone(int results, int scanned, long millis) {
        double seconds = millis / 1000.0;
        label.setText(String.format("Done. %,d unique results from %,d items scanned in %.1fs", results, scanned, seconds));
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }

    public void setDoneLive(int results, int scanned, long millis) {
        double seconds = millis / 1000.0;
        label.setText(String.format("Done. %,d unique results from %,d items scanned in %.1fs · Live", results, scanned, seconds));
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }

    public void setError(String message) {
        label.setText("Invalid regex: " + message);
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }

    public void setLiveDropped(int dropped) {
        String base = label.getText();
        if (!base.contains("· dropped")) {
            label.setText(base + " · dropped " + dropped);
        } else {
            label.setText(base.replaceAll("· dropped \\d+", "· dropped " + dropped));
        }
    }
}
