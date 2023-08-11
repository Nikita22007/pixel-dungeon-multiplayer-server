/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
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
package com.watabou.pixeldungeon.effects;

import com.nikita22007.multiplayer.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.network.SendData;

public class Swap {
	
	public static void SwapChars( Char ch1, Char ch2 ) {
		SendData.addToSendCharSpriteAction(ch1.id(), "swap", ch1.pos, ch2.pos);
		SendData.addToSendCharSpriteAction(ch2.id(), "swap", ch2.pos, ch1.pos);
		Sample.INSTANCE.play( Assets.SND_TELEPORT );
		finish(ch1, ch2);
	}
	
	private static void finish(Char ch1, Char ch2) {
			int pos = ch1.pos;
			ch1.pos = ch2.pos;
			ch2.pos = pos;
			
			if (!ch1.flying) {
				if (ch1 instanceof Mob) {
					Dungeon.level.mobPress( (Mob)ch1 );
				} else {
					Dungeon.level.press( ch1.pos, ch1 );
				}
			}
			if (!ch2.flying) {
				if (ch2 instanceof Mob) {
					Dungeon.level.mobPress( (Mob)ch2 );
				} else {
					Dungeon.level.press( ch2.pos, ch2 );
				}
			}
			
			if (ch1 instanceof Hero || ch2 instanceof Hero) {
				Dungeon.observeAll();
			}
	}

	private Swap(){}
}
