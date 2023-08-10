/*
 * Pixel Dungeon Multiplayer
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

import com.nikita22007.multiplayer.utils.Utils;
import com.nikita22007.multiplayer.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Lightning {

    private static final float DURATION = 0.3f;


    private Lightning() {
        throw new RuntimeException();
    }

    public static void showLightning(int[] cells, int length, Callback callback) {
        if (cells.length != length) {
            cells = Arrays.copyOfRange(cells, 0, length - 1);
        }
        showLightning(cells);

        Sample.INSTANCE.play(Assets.SND_LIGHTNING);

        if (callback != null) {
            callback.call();
        }
    }

    protected static void showLightning(int[] cells) {
        JSONObject actionObj = new JSONObject();
        try {
            actionObj.put("action_type", "lightning_visual");
            actionObj.put("cells", Utils.putToJSONArray(cells));
            actionObj.put("duration", DURATION);
        } catch (JSONException ignore) {
        }
        SendData.sendCustomActionForAll(actionObj);
    }
}
