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
package com.nikita22007.multiplayer.server.ui;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.network.SendData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Banner {

    public static void show(@NotNull Hero actor, @NotNull BannerSprites.Type banner, int color, float fadeTime, float showTime) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("action_type", "show_banner");
            obj.put("banner", banner.toString().toLowerCase(Locale.ROOT));
            obj.put("color", color);
            obj.put("fade_time", fadeTime);
            obj.put("show_time", showTime);
        } catch (JSONException ignored) {

        }
        SendData.sendCustomAction(obj, actor);
    }

    public static void show(@NotNull Hero actor, @NotNull BannerSprites.Type banner, int color, float fadeTime) {
        show(actor, banner, color, fadeTime, Float.MAX_VALUE);
    }
}
