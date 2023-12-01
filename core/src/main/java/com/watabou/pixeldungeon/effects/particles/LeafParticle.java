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
package com.watabou.pixeldungeon.effects.particles;

import com.nikita22007.multiplayer.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.nikita22007.multiplayer.noosa.particles.Emitter.Factory;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class LeafParticle extends PixelParticle.Shrinking {

	public static final Emitter.Factory GENERAL = new Factory() {

        @Override
		public String factoryName() {
			return "leaf";
		}

		@Override
		public JSONObject customParams() {
			JSONObject object = super.customParams();
			try {
				object.put("first_color", 0x004400);
				object.put("second_color", 0x88CC44);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			return object;
		}
	};
	
	public static final Emitter.Factory LEVEL_SPECIFIC = new Factory() {
        @Override
			public String factoryName() {
				return "leaf";
			}

			@Override
			public JSONObject customParams() {
				JSONObject object = super.customParams();
				try {
					object.put("first_color", Dungeon.level.color1);
					object.put("second_color", Dungeon.level.color2);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
				return object;
			}
	};

}