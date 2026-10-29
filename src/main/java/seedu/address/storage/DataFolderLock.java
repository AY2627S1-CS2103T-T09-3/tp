package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Prevents cooperating Scoutly processes from using the same data folder concurrently.
 * Closing this resource releases the operating system lock. The lock file is retained so another
 * process cannot create and lock a different file at the same path while a lock is still held.
 */
public final class DataFolderLock implements AutoCloseable {
    private static final String LOCK_FILE_NAME = "scoutly.lock";

    // Some systems release all process locks when any channel for that file is closed.
    // Avoid opening a second channel for a folder already locked by this JVM.
    private static final Map<Path, DataFolderLock> activeLocks = new HashMap<>();

    private final FileChannel channel;
    private final Path dataFolder;

    private DataFolderLock(FileChannel channel, Path dataFolder) {
        this.channel = channel;
        this.dataFolder = dataFolder;
    }

    /**
     * Attempts to lock {@code dataFolder}, creating it if necessary.
     * Returns an empty result if another instance already holds the lock.
     *
     * @throws IOException if the folder or lock file cannot be accessed
     */
    public static synchronized Optional<DataFolderLock> tryAcquire(Path dataFolder) throws IOException {
        requireNonNull(dataFolder);
        Files.createDirectories(dataFolder);
        Path realFolder = dataFolder.toRealPath();
        if (activeLocks.containsKey(realFolder)) {
            return Optional.empty();
        }
        FileChannel channel = FileChannel.open(realFolder.resolve(LOCK_FILE_NAME),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        boolean acquired = false;
        try {
            try {
                acquired = channel.tryLock() != null;
            } catch (OverlappingFileLockException e) {
                return Optional.empty();
            }
            if (!acquired) {
                return Optional.empty();
            }
            DataFolderLock lock = new DataFolderLock(channel, realFolder);
            activeLocks.put(realFolder, lock);
            return Optional.of(lock);
        } finally {
            if (!acquired) {
                channel.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (DataFolderLock.class) {
            try {
                channel.close();
            } finally {
                activeLocks.remove(dataFolder, this);
            }
        }
    }
}
