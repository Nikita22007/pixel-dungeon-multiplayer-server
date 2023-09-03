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

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DwarfToken;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class WndImp extends WndOptions {
	
	private static final String TXT_MESSAGE	= 
		"Oh yes! You are my hero!\n" +
		"Regarding your reward, I don't have cash with me right now, but I have something better for you. " +
		"This is my family heirloom ring: my granddad took it off a dead paladin's finger.";
	private static final String TXT_REWARD		= "Take the ring";
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP		= 2;

	@NotNull
	private final Imp imp;
	@NotNull
	private final DwarfToken tokens;

	public WndImp(@NotNull final Hero owner, @NotNull final Imp imp, @NotNull final DwarfToken tokens ) {
		super(owner);
		this.imp = imp;
		this.tokens = tokens;

		sendWnd(
				tokens.image(),
				null,
				Utils.capitalize(tokens.name()),
				null, TXT_MESSAGE,
				TXT_REWARD
		);

	}
	
	private void takeReward( Imp imp, DwarfToken tokens, Item reward, Hero hero ) {
		
		hide();
		
		tokens.detachAll( hero.belongings.backpack );

		reward.identify();
		if (reward.doPickUp( hero)) {
			GLog.i( Hero.TXT_YOU_NOW_HAVE, reward.name() );
		} else {
			Dungeon.level.drop( reward, imp.pos );
		}
		
		imp.flee(hero);
		
		Imp.Quest.complete();
	}
	@Override
	protected void onSelect(int index) {
		takeReward(imp, tokens, Imp.Quest.reward, getOwnerHero());
	}
}
