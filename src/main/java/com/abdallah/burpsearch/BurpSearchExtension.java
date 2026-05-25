package com.abdallah.burpsearch;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import com.abdallah.burpsearch.ui.SearchTab;

import javax.swing.*;

public final class BurpSearchExtension implements BurpExtension {
    private SearchTab searchTab;

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Search");

        // Build the UI on the EDT
        SwingUtilities.invokeLater(() -> {
            searchTab = new SearchTab(api);
            api.userInterface().registerSuiteTab("Search", searchTab);
        });

        api.extension().registerUnloadingHandler(() -> {
            if (searchTab != null) {
                searchTab.onExtensionUnloaded();
            }
        });

        api.logging().logToOutput("Burp Search extension loaded.");
    }
}
