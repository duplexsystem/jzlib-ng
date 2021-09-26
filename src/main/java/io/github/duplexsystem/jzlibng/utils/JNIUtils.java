package io.github.duplexsystem.jzlibng.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class JNIUtils {
    private static ConcurrentHashMap<String, Path> loadedLibs = new ConcurrentHashMap<>();

    private static AtomicBoolean loadLock = new AtomicBoolean();

    public static synchronized String loadLib(String name, Path rootPath) throws IOException {
        try {
            while (loadLock.get()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                    //best to keep this forcefully synced
                }
            }
            loadLock.set(true);
            if (!loadedLibs.containsKey(name)) {
                String filename = (rootPath.toString().equals("/") ? "native/" : rootPath.toString() + "/native/") + System.mapLibraryName(name);
                InputStream libStream = JNIUtils.class.getResourceAsStream(filename);
                if (libStream == null) {
                    libStream = new FileInputStream(filename);
                }

                Path tempLib = Files.createTempFile(null, System.mapLibraryName(name));

                Files.copy(libStream, tempLib, StandardCopyOption.REPLACE_EXISTING);
                loadedLibs.put(System.mapLibraryName(name), tempLib);
            }

            System.load(loadedLibs.get(System.mapLibraryName(name)).toAbsolutePath().toString());
        } finally {
            loadLock.set(false);
        }
        return System.mapLibraryName(name);
    }
}

