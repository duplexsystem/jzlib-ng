package io.github.duplexsystem.jzlibng.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class JNIUtils {
    private static final Map OSLIBEXTMAP = Map.ofEntries(Map.entry("linux", ".so"), Map.entry("mac", ".dylib"), Map.entry("windows", ".dll"));

    private static HashMap<String, String> libNames = new HashMap<>();
    private static HashMap<String, Path> loadedLibs = new HashMap<>();

    private static AtomicBoolean loadLock = new AtomicBoolean();

    public static synchronized String loadLib(String name, Path rootPath) throws IOException {
        try {
            while (loadLock.get()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {

                }
            }
            loadLock.set(true);
            if (!loadedLibs.containsKey(name)) {
                if (Objects.equals(OSUtils.getOS(), "")) return "";

                libNames.put(name, name + OSLIBEXTMAP.get(OSUtils.getOsName()));
                InputStream libStream = JNIUtils.class.getResourceAsStream(rootPath.toString() + "native/" + libNames.get(name));

                Path tempLib = Files.createTempFile(null, libNames.get(name));

                Files.copy(libStream, tempLib, StandardCopyOption.REPLACE_EXISTING);
                loadedLibs.put(name, tempLib);
            }

            System.load(loadedLibs.get(libNames.get(name)).toAbsolutePath().toString());
        } finally {
            loadLock.set(false);
        }
        return libNames.get(name);
    }
}

