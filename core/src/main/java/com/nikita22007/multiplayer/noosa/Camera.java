package com.nikita22007.multiplayer.noosa;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.network.SendData;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class Camera {
    public static void shake(float magnitude, float duration) {
        shake(magnitude, duration, null);
    }

    public static void shake(float magnitude, float duration, @Nullable Hero heroForVisual) {
        JSONObject actionObj = new JSONObject();
        try {
            actionObj.put("action_type", "shake_camera");
            actionObj.put("magnitude", magnitude);
            actionObj.put("duration", duration);
        } catch (JSONException ignored) {
        }
        if (heroForVisual != null) {
            SendData.sendCustomAction(actionObj, heroForVisual);
        } else {
            SendData.sendCustomActionForAll(actionObj);
        }
    }
}
