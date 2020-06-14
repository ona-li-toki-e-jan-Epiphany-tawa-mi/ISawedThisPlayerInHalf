package com.epiphany.isawedthisplayerinhalf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A helper for refection so that the other classes are not a mess of try/catch blocks.
 * These methods verification of input due to the limited scope of this project. It would be dead weight.
 */
public class ReflectionHelper {
    /**
     * Gets the value stored in a declared field from an instance of a class, or the default value if something goes wrong.
     *
     * @param clazz The class the field was declared in.
     * @param object The object to act as the instance of clazz.
     * @param fieldName The name of the field to get.
     * @param defaultValue The value to return if something goes wrong.
     *
     * @return The value stored in the field, or the default value.
     */
    public static Object getFieldOrDefault(Class<?> clazz, Object object, String fieldName, Object defaultValue) {
        Object returnValue;

        try {
            Field entityModel = clazz.getDeclaredField(fieldName);
            boolean wasNotAccessible = false;

            if (!entityModel.isAccessible()) {
                entityModel.setAccessible(true);
                wasNotAccessible = true;
            }

            returnValue = entityModel.get(object);

            if (wasNotAccessible)
                entityModel.setAccessible(false);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            returnValue = defaultValue;
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * Sets the value for the given field of the object.
     *
     * @param clazz The class the field was declared in.
     * @param object The object that has the field.
     * @param fieldName The name of the field.
     * @param value The value to set the field to.
     */
    public static void setField(Class<?> clazz, Object object, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            boolean wasNotAccessible = false;

            if (!field.isAccessible()) {
                field.setAccessible(true);
                wasNotAccessible = true;
            }

            field.set(object, value);

            if (wasNotAccessible)
                field.setAccessible(false);

        } catch (NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }



    /**
     * Attempts to get a declared method from the given class through reflection.
     * Returns null if no method is found.
     *
     * @param clazz The class the method was declared in.
     * @param methodName The name of the method.
     * @param argumentTypes The argument types of the method.
     *
     * @return The specified method in the class, or null.
     */
    public static Method getMethodOrNull(Class<?> clazz, String methodName, Class<?>... argumentTypes) {
        Method method;

        try {
            method = clazz.getDeclaredMethod(methodName, argumentTypes);

        } catch (NoSuchMethodException exception) {
            method = null;
            exception.printStackTrace();
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
    public static void invokeMethod(Object object, Method method, Object... methodArguments) {
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
     * Invokes a method and returns the result, or the given default value if something goes wrong.
     *
     * @param object The object to invoke the method with.
     * @param method The method to invoke.
     * @param defaultValue The value to return if something goes wrong.
     * @param methodArguments The arguments to the method.
     *
     * @return The return value of the method, or the given default value.
     */
    public static Object returnInvokeOrDefault(Object object, Method method, Object defaultValue, Object... methodArguments) {
        if (method != null) {
            Object returnValue;

            try {
                boolean notAccessible = false;

                if (!method.isAccessible()) {
                    notAccessible = true;
                    method.setAccessible(true);
                }

                returnValue = method.invoke(object, methodArguments);

                if (notAccessible)
                    method.setAccessible(false);

            } catch (IllegalAccessException | InvocationTargetException exception) {
                returnValue = defaultValue;
                exception.printStackTrace();
            }

            return returnValue;
        }

        return defaultValue;
    }
}
