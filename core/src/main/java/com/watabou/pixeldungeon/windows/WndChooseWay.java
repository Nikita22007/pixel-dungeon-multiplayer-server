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
import com.watabou.pixeldungeon.utils.Utils;

public class WndChooseWay extends WndOptions {

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

		String message =  way1.desc() + "\n\n" + way2.desc() + "\n\n" + TXT_MASTERY;
		sendWnd(
				tome.image(),
				null,
				tome.name(owner),
				null, message,
				way1.title(),
				way2.title(),
				TXT_CANCEL
		);

	}
	
	public WndChooseWay( final Hero owner, final TomeOfMastery tome, final HeroSubClass way ) {

		super(owner);

		this.tome = tome;
		this.way1 = way;
		this.way2 = null;

		final String TXT_OK		= "Yes, I want to respec";
		final String TXT_CANCEL	= "Maybe later";
		

		String message = way.desc() + "\n\n" + Utils.format( TXT_REMASTERY, Utils.indefinite( way.title() ));
				sendWnd(
						tome.image(),
				null,
				tome.name(owner),
						null, message,
				TXT_OK,
				TXT_CANCEL
		);
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
