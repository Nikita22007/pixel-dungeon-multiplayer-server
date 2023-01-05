package com.nikita22007.multiplayer.utils;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

public class Utils {
    public static JSONArray putToJSONArray(Object[] array) throws JSONException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new JSONArray(array);
        }
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        return jsonArray;
    }
    public static JSONArray putToJSONArray(int[] array) throws JSONException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new JSONArray(array);
        }
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        return jsonArray;
    }
}
