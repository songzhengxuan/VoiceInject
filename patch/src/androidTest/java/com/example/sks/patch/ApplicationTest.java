package com.example.sks.patch;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.example.sks.voiceinject.ScriptEngine;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public static final String paramString = "{\"method\":\"clickLongOnText\", " +
            "\"params\":\"String\", \"param0\":\"Hello\"}";

    public static final String paramString1 = "{\"method\":\"clickLongOnText\", " +
            "\"params\":\"String, Integer\", \"param0\":\"Hello\", \"param1\":0}\n";

    public void testParam() throws JSONException {
        final String cmdJson = paramString;
        JSONObject object = new JSONObject(cmdJson);
        String methodName = object.getString("method");
        String params = object.getString("params");
        assertEquals("clickLongOnText", methodName);
        assertNotNull(params);
        List<Object> paraValues = ScriptEngine.getParamValuesFromCmdJson(object);
        assertEquals(1, paraValues.size());
        assertEquals("Hello", paraValues.get(0));
    }

    public void testParam1() throws JSONException {
        final String cmdJson = paramString1;
        JSONObject object = new JSONObject(cmdJson);
        String methodName = object.getString("method");
        String params = object.getString("params");
        assertEquals("clickLongOnText", methodName);
        assertNotNull(params);
        List<Object> paraValues = ScriptEngine.getParamValuesFromCmdJson(object);
        assertEquals(2, paraValues.size());
        assertEquals("Hello", paraValues.get(0));
        assertEquals(0, paraValues.get(1));
    }
}