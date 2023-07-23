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

import com.watabou.pixeldungeon.sprites.CharSprite;

import org.json.JSONObject;

public abstract class  EmoIcon {

	protected float maxSize = 2;
	protected float timeScale = 1;
	
	protected CharSprite owner;
	
	public EmoIcon( CharSprite owner ) {
		super();
		this.owner = owner;
	}

	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		try {
			json.put("max_size", maxSize);
			json.put("time_scale", timeScale);
			json.put("max_size", maxSize);
		} catch (Exception ignored) {
		}
		// todo: part of future, for ShatteredPD
		assert false;
		return new JSONObject();
	}

	public static class Sleep extends EmoIcon {
		
		public Sleep( CharSprite owner ) {
			
			super( owner );
			maxSize = 1.2f;
			timeScale = 0.5f;
		}

		@Override
		public JSONObject toJsonObject() {
			JSONObject json = new JSONObject();
			try {
				json.put("type", "default");
				json.put("emotion", "sleep");
			} catch (Exception ignored) {
			}
			return json;
		}
	}
	
	public static class Alert extends EmoIcon {
		
		public Alert( CharSprite owner ) {
			super( owner );
			maxSize = 1.3f;
			timeScale = 2;
		}

		@Override
		public JSONObject toJsonObject() {
			JSONObject json = new JSONObject();
			try {
				json.put("type", "default");
				json.put("emotion", "alert");
			} catch (Exception ignored) {
			}
			return json;
		}
	}

}
