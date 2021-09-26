package io.github.duplexsystem.jzlibng;

import io.github.duplexsystem.jzlibng.utils.UnsafeUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Interface {
    public static boolean usingNatives = true;

    public static ArrayList<Exception> errorList = new ArrayList<>();

    public static void init(Path rootPath) {
        if (!usingNatives) return;
        FastInflater.initLibs(rootPath);
        FastDeflater.initLibs(rootPath);
    }

    public static void error(Exception e) {
        try {
            e.printStackTrace();
            usingNatives = false;
            if (errorList.size() >= 2) {
                errorList.get(0).printStackTrace();
                errorList.get(1).printStackTrace();
                UnsafeUtils.UNSAFE.throwException(e);
            }
            errorList.add(e);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
