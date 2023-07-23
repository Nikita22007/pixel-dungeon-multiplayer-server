/*
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

import com.watabou.pixeldungeon.network.SendData;

public final class FloatingText {
	public static void show( float x, float y, String text, int color ) {
		SendData.addToSendShowStatus(
				x,
				y,
				null,
				text,
				color,
				true
		);
	}
	
	public static void show( float x, float y, int key, String text, int color ) {
		SendData.addToSendShowStatus(
				x,
				y,
				key,
				text,
				color,
				true
		);
	}
}
