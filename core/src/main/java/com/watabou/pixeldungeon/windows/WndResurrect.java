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

import com.watabou.pixeldungeon.Rankings;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.scenes.InterLevelSceneServer;

public class WndResurrect extends WndOptions {
	
	private static final String TXT_MESSAGE	= "You died, but you were given another chance to win this dungeon. Will you take it?";
	private static final String TXT_YES		= "Yes, I will fight!";
	private static final String TXT_NO		= "No, I give up";
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final float GAP		= 2;

	public static Object causeOfDeath;
	
	public WndResurrect(final Ankh ankh, final Hero hero, Object causeOfDeath ) {
		super(hero);

		WndResurrect.causeOfDeath = causeOfDeath;

		sendWnd(
				ankh.image(),
				null,
				ankh.name(),
				null, TXT_MESSAGE,
				TXT_YES,
				TXT_NO
		);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
	
	@Override
	public void onBackPressed() {
	}

	@Override
	protected void onSelect(int index) {
		if (index == 1) {
			hide();
			Rankings.INSTANCE.submit( false );
			getOwnerHero().reallyDie( WndResurrect.causeOfDeath );
		} else {
			hide();
			Statistics.ankhsUsed++;
			InterLevelSceneServer.resurrect(getOwnerHero());

		}
	}
}
