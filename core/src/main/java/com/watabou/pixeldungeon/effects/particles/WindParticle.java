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
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.nikita22007.multiplayer.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.nikita22007.multiplayer.noosa.particles.Emitter.Factory;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WindParticle extends PixelParticle {

	public static final Emitter.Factory FACTORY = new Factory() {

        @Override
		public String factoryName() {
			return "wind";
		}
	};
	public static class Wind extends Group {
		
		private int pos;
		
		private float x;
		private float y;
		
		private float delay;
		
		public Wind( int pos ) {
			super();
			
			this.pos = pos;
			PointF p = DungeonTilemap.tileToWorld( pos );
			x = p.x;
			y = p.y;
			
			delay = Random.Float( 5 );
		}
		
		@Override
		public void update() {

		}
	}
}