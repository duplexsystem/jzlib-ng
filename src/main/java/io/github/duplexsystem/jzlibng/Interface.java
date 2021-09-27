package io.github.duplexsystem.jzlibng;

import io.github.duplexsystem.jzlibng.utils.JNIUtils;
import io.github.duplexsystem.jzlibng.utils.UnsafeUtils;

import java.nio.file.Path;
import java.util.ArrayList;

public class Interface {
    public static boolean usingNatives = true;

    public static ArrayList<Exception> errorList = new ArrayList<>();

    public static void init(Path rootPath) {
        try {
            JNIUtils.loadLib("jzlibng", rootPath);
            initSymbols(JNIUtils.loadLib("cpu_features", rootPath));
            if (!supportsExtensions()) {
                usingNatives = false;
                return;
            }
            if (!usingNatives) return;
            FastInflater.initLibs(rootPath);
            FastDeflater.initLibs(rootPath);
        } catch (Exception e) {
            usingNatives = false;
            error(e);
        }
    }

    public static void error(Exception e) {
        try {
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

    public static native boolean supportsExtensions();
    public static native void initSymbols(String libName);
}
