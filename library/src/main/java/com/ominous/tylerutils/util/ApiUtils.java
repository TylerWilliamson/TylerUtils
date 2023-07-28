/*
 * Copyright 2020 - 2023 Tyler Williamson
 *
 * This file is part of TylerUtils.
 *
 * TylerUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TylerUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.util;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ApiUtils {
    private static Method forName, getDeclaredMethod;

    static {
        try {
            forName = Class.class
                    .getDeclaredMethod("forName", String.class);
            getDeclaredMethod = Class.class
                    .getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
        } catch (NoSuchMethodException e) {
            //They exist
        }
    }

    //https://github.com/tiann/FreeReflection/blob/master/library/src/main/java/me/weishu/reflection/Reflection.java
    public static void enableDarkGreylistedApis(String... methods) throws InvocationTargetException, IllegalAccessException {
        //dalvik.system.VMRuntime sVmRuntime = dalvik.system.VMRuntime.getRuntime();
        Object sVmRuntime = getDoubleReflectedMethod("dalvik.system.VMRuntime", null, "getRuntime", null).invoke(null);

        //sVmRuntime.setHiddenApiExemptions(new Object[]{methods})
        getDoubleReflectedMethod("dalvik.system.VMRuntime", sVmRuntime, "setHiddenApiExemptions", new Class[]{String[].class})
                .invoke(sVmRuntime, new Object[]{methods});
    }

    //Meta-reflection. Has science gone too far?
    @SuppressWarnings("rawtypes")
    public static Method getDoubleReflectedMethod(String className, Object instance, String method, Class[] methodArgs) throws InvocationTargetException, IllegalAccessException {
        return (Method) getDeclaredMethod.invoke(forName.invoke(instance, className), method, methodArgs);
    }

    public static void setPrivateField(Class<?> c, Object instance, String fieldName, Object value) {
        try {
            Field field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getPrivateField(Class<?> c, Object instance, String fieldName) {
        try {
            Field field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("DiscouragedApi")
    public static CharSequence getStringResourceFromApplication(
            PackageManager packageManager,
            String packageName,
            String identifier,
            CharSequence defaultValue) {
        CharSequence value;

        try {
            int resId = packageManager
                    .getResourcesForApplication(packageName)
                    .getIdentifier(packageName + ":string/" + identifier, null, null);

            value = resId == 0 ? null : packageManager.getText(packageName, resId, null);
        } catch (PackageManager.NameNotFoundException e) {
            value = null;
        }

        return value == null ? defaultValue : value;
    }
}
