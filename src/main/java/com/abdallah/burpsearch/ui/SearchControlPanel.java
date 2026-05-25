package com.abdallah.burpsearch.ui;

import com.abdallah.burpsearch.core.*;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class SearchControlPanel extends JPanel {
    private final JTextField expressionField;
    private final JButton searchButton;
    private final JButton cancelButton;

    private final JCheckBox regexCheck;
    private final JCheckBox caseSensitiveCheck;
    private final JCheckBox negativeCheck;
    private final JCheckBox inScopeCheck;
    private final JCheckBox dynamicUpdateCheck;

    private final Map<SearchLocation, JCheckBox> locationChecks = new EnumMap<>(SearchLocation.class);
    private final Map<SearchSource, JCheckBox> sourceChecks = new EnumMap<>(SearchSource.class);

    private Runnable searchAction;
    private Runnable cancelAction;
    private Runnable dynamicUpdateChangeAction;

    public SearchControlPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: expression + buttons
        expressionField = new JTextField(40);
        expressionField.setToolTipText("Enter search expression");
        searchButton = new JButton("Search");
        cancelButton = new JButton("Cancel");
        cancelButton.setVisible(false);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        add(expressionField, gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        add(searchButton, gbc);
        gbc.gridx = 2;
        add(cancelButton, gbc);

        // Row 1: toggles
        regexCheck = new JCheckBox("Regex");
        caseSensitiveCheck = new JCheckBox("Case sensitive");
        negativeCheck = new JCheckBox("Negative match");

        JPanel toggleRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toggleRow1.add(regexCheck);
        toggleRow1.add(caseSensitiveCheck);
        toggleRow1.add(negativeCheck);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        add(toggleRow1, gbc);

        // Row 2: more toggles
        inScopeCheck = new JCheckBox("In-scope only");
        dynamicUpdateCheck = new JCheckBox("Dynamic update");

        JPanel toggleRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toggleRow2.add(inScopeCheck);
        toggleRow2.add(dynamicUpdateCheck);

        gbc.gridy = 2;
        add(toggleRow2, gbc);

        // Row 3: locations
        JPanel locPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        locPanel.add(new JLabel("Locations:"));
        for (SearchLocation loc : SearchLocation.values()) {
            JCheckBox cb = new JCheckBox(loc.getDisplayName(), true);
            locationChecks.put(loc, cb);
            cb.addActionListener(e -> updateSearchButtonState());
            locPanel.add(cb);
        }

        gbc.gridy = 3;
        add(locPanel, gbc);

        // Row 4: sources
        JPanel srcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        srcPanel.add(new JLabel("Sources:   "));
        for (SearchSource src : SearchSource.values()) {
            JCheckBox cb = new JCheckBox(src.getDisplayName(), true);
            sourceChecks.put(src, cb);
            cb.addActionListener(e -> updateSearchButtonState());
            srcPanel.add(cb);
        }

        gbc.gridy = 4;
        add(srcPanel, gbc);

        // Wiring
        searchButton.addActionListener(e -> { if (searchAction != null) searchAction.run(); });
        cancelButton.addActionListener(e -> { if (cancelAction != null) cancelAction.run(); });
        dynamicUpdateCheck.addActionListener(e -> { if (dynamicUpdateChangeAction != null) dynamicUpdateChangeAction.run(); });

        expressionField.addActionListener(e -> { if (searchAction != null) searchAction.run(); });
        expressionField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSearchButtonState(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSearchButtonState(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSearchButtonState(); }
        });

        updateSearchButtonState();
    }

    private void updateSearchButtonState() {
        boolean hasExpression = !expressionField.getText().isBlank();
        boolean hasLocation = locationChecks.values().stream().anyMatch(JCheckBox::isSelected);
        boolean hasSource = sourceChecks.values().stream().anyMatch(JCheckBox::isSelected);

        searchButton.setEnabled(hasExpression && hasLocation && hasSource);

        if (!hasExpression) {
            searchButton.setToolTipText("Enter a search expression");
        } else if (!hasLocation) {
            searchButton.setToolTipText("Select at least one location");
        } else if (!hasSource) {
            searchButton.setToolTipText("Select at least one source");
        } else {
            searchButton.setToolTipText(null);
        }
    }

    public SearchQuery buildQuery() {
        String expression = expressionField.getText();

        Set<SearchLocation> locations = EnumSet.noneOf(SearchLocation.class);
        locationChecks.forEach((loc, cb) -> { if (cb.isSelected()) locations.add(loc); });

        Set<SearchSource> sources = EnumSet.noneOf(SearchSource.class);
        sourceChecks.forEach((src, cb) -> { if (cb.isSelected()) sources.add(src); });

        SearchOptions options = new SearchOptions(
                regexCheck.isSelected(),
                caseSensitiveCheck.isSelected(),
                negativeCheck.isSelected(),
                inScopeCheck.isSelected(),
                locations,
                sources
        );

        return new SearchQuery(expression, options);
    }

    public boolean isDynamicUpdateEnabled() {
        return dynamicUpdateCheck.isSelected();
    }

    public void setSearching(boolean searching) {
        searchButton.setVisible(!searching);
        cancelButton.setVisible(searching);
        expressionField.setEnabled(!searching);
        regexCheck.setEnabled(!searching);
        caseSensitiveCheck.setEnabled(!searching);
        negativeCheck.setEnabled(!searching);
        inScopeCheck.setEnabled(!searching);
        locationChecks.values().forEach(cb -> cb.setEnabled(!searching));
        sourceChecks.values().forEach(cb -> cb.setEnabled(!searching));
    }

    public void setSearchAction(Runnable action) { this.searchAction = action; }
    public void setCancelAction(Runnable action) { this.cancelAction = action; }
    public void setDynamicUpdateChangeAction(Runnable action) { this.dynamicUpdateChangeAction = action; }
}
