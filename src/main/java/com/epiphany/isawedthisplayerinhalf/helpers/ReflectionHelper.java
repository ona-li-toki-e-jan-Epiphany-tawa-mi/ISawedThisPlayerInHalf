package com.epiphany.isawedthisplayerinhalf.helpers;

import javax.annotation.Nullable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * MIT License
 *
 * Copyright (c) 2020-2022 Nathaniel Needham
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

/**
 * A helper for refection so that the other classes are not a mess of try/catch blocks.
 */
public class ReflectionHelper {
    /**
     * Gets a declared field from a class, or null, if nothing is found.
     *
     * @param clazz The class to get the field from.
     * @param fieldName The name of the field to get.
     * @param obfuscatedFieldName The obfuscated name of the field to get.
     *
     * @return The field with the name fieldName in class clazz, or null.
     */
    public static Field getDeclaredFieldOrNull(Class<?> clazz, String fieldName, String obfuscatedFieldName) {
        Field field;

        try {
            field = clazz.getDeclaredField(fieldName);

        } catch (NoSuchFieldException noSuchFieldException) {
            try {
                field = clazz.getDeclaredField(obfuscatedFieldName);

            } catch (NoSuchFieldException innerNoSuchFieldException) {
                field = null;

                noSuchFieldException.printStackTrace();
                innerNoSuchFieldException.printStackTrace();
            }
        }

        makeAccessible(field);
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
    public static Object getValueOrDefault(Field field, Object object, @Nullable Object defaultValue) {
        Object returnValue;

        try {
            returnValue = field.get(object);

        } catch (IllegalAccessException illegalAccessException) {
            returnValue = defaultValue;
            illegalAccessException.printStackTrace();
        }

        return returnValue;
    }

    /**
     * Sets the value for the field in the given object.
     *
     * @param field The field to set the value of.
     * @param object The object to set the field's value to.
     * @param value The value to set to the field.
     */
    public static void setValue(Field field, Object object, @Nullable Object value) {
        try {
            field.set(object, value);

        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
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
    public static Method getDeclaredMethodOrNull(Class<?> clazz, String methodName, Class<?>... argumentTypes) {
       return getDeclaredMethodOrNull(clazz, methodName, null, argumentTypes);
    }

    /**
     * Attempts to get a declared method from the given class through reflection.
     * Returns null if no method is found.
     *
     * @param clazz The class the method was declared in.
     * @param methodName The name of the method.
     * @param argumentTypes The argument types of the method.
     * @param obfuscatedMethodName The obfuscated method name.
     *
     * @return The specified method in the class, or null.
     */
    public static Method getDeclaredMethodOrNull(Class<?> clazz, String methodName, @Nullable String obfuscatedMethodName, Class<?>... argumentTypes) {
        Method method;

        try {
            method = clazz.getDeclaredMethod(methodName, argumentTypes);

        } catch (NoSuchMethodException exception) {
            if (obfuscatedMethodName != null) {
                try {
                    method = clazz.getDeclaredMethod(obfuscatedMethodName, argumentTypes);

                } catch (NoSuchMethodException innerException) {
                    innerException.printStackTrace();
                    exception.printStackTrace();
                    method = null;
                }

            } else {
                exception.printStackTrace();
                method = null;
            }
        }

        makeAccessible(method);
        return method;
    }

    /**
     * Invokes a method.
     *
     * @param object The object to invoke the method with.
     * @param method The method to invoke.
     * @param methodArguments The arguments to the method.
     *
     * @return The result of invoking the method, or null, if the method call fails.
     */
    public static Object invokeMethod(Method method, Object object, Object... methodArguments) {
        try {
            return method.invoke(object, methodArguments);

        } catch (IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
            return null;
        }
    }



    /**
     * Gets the class under the given fully-qualified name
     *
     * @param className The fully-qualified name of the desired class.
     *
     * @return The desired class, or null, if nothing is found.
     */
    public static Class<?> classForNameOrNull(String className) {
        try {
            return Class.forName(className);

        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
            return null;
        }
    }



    /**
     * Sets fields, methods, and any other object with accessibility restrictions to be accessible.
     *
     * @param accessibleObject The object to make accessible.
     */
    private static void makeAccessible(@Nullable AccessibleObject accessibleObject) {
        if (accessibleObject != null)
            accessibleObject.setAccessible(true);
    }
}
