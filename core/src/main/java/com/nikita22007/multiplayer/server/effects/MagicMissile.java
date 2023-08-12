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

import com.nikita22007.multiplayer.noosa.particles.Emitter;
import com.watabou.pixeldungeon.network.SendData;

import org.json.JSONException;
import org.json.JSONObject;

public class MagicMissile extends Emitter {
	
	public static void blueLight(int from, int to) {
		Send("bluelight", from, to);
	}
	
	public static void fire(int from, int to)  {
		Send("fire", from, to);
	}
	
	public static void earth(int from, int to)  {
		Send("earth", from, to);
	}
	
	public static void purpleLight(int from, int to)  {
		Send("purplelight", from, to);
	}
	
	public static void whiteLight(int from, int to)  {
		Send("whitelight", from, to);
	}
	
	public static void wool(int from, int to)  {
		Send("wool", from, to);
	}
	
	public static void poison(int from, int to)  {
		Send("poison", from, to);
	}
	
	public static void foliage(int from, int to)  {
		Send("foliage", from, to);
	}
	
	public static void slowness(int from, int to)  {
		Send("slowness", from, to);
	}
	
	public static void force(int from, int to)  {
		Send("force", from, to);
	}
	
	public static void coldLight(int from, int to)  {
		Send("coldlight", from, to);
	}
	
	public static void shadow(int from, int to)  {
		Send("shadow", from, to);
	}
	
	protected static void Send(String type, int from, int to){
		JSONObject actionObj = new JSONObject();
		try {
			actionObj.put("action_type", "magic_missile");
		actionObj.put("type", type);
		actionObj.put("from", from);
		actionObj.put("to", to);
		} catch (JSONException ignored) {}
		SendData.sendCustomActionForAll(actionObj);
	}
}
