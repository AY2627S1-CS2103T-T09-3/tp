package seedu.address.testutil;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Starts independent JVMs with the test runtime's dependencies and coverage instrumentation.
 */
public class TestProcess {

    /**
     * Runs the given entry point using the current JDK, including classes loaded by the Gradle test worker.
     */
    public static Process start(Class<?> entryPoint, String... args) throws IOException {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", executable).toString());
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("-javaagent:") && argument.contains("jacoco")) {
                command.add(argument.replace("append=false", "append=true"));
            }
        }
        command.add("-cp");
        command.add(getClassPath(entryPoint.getClassLoader()));
        command.add(entryPoint.getName());
        command.addAll(Arrays.asList(args));
        return new ProcessBuilder(command).start();
    }

    private static String getClassPath(ClassLoader loader) {
        Set<String> paths = new LinkedHashSet<>(Arrays.asList(System.getProperty("java.class.path")
                .split(Pattern.quote(File.pathSeparator))));
        for (ClassLoader current = loader; current != null; current = current.getParent()) {
            if (current instanceof URLClassLoader urls) {
                for (URL url : urls.getURLs()) {
                    if (url.getProtocol().equals("file")) {
                        try {
                            paths.add(Path.of(url.toURI()).toString());
                        } catch (URISyntaxException e) {
                            throw new IllegalStateException("Invalid test classpath URL: " + url, e);
                        }
                    }
                }
            }
        }
        return String.join(File.pathSeparator, paths);
    }
}
