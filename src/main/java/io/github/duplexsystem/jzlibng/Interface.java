package io.github.duplexsystem.jzlibng;

import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

public class Interface {
    public static boolean usingNatives = true;

    public static Class getDeflater() {
        if (usingNatives) return FastDeflater.class;
        return Deflater.class;
    }

    public static Class getInflater() {
        if (usingNatives) return FastInflater.class;
        return Inflater.class;
    }

    public static Class getDeflaterOutputStream() {
        if (usingNatives) return FastDeflaterOutputStream.class;
        return DeflaterOutputStream.class;
    }

    public static Class getFastGZIPOutputStream() {
        if (usingNatives) return FastGZIPOutputStream.class;
        return GZIPOutputStream.class;
    }
}
