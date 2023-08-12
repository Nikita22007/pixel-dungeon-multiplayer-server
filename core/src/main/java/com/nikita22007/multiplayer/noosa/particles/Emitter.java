/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * Pixel Dungeon Multiplayer
 * Copyright (C) 2021-2023 Shaposhnikov Nikita
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

package com.nikita22007.multiplayer.noosa.particles;

import com.watabou.pixeldungeon.effects.particles.FlowParticle;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.PointF;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Emitter emits visual particles from random place in its area.
 * <p>
 * Emitter's area is Rect(x, y, width, height).
 * <p>
 * Emitter emits {@link #quantity} count of particles with {@link #interval} delay.
 * If {@code quantity == 0} emitter should be stopped manually */
public class Emitter {

	protected boolean lightMode = false;
	/**
	 * X position of left-top angle of emitter's area
	 */
	public float x;
	/**
	 * Y position of left-top angle of emitter's area
	 */
	public float y;
	/**
	 * Width of emitter's area
	 */
	public float width;
	/**
	 * Height of emitter's area
	 */
	public float height;

	/**
	 * If target != null, emitter's will use target's position instead of itself
	 */
	protected CharSprite target;
	/**
	 * If target != null, emitter's will use target's size instead of itself
	 */
	public boolean fillTarget = true;
	
	protected float interval;
	protected int quantity;

	/**
	 * if {@code on == false}, Emitter stops emitting
	 */
	public boolean on = false;

	/**
	 * Factory which producing particles
	 */
	protected Factory factory;
	private Integer cell = null;
	private PointF shift = new PointF(0, 0);


	public void cellPos(int cell) {
		cellPos(cell, 0, 0);
	}

	public void cellPos(int cell, float width, float height){
		this.cell = cell;
		this.width = width;
		this.height = height;
	}

	public void cellPosWithShift(int cell, float shiftX, float shiftY) {
		cellPosWithShift(cell, new PointF(shiftX, shiftY));
	}

	public void cellPosWithShift(int cell, PointF shift) {
		cellPosWithShift(cell, shift, 0,0);
	}

	public void cellPosWithShift(int cell, float shiftX,float shiftY, float width, float height) {
		cellPosWithShift(cell, new PointF(shiftX, shiftY), width, height);
	}

	public void cellPosWithShift(int cell, PointF shift, float width, float height) {
		this.cell = cell;
		this.shift = shift;
		this.width = width;
		this.height = height;
		target = null;
	}

	public void pos( PointF p ) {
		pos( p.x, p.y, 0, 0 );
	}
	
	public void pos( float x, float y, float width, float height ) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		target = null;
	}

	public void pos( CharSprite target ) {
		this.target = target;
	}

	public void pos( CharSprite target, PointF shift ) {
		this.target = target;
		this.shift = shift;
	}

	/**
	 * Emits {@code quantity} particles in one time
	 * @param factory factory of particles
	 * @param quantity count of particles
	 */
	public void burst( Factory factory, int quantity ) {
		start( factory, 0, quantity );
	}

	/**
	 * Emits particles each {@code interval} seconds. Should be stopped manually
	 * @param factory factory of particles
	 * @param interval interval between emitting
	 */
	public void pour( Factory factory, float interval ) {
		//todo
		//start( factory, interval, 0 );
	}

	/**
	 * Emits {@code quantity} particles each {@code interval} seconds
	 * @param factory factory of particles
	 * @param interval interval between emitting
	 */
	public void start( Factory factory, float interval, int quantity ) {
		
		this.factory = factory;
		this.lightMode = factory.lightMode();
		
		this.interval = interval;
		this.quantity = quantity;
		
		on = true;
		sendSelf();
	}

	protected void sendSelf() {
		JSONObject actionObj = new JSONObject();
		try {
			actionObj.put("action_type", "emitter_visual");

			if ((target != null) && (target.ch != null) && (target.ch.id() != -1)) {
				actionObj.put("target_char", target.ch.id());
				actionObj.put("fill_target", fillTarget);
			} else if (cell != null){
				actionObj.put("pos", cell);
			} else {
				actionObj.put("position_x", x);
				actionObj.put("position_y", y);
			}

			actionObj.put("shift_x", shift.x);
			actionObj.put("shift_y", shift.y);

			actionObj.put("width", width);
			actionObj.put("height", height);
			actionObj.put("factory", factory.toJsonObject());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		SendData.sendCustomActionForAll(actionObj);
	}

	abstract public static class Factory {

		public boolean lightMode() {
			return false;
		}

		public abstract String factoryName();

		public JSONObject customParams() {
			return new JSONObject();
		}

		public final JSONObject toJsonObject() {
			JSONObject result =  customParams();
			try {
			result = result == null? new JSONObject(): result;
			result.put("factory_type", factoryName());
				result.put("light_mode", lightMode());
			} catch (JSONException ignored) {}
			return result;
		}
	}
}
