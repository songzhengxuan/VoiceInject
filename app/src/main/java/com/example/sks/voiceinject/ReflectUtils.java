package com.example.sks.voiceinject;

import java.lang.reflect.Method;

/**
 * Created by sks on 2016/8/20.
 */
public class ReflectUtils {
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>...paramters)  {
        try {
            Method method = clazz.getMethod(methodName, paramters);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
