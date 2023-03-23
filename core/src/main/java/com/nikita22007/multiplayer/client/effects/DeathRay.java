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
package com.nikita22007.multiplayer.client.effects;

import com.watabou.pixeldungeon.network.SendData;

import org.json.JSONException;
import org.json.JSONObject;

public class DeathRay {

    private static final float DURATION = 0.5f;

    public static void showDeathRayCentered(int start_pos, int end_pos) {
        JSONObject actionObj = new JSONObject();
        try {
            actionObj.put("action_type", "death_ray_centered_visual");
            actionObj.put("start_pos", start_pos);
            actionObj.put("end_pos", end_pos);
            actionObj.put("duration", DURATION);
        } catch (JSONException ignore) {
        }
        SendData.sendCustomActionForAll(actionObj);
    }

}
