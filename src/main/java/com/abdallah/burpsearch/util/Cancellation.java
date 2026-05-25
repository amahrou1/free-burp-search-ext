package com.abdallah.burpsearch.util;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Cancellation {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void reset() {
        cancelled.set(false);
    }
}
