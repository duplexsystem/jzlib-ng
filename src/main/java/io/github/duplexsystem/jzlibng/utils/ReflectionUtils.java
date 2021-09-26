package io.github.duplexsystem.jzlibng.utils;

import io.github.duplexsystem.jzlibng.Interface;

import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {
    public static ConcurrentHashMap<String, Class> reflectedClasses = new ConcurrentHashMap<>();

    public static Class getClass(String className) {
        return reflectedClasses.computeIfAbsent(className, name -> getReflectedClass(name));
    }

    private static Class getReflectedClass(String className) {
        try {
            Class classObj;
            int $loc = className.indexOf("$");
            if ($loc > -1) {
                classObj = getNestedClass(Class.forName(className.substring(0, $loc)), className.substring($loc + 1));
            } else {
                classObj = Class.forName(className);
            }
            assert classObj != null;
            return classObj;
        } catch (ClassNotFoundException e) {
            Interface.error(e);
        }
        return null;
    }

    private static Class getNestedClass(Class upperClass, String nestedClassName) {
        Class[] classObjArr = upperClass.getDeclaredClasses();
        for (Class classArrObj : classObjArr) {
            if (classArrObj.getName().equals(upperClass.getName() + "$" + nestedClassName)) {
                return classArrObj;
            }
        }
        return null;
    }
}
