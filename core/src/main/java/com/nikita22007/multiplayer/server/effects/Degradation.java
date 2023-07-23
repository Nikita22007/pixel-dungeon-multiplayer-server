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
package com.nikita22007.multiplayer.server.effects;

import com.watabou.pixeldungeon.network.SendData;
import com.watabou.utils.PointF;

import org.json.JSONException;
import org.json.JSONObject;

import static com.nikita22007.multiplayer.utils.Utils.putToJSONArray;

public class Degradation {

	private static final int[] WEAPON = {
		+2, -2,
		+1, -1,
		 0,  0,
		-1, +1,
		-2, +2,
		-2,  0,
		 0, +2
	};
	
	private static final int[] ARMOR = {
		-2, -1,
		-1, -1,
		+1, -1,
		+2, -1,
		-2,  0,
		-1,  0,
		 0,  0,
		+1,  0,
		+2,  0,
		-1, +1,
		+1, +1,
		-1, +2,
		 0, +2,
		+1, +2
	};
	
	private static final int[] RING = {
		 0, -1,
		-1,  0,
		 0,  0,
		+1,  0,
		-1, +1,
		+1, +1,
		-1, +2,
		 0, +2,
		+1, +2
	};
	
	private static final int[] WAND = {
		+2, -2,
		+1, -1,
		 0,  0,
		-1, +1,
		-2, +2,
		+1, -2,
		+2, -1
	};

	public static void weapon( PointF p ) {
		Degradation( p, WEAPON );
	}

	public static void armor( PointF p ) {
		Degradation( p, ARMOR );
	}

	public static void ring( PointF p ) {
		Degradation( p, RING );
	}

	public static void wand( PointF p ) {
		Degradation( p, WAND );
	}

	private static void Degradation(PointF p, int[] matrix ) {
		JSONObject action = new JSONObject();
		try {
			action.put("action_type", "degradation");
			action.put("position_x", p.x);
			action.put("position_y", p.y);
			action.put("matrix", putToJSONArray(matrix));
		} catch (JSONException ignored) {
		}
		SendData.sendCustomActionForAll(action);
	}

    public static class Speck {
        private static final int COLOR = 0xFF4422;
        private static final int SIZE = 3;
    }
}
