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
package com.watabou.pixeldungeon.effects;

import com.nikita22007.multiplayer.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import static com.watabou.utils.PointF.PI;

public class Splash {
	
	public static void at( int cell, final int color, int n ) {
		at( DungeonTilemap.tileCenterToWorld( cell ), color, n );
	}
	
	public static void at( PointF p, final int color, int n ) {
		
		if (n <= 0) {
			return;
		}
		
		Emitter emitter = GameScene.emitter();
		emitter.pos( p );
		
		FACTORY.color = color;
		FACTORY.dir = -PI / 2;
		FACTORY.cone = PI;
		emitter.burst( FACTORY, n );
	}

	public static void at( PointF p, final float dir, final float cone, final int color, int n ) {
		
		if (n <= 0) {
			return;
		}
		
		Emitter emitter = GameScene.emitter();
		emitter.pos( p );
		
		FACTORY.color = color;
		FACTORY.dir = dir;
		FACTORY.cone = cone;
		emitter.burst( FACTORY, n );
	}
	
	private static final SplashFactory FACTORY = new SplashFactory(); 
			
	private static class SplashFactory extends Emitter.Factory {

		public int color;
		public float dir;
		public float cone;

		@Override
		public String factoryName() {
			return "splash";
		}

		@Override
		public JSONObject customParams() {
			JSONObject object = super.customParams();
			try {
				object.put("color", color);
				object.put("dir", dir);
				object.put("cone", cone);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			return object;
		}
	}
}
