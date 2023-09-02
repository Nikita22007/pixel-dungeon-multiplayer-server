/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.nikita22007.multiplayer.server.sprites;

import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.sprites.ItemSpriteGlowing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

import org.json.JSONException;
import org.json.JSONObject;

import static com.watabou.pixeldungeon.sprites.ItemSpriteSheet.BOOMERANG;
import static com.watabou.pixeldungeon.sprites.ItemSpriteSheet.CURARE_DART;
import static com.watabou.pixeldungeon.sprites.ItemSpriteSheet.DART;
import static com.watabou.pixeldungeon.sprites.ItemSpriteSheet.INCENDIARY_DART;
import static com.watabou.pixeldungeon.sprites.ItemSpriteSheet.JAVELIN;
import static com.watabou.pixeldungeon.sprites.ItemSpriteSheet.SHURIKEN;

public class MissileSprite {

	private static final float SPEED = 240f;

	public MissileSprite() {
	}

	public static void reset(int from, int to, Item item, Callback listener) {
		if (item == null) {
			reset(from, to, ItemSpriteSheet.SMTH, null, listener);
			GLog.n("Missile sprite of NULL item");
		} else {
			reset(from, to, item.image(), item.glowing(), listener);
		}
	}

	public static void reset(int from, int to, int image, ItemSpriteGlowing glowing, Callback listener) {
		float angularSpeed, angle; //degrees

		PointF start = DungeonTilemap.tileToWorld(from);
		PointF dest = DungeonTilemap.tileToWorld(to);

		PointF d = PointF.diff(dest, start);

		if (image == DART || image == INCENDIARY_DART || image == CURARE_DART || image == JAVELIN) {
			//no rotation while fly, use angle correction for sprite
			angularSpeed = 0;
			angle = 135 - (float) (Math.atan2(d.x, d.y) / 3.1415926 * 180);

		} else {
			//rotation in flight, SHURIKEN and BOOMERANG rotate twice faster
			angularSpeed = image == SHURIKEN || image == BOOMERANG ? 1440 : 720;
			angle = 0; //is not important
		}

		JSONObject action = new JSONObject();
		try {
			action.put("action_type", "missile_sprite_visual");
			action.put("from", from);
			action.put("to", to);
			action.put("speed", SPEED);
			action.put("angular_speed", angularSpeed);
			action.put("angle", angle);

			action.put("item_image", image);
			if (glowing != null){
				action.put("item_glowing", glowing.toJsonObject());
			} else {
				action.put("item_glowing", JSONObject.NULL);
			}
		} catch (JSONException ignore) {
		}
		SendData.sendCustomActionForAll(action);
		if (listener != null) {
			listener.call();
		}
	}
}
