package com.nikita22007.multiplayer.noosa;

import com.watabou.pixeldungeon.network.SendData;

import org.json.JSONException;
import org.json.JSONObject;

public class Camera {
    public static void shake(float magnitude, float duration) {
        JSONObject actionObj = new JSONObject();
        try {
            actionObj.put("action_type", "shake_camera");
            actionObj.put("magnitude", magnitude);
            actionObj.put("duration", duration);
        } catch (JSONException ignored) {
        }
        SendData.sendCustomActionForAll(actionObj);
    }
}
