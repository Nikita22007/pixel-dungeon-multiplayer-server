/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.watabou.pixeldungeon;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.PointF;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class DungeonTilemap{

	public static final int SIZE = 16;

	public static PointF tileToWorld( int pos ) {
		return new PointF( pos % Level.WIDTH, pos / Level.WIDTH  ).scale( SIZE );
	}
	
	public static PointF tileCenterToWorld( int pos ) {
		return new PointF( 
			(pos % Level.WIDTH + 0.5f) * SIZE, 
			(pos / Level.WIDTH + 0.5f) * SIZE );
	}
}
