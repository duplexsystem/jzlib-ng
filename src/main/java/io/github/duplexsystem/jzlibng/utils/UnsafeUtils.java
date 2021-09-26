package io.github.duplexsystem.jzlibng.utils;

import io.github.duplexsystem.jzlibng.Interface;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

public class UnsafeUtils {
    public static final Unsafe UNSAFE;

    public static ConcurrentHashMap<TheoreticalField, Field> foundFeilds = new ConcurrentHashMap<>();

    static {
        Unsafe UNSAFETMP;
        try {
            Field unsafe;
            unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);
            UNSAFETMP = (Unsafe) unsafe.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            UNSAFETMP = null;
            Interface.error(e);
        }
        UNSAFE = UNSAFETMP;
    }

    public static Object getStaticFieldObject(Class targetClass, String targetField) {
        Field foundTargetField = foundFeilds.computeIfAbsent(new TheoreticalField(targetClass, targetField), data -> {
            try {
                return data.targetClass.getDeclaredField(data.targetField);
            } catch (NoSuchFieldException e) {
                Interface.error(e);
            }
            return null;
        });
        return UNSAFE.getObject(UNSAFE.staticFieldBase(foundTargetField), UNSAFE.staticFieldOffset(foundTargetField));
    }

    public static long getFieldLong(Object targetObject, String targetField) {
        Field foundTargetField = foundFeilds.computeIfAbsent(new TheoreticalField(targetObject.getClass(), targetField), data -> {
            try {
                return data.targetClass.getDeclaredField(data.targetField);
            } catch (NoSuchFieldException e) {
                Interface.error(e);
            }
            return null;
        });
        return UNSAFE.getLong(targetObject, UNSAFE.objectFieldOffset(foundTargetField));
    }

    public record TheoreticalField(Class targetClass, String targetField) {
    }
}
