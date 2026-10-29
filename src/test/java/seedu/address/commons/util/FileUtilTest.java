package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests safe replacement of saved files, including failures after a partial write. */
public class FileUtilTest {
    @TempDir
    public Path directory;

    @Test
    public void writeToFile_existingFile_replacesCompleteContents() throws IOException {
        Path file = directory.resolve("candidates.json");
        Files.writeString(file, "previous content that is longer than the replacement");

        FileUtil.writeToFile(file, "Zoë 李");

        assertEquals("Zoë 李", Files.readString(file));
        assertDirectoryContents(directory, file);
    }

    @Test
    public void writeToFile_missingParent_createsFile() throws IOException {
        Path file = directory.resolve("new/data/candidates.json");

        FileUtil.writeToFile(file, "new candidates");

        assertEquals("new candidates", Files.readString(file));
        assertDirectoryContents(file.getParent(), file);
    }

    @Test
    public void writeToFile_partialWriteFailure_preservesOriginalAndCleansTemporaryFile() throws IOException {
        Path file = directory.resolve("candidates.json");
        byte[] original = "previously saved candidates".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(file, original);
        IOException failure = new IOException("Disk full after partial write");

        IOException thrown = assertThrows(IOException.class, () ->
                FileUtil.writeToFile(file, "replacement candidates", (temporary, content) -> {
                    assertNotEquals(file, temporary);
                    assertEquals(file.getParent(), temporary.getParent());
                    Files.writeString(temporary, "partial");
                    throw failure;
                }));

        assertSame(failure, thrown);
        assertArrayEquals(original, Files.readAllBytes(file));
        assertDirectoryContents(directory, file);
    }

    @Test
    public void writeToFile_firstWriteFailure_leavesNoDestinationOrTemporaryFile() throws IOException {
        Path file = directory.resolve("candidates.json");

        assertThrows(IOException.class, () -> FileUtil.writeToFile(file, "new candidates", (temporary, content) -> {
            Files.writeString(temporary, "partial");
            throw new IOException("Disk full");
        }));

        assertFalse(Files.exists(file));
        assertDirectoryContents(directory);
    }

    @Test
    public void writeToFile_replacementFailure_preservesDestinationAndCleansTemporaryFile() throws IOException {
        Path destination = Files.createDirectory(directory.resolve("candidates.json"));
        Path existingFile = destination.resolve("keep.txt");
        Files.writeString(existingFile, "keep this data");

        assertThrows(IOException.class, () -> FileUtil.writeToFile(destination, "replacement"));

        assertEquals("keep this data", Files.readString(existingFile));
        assertDirectoryContents(directory, destination);
        assertDirectoryContents(destination, existingFile);
    }

    @Test
    public void writeToFile_readOnlyDestination_preservesOriginal() throws IOException {
        assumeTrue(Files.getFileAttributeView(directory, PosixFileAttributeView.class) != null);
        Path file = directory.resolve("candidates.json");
        Files.writeString(file, "saved candidates");
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(file);
        try {
            Files.setPosixFilePermissions(file, Set.of(PosixFilePermission.OWNER_READ));
            assumeFalse(Files.isWritable(file), "Privileged users can write read-only files");

            assertThrows(AccessDeniedException.class, () -> FileUtil.writeToFile(file, "replacement"));

            assertEquals("saved candidates", Files.readString(file));
            assertDirectoryContents(directory, file);
        } finally {
            Files.setPosixFilePermissions(file, originalPermissions);
        }
    }

    @Test
    public void writeToFile_existingPermissions_preservesPermissions() throws IOException {
        assumeTrue(Files.getFileAttributeView(directory, PosixFileAttributeView.class) != null);
        Path file = directory.resolve("candidates.json");
        Files.writeString(file, "original");
        Set<PosixFilePermission> permissions = Set.of(PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ);
        Files.setPosixFilePermissions(file, permissions);

        FileUtil.writeToFile(file, "replacement");

        assertEquals(permissions, Files.getPosixFilePermissions(file));
    }

    @Test
    public void writeToFile_symbolicLink_updatesTargetAndPreservesLink() throws IOException {
        assumeTrue(Files.getFileAttributeView(directory, PosixFileAttributeView.class) != null);
        Path target = directory.resolve("real-candidates.json");
        Files.writeString(target, "original");
        Path link = directory.resolve("candidates.json");
        Files.createSymbolicLink(link, target.getFileName());

        FileUtil.writeToFile(link, "replacement");

        assertTrue(Files.isSymbolicLink(link));
        assertEquals("replacement", Files.readString(target));
        assertEquals("replacement", Files.readString(link));
    }

    private void assertDirectoryContents(Path parent, Path... expected) throws IOException {
        try (var files = Files.list(parent)) {
            assertEquals(Set.copyOf(List.of(expected)), Set.copyOf(files.toList()));
        }
    }
}
