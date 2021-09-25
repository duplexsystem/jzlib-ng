package io.github.duplexsystem.jzlibng.utils;

import java.util.Locale;

public final class OSUtils
{
    private static String OS = null;

    public static String getOsName()
    {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }

    public String getOS() {
        if (OS == null) return OS;
        else if (isOS("windows")) return OS = "windows";
        else if (isOS("linux")) return OS = "linux";
        else if (isOS("mac")) return OS = "mac";
        else return OS = "";
    }

    private static boolean isOS(String os)
    {
        return getOsName().toLowerCase(Locale.ROOT).startsWith(os);
    }

}
