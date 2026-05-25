package com.abdallah.burpsearch.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public final class PreviewPanel extends JPanel {
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;

    public PreviewPanel(MontoyaApi api) {
        setLayout(new BorderLayout());

        requestEditor = api.userInterface().createHttpRequestEditor(READ_ONLY);
        responseEditor = api.userInterface().createHttpResponseEditor(READ_ONLY);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                requestEditor.uiComponent(),
                responseEditor.uiComponent()
        );
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);

        add(split, BorderLayout.CENTER);
    }

    public void show(HttpRequestResponse item, String searchExpression) {
        if (item == null) {
            requestEditor.setRequest(null);
            responseEditor.setResponse(null);
            return;
        }

        requestEditor.setRequest(item.request());
        responseEditor.setResponse(item.response());

        // Attempt to set search expression for highlighting.
        // The Montoya API exposes setSearchExpression on editors in some versions.
        // We use reflection to avoid compilation failure if the method is absent.
        if (searchExpression != null && !searchExpression.isBlank()) {
            trySetSearchExpression(requestEditor, searchExpression);
            trySetSearchExpression(responseEditor, searchExpression);
        }
    }

    private void trySetSearchExpression(Object editor, String expression) {
        try {
            editor.getClass().getMethod("setSearchExpression", String.class).invoke(editor, expression);
        } catch (NoSuchMethodException ignored) {
            // API version does not expose setSearchExpression; the editor's own search bar is the fallback
        } catch (Exception ignored) {
        }
    }

    public void clear() {
        requestEditor.setRequest(null);
        responseEditor.setResponse(null);
    }
}
