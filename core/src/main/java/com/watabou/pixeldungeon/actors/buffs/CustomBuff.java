package com.watabou.pixeldungeon.actors.buffs;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

public class CustomBuff extends Buff{
    private int icon = 0;

    private String desc = "unknown";

    public CustomBuff(JSONObject obj) throws JSONException {
        buff_id = obj.getInt("id");
        update(obj);
    }

    public void update(JSONObject obj) throws JSONException {
        setIcon(obj.optInt("icon", icon));
        setDesc(obj.optString("desc", desc));
    }

    public int icon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    @Override
    public String toString()  {
        return desc;
    }

}
