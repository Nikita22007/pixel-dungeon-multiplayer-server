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

import com.watabou.pixeldungeon.sprites.CharSprite;

public class Halo  {

	protected static final int RADIUS	= 64;
	protected float radius = RADIUS;
	protected float brightness = 1;
	protected int color;
	float x,y;
	CharSprite target;

	public Halo(CharSprite target) {
		super();
		this.target = target;
	}
	
	public Halo(CharSprite target, float radius, int color, float brightness ) {

		this(target);
		
		hardlight( color );
		this.brightness = brightness;
		radius( radius );
	}

	private void hardlight(int color) {
		this.color = color;
	}

	public Halo point( float x, float y ) {
		this.x = x - RADIUS;
		this.y = y - RADIUS;
		return this;
	}
	
	public void radius( float value ) {
		this.radius = value ;
	}

	public final void putOut()
	{
		//todo
	}
}
