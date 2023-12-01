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

import android.util.SparseArray;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.nikita22007.multiplayer.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class Speck  {

	public static final int HEALING		= 0;
	public static final int STAR		= 1;
	public static final int LIGHT		= 2;
	public static final int QUESTION	= 3;
	public static final int UP			= 4;
	public static final int SCREAM		= 5;
	public static final int BONE		= 6;
	public static final int WOOL		= 7;
	public static final int ROCK		= 8;
	public static final int NOTE		= 9;
	public static final int CHANGE		= 10;
	public static final int HEART		= 11;
	public static final int BUBBLE		= 12;
	public static final int STEAM		= 13;
	public static final int COIN		= 14;
	
	public static final int DISCOVER	= 101;
	public static final int EVOKE		= 102;
	public static final int MASTERY		= 103;
	public static final int KIT			= 104;
	public static final int RATTLE		= 105;
	public static final int JET			= 106;
	public static final int TOXIC		= 107;
	public static final int PARALYSIS	= 108;
	public static final int DUST		= 109;
	public static final int FORGE		= 110;
	public static final int CONFUSION	= 111;
	
	private static final SparseArray<Emitter.Factory> factories = new SparseArray<Emitter.Factory>();
	
	public Speck() {
		super();
	}

	public static Emitter.Factory factory( final int type ) {
		return factory( type, false );
	}
	
	public static Emitter.Factory factory( final int type, final boolean lightMode ) {
		
		Emitter.Factory factory = factories.get( type );
		
		if (factory == null) {
			factory = new Emitter.Factory() {
				@Override
				public boolean lightMode() {
					return lightMode;
				}

				@Override
				public String factoryName() {
					return "speck";
				}

				@Override
				public JSONObject customParams() {
					JSONObject obj =  super.customParams();
					try {
						obj.put("type", type);
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}
					return obj;
				}
			};
			factories.put( type, factory );
		}
		
		return factory;
	}
}
