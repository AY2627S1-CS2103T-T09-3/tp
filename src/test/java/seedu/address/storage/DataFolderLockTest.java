package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DataFolderLockTest {

    @TempDir
    public Path directory;

    @Test
    public void tryAcquire_nullFolder_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DataFolderLock.tryAcquire(null));
    }

    @Test
    public void tryAcquire_newFolder_reopensWithExistingLockFile() throws Exception {
        Path dataFolder = directory.resolve("nested/data");
        DataFolderLock first = DataFolderLock.tryAcquire(dataFolder).orElseThrow();
        first.close();
        assertTrue(Files.exists(dataFolder.resolve("scoutly.lock")));

        try (DataFolderLock reopened = DataFolderLock.tryAcquire(dataFolder).orElseThrow()) {
            first.close(); // Closing an old handle must not release the new owner's lock.
            assertTrue(DataFolderLock.tryAcquire(dataFolder).isEmpty());
        }
    }

    @Test
    public void tryAcquire_sameFolderAndAlias_doesNotReleaseOriginalProcessLock() throws Exception {
        try (DataFolderLock first = DataFolderLock.tryAcquire(directory).orElseThrow()) {
            assertTrue(DataFolderLock.tryAcquire(directory).isEmpty());
            assertTrue(DataFolderLock.tryAcquire(directory.resolve(".")).isEmpty());
            Process contender = startProcess(directory, false);
            try {
                assertEquals("IN_USE", readStatus(contender));
                assertTrue(contender.waitFor(10, TimeUnit.SECONDS));
                assertEquals(0, contender.exitValue());
            } finally {
                stopProcess(contender);
            }
        }
    }

    @Test
    public void tryAcquire_differentFolders_canBeUsedIndependently() throws Exception {
        Path otherFolder = directory.resolve("other");
        try (DataFolderLock first = DataFolderLock.tryAcquire(directory).orElseThrow();
                DataFolderLock second = DataFolderLock.tryAcquire(otherFolder).orElseThrow()) {
            assertTrue(DataFolderLock.tryAcquire(directory).isEmpty());
            assertTrue(DataFolderLock.tryAcquire(otherFolder).isEmpty());
        }
    }

    @Test
    public void tryAcquire_otherProcessOwnsFolder_reopensAfterNormalExit() throws Exception {
        Process owner = startProcess(directory, true);
        try {
            assertEquals("ACQUIRED", readStatus(owner));
            assertTrue(DataFolderLock.tryAcquire(directory).isEmpty());
            assertTrue(DataFolderLock.tryAcquire(directory).isEmpty());
            owner.getOutputStream().close();
            assertTrue(owner.waitFor(10, TimeUnit.SECONDS));
            assertEquals(0, owner.exitValue());

            try (DataFolderLock reopened = DataFolderLock.tryAcquire(directory).orElseThrow()) {
                assertTrue(DataFolderLock.tryAcquire(directory).isEmpty());
            }
        } finally {
            stopProcess(owner);
        }
    }

    @Test
    public void tryAcquire_ownerTerminated_reopensWithoutDeletingLockFile() throws Exception {
        Process owner = startProcess(directory, true);
        try {
            assertEquals("ACQUIRED", readStatus(owner));
            assertTrue(DataFolderLock.tryAcquire(directory).isEmpty());
            owner.destroyForcibly();
            assertTrue(owner.waitFor(10, TimeUnit.SECONDS));

            try (DataFolderLock reopened = DataFolderLock.tryAcquire(directory).orElseThrow()) {
                assertTrue(DataFolderLock.tryAcquire(directory).isEmpty());
            }
        } finally {
            stopProcess(owner);
        }
    }

    @Test
    public void tryAcquire_regularFileAsFolder_throwsIoException() throws Exception {
        Path file = directory.resolve("file");
        Files.writeString(file, "existing data");
        assertThrows(IOException.class, () -> DataFolderLock.tryAcquire(file));
        assertEquals("existing data", Files.readString(file));
    }

    private Process startProcess(Path dataFolder, boolean holdLock) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        Path mainClasses = Path.of(DataFolderLock.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path testClasses = Path.of(DataFolderLockTestProcess.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        return new ProcessBuilder(java.toString(), "-cp", mainClasses + File.pathSeparator + testClasses,
                DataFolderLockTestProcess.class.getName(), dataFolder.toString(), holdLock ? "hold" : "")
                .redirectErrorStream(true).start();
    }

    private String readStatus(Process process) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return process.inputReader(StandardCharsets.UTF_8).readLine();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).get(10, TimeUnit.SECONDS);
    }

    private void stopProcess(Process process) throws InterruptedException {
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        assertTrue(process.waitFor(10, TimeUnit.SECONDS));
        assertFalse(process.isAlive());
    }
}
