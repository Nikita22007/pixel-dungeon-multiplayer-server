/*
 * Pixel Dungeon
 * Copyright (C) 2021-2023 Nikita Shaposhnikov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.nikita22007.multiplayer.server.effects;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.network.SendData;

import org.json.JSONException;
import org.json.JSONObject;

public class Wound {

    private static final float TIME_TO_FADE = 0.8f;

    protected Wound() {
        throw new RuntimeException();
    }

    public static void reset(int p) {
        JSONObject actionObj = new JSONObject();
        try {
            actionObj.put("action_type", "wound_visual");
            actionObj.put("pos", p);
            actionObj.put("time_to_fade", TIME_TO_FADE);
        } catch (JSONException ignore) {
        }
        SendData.sendCustomActionForAll(actionObj);
    }

    public static void hit(Char ch) {
        hit(ch, 0);
    }

    public static void hit(Char ch, float angle) {
        reset(ch.pos);
    }

    public static void hit(int pos) {
        reset(pos);
    }
}
