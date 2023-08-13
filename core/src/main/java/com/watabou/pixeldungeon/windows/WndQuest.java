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
package com.watabou.pixeldungeon.windows;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WndQuest extends Window {

	public WndQuest(Hero owner, NPC questgiver, String text, String... options ) {
		super(owner);

		CharSprite questgiverSprite = questgiver.sprite();
		String title = Utils.capitalize( questgiver.name );

		JSONObject params = new JSONObject();
		try {
			params.put("title", title);
			params.put("text", text);
			params.put("sprite", questgiverSprite.spriteName());
			JSONArray optionsArr = new JSONArray();
			for (String option : options) {
				optionsArr.put(option);
			}
			params.put("options", optionsArr);
		} catch (JSONException ignored) {}
		SendData.sendWindow(owner.networkID, "wnd_quest", getId(), params);
	}
	
	protected void onSelect( int index ) {};
}
