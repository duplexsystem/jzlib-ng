package io.github.duplexsystem.jzlibng;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

public class Interface {
    public static boolean usingNatives = true;

    public static Deflater newDeflater() {
        if (usingNatives) return (Deflater) (Object) new FastDeflater();
        return new Deflater();
    }

    public static Inflater newInflator() {
        if (usingNatives) return (Inflater) (Object) new FastInflater();
        return new Inflater();
    }

    public static Deflater newDeflater(int level, boolean nowrap) {
        if (usingNatives) return (Deflater) (Object) new FastDeflater(level, nowrap);
        return new Deflater(level, nowrap);
    }

    public static Inflater newInflator(boolean nowrap) {
        if (usingNatives) return (Inflater) (Object) new FastInflater(nowrap);
        return new Inflater(nowrap);
    }

    public static void init(Path rootPath) {
        System.out.println(rootPath);
        FastDeflater.initLibs(rootPath);
        FastInflater.initLibs(rootPath);
    }
}
