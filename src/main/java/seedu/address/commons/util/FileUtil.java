package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes UTF-8 text to a temporary file, then atomically replaces the destination.
     * Creates missing parent directories. If writing or atomic replacement fails, the previous file is preserved.
     * Filesystems without atomic replacement support report an error rather than risk a partial save.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        writeToFile(file, content, Files::write);
    }

    /** Performs the same safe replacement with an injectable writer for partial-write failure tests. */
    static void writeToFile(Path file, String content, FileWriter writer) throws IOException {
        requireNonNull(file);
        requireNonNull(content);
        requireNonNull(writer);

        // Keep saving through an existing symbolic link rather than replacing the link itself.
        Path destination = Files.isSymbolicLink(file) ? file.toRealPath() : file.toAbsolutePath();
        boolean exists = Files.exists(destination);
        if (exists && !Files.isWritable(destination)) {
            throw new AccessDeniedException(destination.toString());
        }
        Set<PosixFilePermission> permissions = exists
                && Files.getFileAttributeView(destination, PosixFileAttributeView.class) != null
                ? Files.getPosixFilePermissions(destination) : null;

        createParentDirsOfFile(destination);
        Path temporaryFile = Files.createTempFile(destination.getParent(), ".scoutly-", ".tmp");
        try {
            writer.write(temporaryFile, content.getBytes(CHARSET));
            if (permissions != null) {
                Files.setPosixFilePermissions(temporaryFile, permissions);
            }
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException | Error failure) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    /** Writes a complete temporary file or reports an I/O failure. */
    @FunctionalInterface
    interface FileWriter {
        void write(Path file, byte[] content) throws IOException;
    }
}
