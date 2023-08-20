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
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.PointF;

public class CellEmitter {

	public static Emitter get( int cell ) {
		
		Emitter emitter = GameScene.emitter();
		assert emitter != null;
		emitter.cellPos( cell, DungeonTilemap.SIZE, DungeonTilemap.SIZE );
		
		return emitter;
	}
	
	@SuppressWarnings("IntegerDivisionInFloatingPointContext")
	public static Emitter center(int cell ) {
		Emitter emitter = GameScene.emitter();
		assert emitter != null;
		emitter.cellPosWithShift(cell, DungeonTilemap.SIZE/2, DungeonTilemap.SIZE/2);

		return emitter;
	}
	
	public static Emitter bottom( int cell ) {
		
		Emitter emitter = GameScene.emitter();
		assert emitter != null;
		emitter.cellPosWithShift(cell, 0, DungeonTilemap.SIZE, DungeonTilemap.SIZE, 0);
		
		return emitter;
	}
}
