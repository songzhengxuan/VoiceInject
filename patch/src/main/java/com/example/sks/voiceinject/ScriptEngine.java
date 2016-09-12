package com.example.sks.voiceinject;

import android.util.Log;

import com.robotium.solo.Solo;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by song on 16-9-5.
 */
public class ScriptEngine {
    static final String TAG = "voicectrl";

    private Class soloClass = Solo.class;
    /**
     * methodName to [paramsString: method] map
     */
    private HashMap<String, Method> cachedMethods = new HashMap<>();
    private Solo mSoloInstance;

    public ScriptEngine(Solo solo) {
        mSoloInstance = solo;
    }

    /**
     * start execute a script
     *      while execute line by line, all line must be executed
     * @return
     */
    public boolean startExecuteScript(String scriptSource) {
        BufferedReader reader = null;
        boolean done = false;
        boolean lastCmdRet = false;
        try {
            reader = new BufferedReader(new StringReader(scriptSource));
            String line = null;
            while ((line = reader.readLine()) != null) {
                lastCmdRet = executeCmd(line);
            }
            done = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeQuietly(reader);
        }
        if (!done) {
            return false;
        }
        return lastCmdRet;
    }

    /**
     * cmd line = JSON Object
     * {
     *  "method":"methodName",
     *  "params":"String, String, Integer"
     *  "param0": StringValue0
     *  "param1": StringValue1
     *  "param2": IntegerValue
     * }
     * @param cmdJson
     * @return
     */
    private synchronized boolean executeCmd(String cmdJson) {
        try {
            Log.e(TAG, "start executeCmd " + cmdJson);
            JSONObject object = new JSONObject(cmdJson);
            String methodName = object.getString("method");
            String params = object.getString("params");
            final String cacheKey = methodName + "#" + params;

            Method cachedMethod = cachedMethods.get(cacheKey);
            if (cachedMethod == null) {
                Class[] typeArray = getClassesFromTypeStrings(params);
                Method[] methods = soloClass.getMethods();
                for (Method aMethod : methods) {
                    if (methodName.equals(aMethod.getName())) {
                        Class[] aMethodParams = aMethod.getParameterTypes();
                        if (isTypeEquals(aMethodParams, typeArray)) {
                            cachedMethods.put(cacheKey, aMethod);
                            break;
                        }
                    }
                }
            }

            Method method = cachedMethods.get(cacheKey);
            Log.e(TAG, "find target method is " + method);
            int modifiers = method.getModifiers();
            Object receiver = null;
            if ((modifiers & Modifier.STATIC) == 0) {
                receiver = mSoloInstance;
            }
            Log.e(TAG, "method receiver is " + receiver);
            List<Object> paramList = getParamValuesFromCmdJson(object);
            Log.e(TAG, "paramValues is " + paramList);
            try {
                Object result = invokeMethod(method, receiver, paramList);
                Log.e(TAG, "invokeMethod result is " + result);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    static Object invokeMethod(Method method, Object receiver, List<Object> paramValues) throws
            InvocationTargetException, IllegalAccessException {
        Object result = null;
        if (paramValues == null && paramValues.size() == 0) {
            result = method.invoke(receiver);
        } else if (paramValues.size() == 1) {
            result = method.invoke(receiver, paramValues.get(0));
        } else if (paramValues.size() == 2) {
            result = method.invoke(receiver, paramValues.get(0), paramValues.get(1));
        } else if (paramValues.size() == 3) {
            result = method.invoke(receiver, paramValues.get(0), paramValues.get(1), paramValues.get
                    (2));
        } else if (paramValues.size() == 4) {
            result = method.invoke(receiver, paramValues.get(0), paramValues.get(1), paramValues.get
                    (2), paramValues.get(3));
        } else {
            throw new IllegalArgumentException("parameter values size exceeds max limit 3");
        }
        return result;
    }

    public static Class[] getClassesFromTypeStrings(String params) {
        String[] paramTypes = params.split(",");
        if (paramTypes == null) {
            return null;
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (String para : paramTypes) {
            if ("String".equalsIgnoreCase(para)) {
                classes.add(String.class);
            } else if ("Integer".equalsIgnoreCase(para)) {
                classes.add(Integer.class);
            } else if ("Long".equalsIgnoreCase(para)) {
                classes.add(Long.class);
            } else {
                throw new IllegalArgumentException("Unsupport paramtype");
            }
        }
        return classes.toArray(new Class[0]);
    }

    static boolean isTypeEquals(Class<?>[] arg1, Class<?>[] arg2) {
        return Arrays.equals(arg1, arg2);
    }

    public static List<Object> getParamValuesFromCmdJson(JSONObject cmdJson) throws JSONException {
        ArrayList<Object> result = new ArrayList<Object>();
        final String param = "param";
        final String paramTypeDesc = cmdJson.getString("params");
        Class[] paramTypes = getClassesFromTypeStrings(paramTypeDesc);
        if (paramTypes == null) {
            return result; // empty result
        }
        for (int i = 0; i < paramTypes.length; ++i) {
            result.add(getValueFromCmdJson(cmdJson, paramTypes[i], param + i));
        }
        return result;
    }

    public static Object getValueFromCmdJson(JSONObject cmdJson, Class paramClass, String key)
            throws JSONException {
        if (paramClass == String.class) {
            return cmdJson.getString(key);
        } else if (paramClass == Integer.class) {
            return cmdJson.getInt(key);
        } else if (paramClass == Long.class) {
            return cmdJson.getLong(key);
        } else {
            throw new IllegalArgumentException("Unsupport paramtype");
        }
    }
}
