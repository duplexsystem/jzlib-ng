package io.github.duplexsystem.jzlibng.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JNIUtils {
    private static final Map OSLIBEXTMAP = Map.ofEntries(Map.entry("linux", ".so"), Map.entry("mac", ".dylib"), Map.entry("windows", ".dll"));

    public static String loadLib(String name) throws IOException {
        if (Objects.equals(OSUtils.getOsName(), "")) return "";
        String libName = name + OSLIBEXTMAP.get(OSUtils.getOsName());
        InputStream libStream = JNIUtils.class.getResourceAsStream("native/" + libName);

        Path tempLib = Files.createTempFile(null, libName);
        assert libStream != null;
        Files.copy(libStream, tempLib, StandardCopyOption.REPLACE_EXISTING);

        System.load(tempLib.toAbsolutePath().toString());

        return libName;
    }
}

