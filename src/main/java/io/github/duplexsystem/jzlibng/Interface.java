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
            if (!usingNatives) return;
            JNIUtils.loadLib("jzlibng", rootPath);
            initSymbols(JNIUtils.loadLib("cpu_features", rootPath));
            if (!supportsExtensions()) usingNatives = false;
            if (!usingNatives) return;
            JNIUtils.loadLib("jzlibng", rootPath);
            String libname = JNIUtils.loadLib("z", rootPath);
            FastDeflater.initSymbols(libname);
            FastInflater.initSymbols(libname);
            FastCRC32.initSymbols(libname);
            FastInflater.initIDs();
        } catch (Exception e) {
            error(e);
        }
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

    public static native boolean supportsExtensions();
    public static native void initSymbols(String libName);
}
