package com.balugaq.sfworldedit.utils;

import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * @author Final_ROOT
 */
@UtilityClass
public class ReflectionUtil {

    public static boolean setValue(Object object, String field, Object value) {
        try {
            Field declaredField = object.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static <T> boolean setStaticValue(Class<T> clazz, String field, Object value) {
        try {
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(null, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Method getMethod(Class<?> clazz, String methodName) {
        while (clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static <T, V> T getProperty(Object o, Class<V> clazz, String fieldName) throws IllegalAccessException {
        Field field = getField(clazz, fieldName);
        if (field != null) {
            boolean b = field.canAccess(o);
            field.setAccessible(true);
            Object result = field.get(o);
            field.setAccessible(b);
            return (T) result;
        }

        return null;
    }

    public static Pair<Field, Class<?>> getDeclaredFieldsRecursively(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return new Pair<>(field, clazz);
        } catch (Throwable e) {
            clazz = clazz.getSuperclass();
            if (clazz == null) {
                return null;
            } else {
                return getDeclaredFieldsRecursively(clazz, fieldName);
            }
        }
    }
}
