package seedu.address.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/** Starts JavaFX once per test worker and propagates failures from its application thread. */
public final class FxTestUtil {
    private static boolean initialized;

    private FxTestUtil() {
    }

    /** Starts the toolkit without relying on test-class execution order. */
    public static synchronized void initialize() throws Exception {
        if (initialized) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX startup timed out");
        }
        initialized = true;
    }

    /** Runs an assertion or interaction on JavaFX with a bounded wait. */
    public static void run(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
