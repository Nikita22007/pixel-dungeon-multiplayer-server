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
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WndChooseWay extends Window {
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	private static final float GAP		= 2;
	private static final String TXT_REMASTERY	= "Do you want to respec into %s?";
	private static final String TXT_MASTERY	= "Which way will you follow?";

	private final TomeOfMastery tome;
	private final HeroSubClass way1, way2;

	public WndChooseWay(final Hero owner, final TomeOfMastery tome, final HeroSubClass way1, final HeroSubClass way2 ) {
		
		super(owner);

		this.tome = tome;
		this.way1 = way1;
		this.way2 = way2;

		final String TXT_CANCEL		= "I'll decide later";
		
		JSONObject params = createCommonStuff( tome, way1.desc() + "\n\n" + way2.desc() + "\n\n" + TXT_MASTERY );


		JSONArray optionsArr = new JSONArray();
		optionsArr.put(way1.title());
		optionsArr.put(way2.title());
		optionsArr.put(TXT_CANCEL);
		try {
			params.put("options", optionsArr);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		SendData.sendWindow(owner.networkID, "wnd_choose_way", getId(), params);
	}
	
	public WndChooseWay( final Hero owner, final TomeOfMastery tome, final HeroSubClass way ) {

		super(owner);

		this.tome = tome;
		this.way1 = way;
		this.way2 = null;

		final String TXT_OK		= "Yes, I want to respec";
		final String TXT_CANCEL	= "Maybe later";
		
		JSONObject params = createCommonStuff( tome, way.desc() + "\n\n" + Utils.format( TXT_REMASTERY, Utils.indefinite( way.title() ) ) );

		JSONArray optionsArr = new JSONArray();
		optionsArr.put(TXT_OK);
		optionsArr.put(TXT_CANCEL);
		try {
			params.put("options", optionsArr);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		SendData.sendWindow(owner.networkID, "wnd_choose_way", getId(), params);
	}

	@NotNull
	private JSONObject createCommonStuff(@NotNull TomeOfMastery tome, @NotNull String text ) {
		JSONObject obj = new JSONObject();

		try {
			obj.put("title", tome.name());
			obj.put("title_icon", tome.image());
			obj.put("message", text);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return obj;
	}

	protected void onSelect(int index) {
		hide();
		if (index == 0) {
			tome.choose(way1);
		}
		if (index == 1) {
			if (way2 != null) {
				tome.choose(way2);
			}
		}
	};

}
