/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.watabou.pixeldungeon.effects;

import android.util.Log;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.PointF;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.nikita22007.multiplayer.utils.Utils.putToJSONArray;

public class SpellSprite {

	public static final int FOOD		= 0;
	public static final int MAP			= 1;
	public static final int CHARGE		= 2;
	public static final int MASTERY		= 3;
	
	public static void show( Char ch, int index ) {

		if (ch == null) {
			Log.e("SpellSprite", "spell sprite target is null");
			return;
		}

		JSONObject action = new JSONObject();
		try {
			action.put("action_type", "spell_sprite");
			action.put("target", ch.id());
			action.put("spell", index);
		} catch (JSONException ignored) {
		}
		SendData.sendCustomActionForAll(action);
	}
}
