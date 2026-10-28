package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A separate JVM used to exercise operating system lock ownership in {@link DataFolderLockTest}.
 */
public class DataFolderLockTestProcess {

    /**
     * Reports whether the folder could be locked, optionally holding the lock until standard input closes.
     */
    public static void main(String[] args) throws IOException {
        try (DataFolderLock lock = DataFolderLock.tryAcquire(Path.of(args[0])).orElse(null)) {
            System.out.println(lock == null ? "IN_USE" : "ACQUIRED");
            System.out.flush();
            if (lock != null && args.length > 1 && args[1].equals("hold")) {
                System.in.read();
            }
        }
    }
}
