package com.epiphany.isawedthisplayerinhalf.helpers;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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
     * @param obfuscatedFieldName The obfuscated name of the field to get.
     *
     * @return The field with the name fieldName in class clazz.
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
     * Sets fields and methods to be accessible.
     *
     * @param object The field or method to make accessible.
     */
    public static void makeAccessible(@Nullable Object object) {
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
