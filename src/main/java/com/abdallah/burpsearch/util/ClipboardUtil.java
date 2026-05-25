package com.abdallah.burpsearch.util;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public final class ClipboardUtil {
    private ClipboardUtil() {}

    public static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit()
               .getSystemClipboard()
               .setContents(new StringSelection(text), null);
    }
}
