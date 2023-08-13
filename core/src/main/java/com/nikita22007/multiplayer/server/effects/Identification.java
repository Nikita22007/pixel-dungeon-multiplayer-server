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
package com.nikita22007.multiplayer.server.effects;

import com.watabou.utils.PointF;

public class Identification {

	private static final int COLOR = 0x4488CC;

	private static final int[] DOTS = {
		-1, -3,
		 0, -3,
		+1, -3,
		-1, -2,
		+1, -2,
		+1, -1,
		 0,  0,
		+1,  0,
		 0, +1,
		 0, +3
	};

	public static void showIdentification(PointF p){
		Degradation.showDegradation(p, DOTS, COLOR);
	}

	public Identification(PointF p) {
		showIdentification(p);
	}
}
