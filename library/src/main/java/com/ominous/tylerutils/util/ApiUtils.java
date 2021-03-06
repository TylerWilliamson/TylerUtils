package com.ominous.tylerutils.util;

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
        Object sVmRuntime = getDoubleReflectedMethod("dalvik.system.VMRuntime",null,"getRuntime", null).invoke(null);

        //sVmRuntime.setHiddenApiExemptions(new Object[]{methods})
        getDoubleReflectedMethod("dalvik.system.VMRuntime",sVmRuntime,"setHiddenApiExemptions", new Class[]{String[].class})
                .invoke(sVmRuntime, new Object[]{methods});
    }

    //Meta-reflection. Has science gone too far?
    public static Method getDoubleReflectedMethod(String className, Object instance, String method, Class[] methodArgs) throws InvocationTargetException, IllegalAccessException {
        return (Method) getDeclaredMethod.invoke(forName.invoke(instance, className), method, methodArgs);
    }
}
