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
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;
import com.nikita22007.multiplayer.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.Warlock;
import com.nikita22007.multiplayer.server.effects.MagicMissile;
import com.watabou.utils.Callback;

public class WarlockSprite extends MobSprite {
	
	public WarlockSprite() {
		super();
		
		texture( Assets.WARLOCK );
		
		TextureFilm frames = new TextureFilm( texture, 12, 15 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );
		
		run = new Animation( 15, true );
		run.frames( frames, 0, 2, 3, 4 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 0, 5, 6 );
		
		zap = attack.clone();
		
		die = new Animation( 15, false );
		die.frames( frames, 0, 7, 8, 8, 9, 10 );
		
		play( idle );
	}
	
	public void zap( int cell ) {
		
		turnTo( ch.pos , cell );
		play( zap );
		
		MagicMissile.shadow( ch.pos, cell);
		Sample.INSTANCE.play( Assets.SND_ZAP );
		((Warlock)ch).onZapComplete();
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}
}
