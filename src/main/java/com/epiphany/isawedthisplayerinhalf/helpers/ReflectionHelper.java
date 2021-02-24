package com.epiphany.isawedthisplayerinhalf.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A helper for refection so that the other classes are not a mess of try/catch blocks.
 */
public class ReflectionHelper {
    /**
     * Gets a declared field from a class, or null.
     *
     * @param clazz The class to get the field from.
     * @param fieldName The name of the field to get.
     *
     * @return The field with the name fieldName in class clazz.
     */
    public static Field getFieldOrNull(Class<?> clazz, String fieldName) {
        Field field;

        try {
            field = clazz.getDeclaredField(fieldName);

        } catch (NoSuchFieldException exception) {
            field = null;
            exception.printStackTrace();
        }

        return field;
    }

    /**
     * Gets a declared field from a class, or null.
     *
     * @param clazz The class to get the field from.
     * @param fieldName The name of the field to get.
     * @param obfuscatedFieldName The obfuscated name of the field to get.
     *
     * @return The field with the name fieldName in class clazz.
     */
    public static Field getFieldOrNull(Class<?> clazz, String fieldName, String obfuscatedFieldName) {
        Field field;

        try {
            field = clazz.getDeclaredField(fieldName);

        } catch (NoSuchFieldException exception) {
            try {
                field = clazz.getDeclaredField(obfuscatedFieldName);

            } catch (NoSuchFieldException innerException) {
                field = null;

                exception.printStackTrace();
                innerException.printStackTrace();
            }
        }

        return field;
    }

    /**
     * Gets the value stored in the field of the given class, or the default value if something goes wrong.
     *
     * @param field The field to get the value from.
     * @param object The object that has the field.
     * @param defaultValue The value to return if something goes wrong.
     *
     * @return The value stored in the field, or the default value.
     */
    public static Object getFieldOrDefault(Field field, Object object, Object defaultValue) {
        if (field != null) {
            Object returnValue;

            try {
                boolean wasNotAccessible = false;

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                    wasNotAccessible = true;
                }

                returnValue = field.get(object);

                if (wasNotAccessible)
                    field.setAccessible(false);

            } catch (IllegalAccessException e) {
                returnValue = defaultValue;
                e.printStackTrace();
            }

            return returnValue;
        }

        return defaultValue;
    }

    /**
     * Sets the value for the field in the given object.
     *
     * @param field The field to set the value of.
     * @param object The object to set the field's value to.
     * @param value The value to set to the field.
     */
    public static void setField(Field field, Object object, Object value) {
        if (field != null)
            try {
                boolean wasNotAccessible = false;

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                    wasNotAccessible = true;
                }

                field.set(object, value);

                if (wasNotAccessible)
                    field.setAccessible(false);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
    }



    /**
     * Attempts to get a declared method from the given class through reflection.
     * Returns null if no method is found.
     *
     * @param clazz The class the method was declared in.
     * @param methodName The name of the method.
     * @param obfuscatedMethodName The obfuscated name of the method.
     * @param argumentTypes The argument types of the method.
     *
     * @return The specified method in the class, or null.
     */
    public static Method getMethodOrNull(Class<?> clazz, String methodName, String obfuscatedMethodName, Class<?>... argumentTypes) {
        Method method;

        try {
            method = clazz.getDeclaredMethod(methodName, argumentTypes);

        } catch (NoSuchMethodException exception) {
            try {
                method = clazz.getDeclaredMethod(obfuscatedMethodName, argumentTypes);

            } catch (NoSuchMethodException innerException) {
                method = null;

                exception.printStackTrace();
                innerException.printStackTrace();
            }
        }

        return method;
    }

    /**
     * Invokes a method.
     *
     * @param object The object to invoke the method with.
     * @param method The method to invoke.
     * @param methodArguments The arguments to the method.
     */
    public static void invokeMethod(Method method, Object object, Object... methodArguments) {
        if (method != null)
            try {
                boolean notAccessible = false;

                if (!method.isAccessible()) {
                    notAccessible = true;
                    method.setAccessible(true);
                }

                method.invoke(object, methodArguments);

                if (notAccessible)
                    method.setAccessible(false);

            } catch (IllegalAccessException | InvocationTargetException exception) {
                exception.printStackTrace();
            }
    }



    /**
     * Sets fields and methods to be accessible.
     *
     * @param object The field or method to make accessible.
     */
    public static void makeAccessible(Object object) {
        if (object != null)
            if (object instanceof Method) {
                Method method = (Method) object;

                if (!method.isAccessible())
                    method.setAccessible(true);

            } else if (object instanceof Field) {
                Field field = (Field) object;

                if (!field.isAccessible())
                    field.setAccessible(true);
            }
    }
}
