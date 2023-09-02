package com.watabou.pixeldungeon.sprites;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemSpriteGlowing {

    public static final ItemSpriteGlowing WHITE = new ItemSpriteGlowing(0xFFFFFF, 0.6f);

    public int color;
    public float red;
    public float green;
    public float blue;
    public float period;

    public ItemSpriteGlowing(int color) {
        this(color, 1f);
    }

    public ItemSpriteGlowing(int color, float period) {

        this.color = color;

        red = (color >> 16) / 255f;
        green = ((color >> 8) & 0xFF) / 255f;
        blue = (color & 0xFF) / 255f;

        this.period = period;
    }

    public JSONObject toJsonObject() {
        JSONObject result = new JSONObject();
        try {
            result.put("color", color);
            result.put("period", period);
        } catch (JSONException ignored) {
            return new JSONObject();
        }
        return result;

    }
}
