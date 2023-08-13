/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
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

package com.nikita22007.multiplayer.noosa;

import android.graphics.RectF;

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.TextureFilm;

public class MovieClip {

	protected Animation curAnim;
	
	public boolean paused = false;
	public boolean visible = true;

	public Listener listener;
	
	public MovieClip() {
		super();
	}

	public void update() {
	}

	public boolean looping(){
		return curAnim != null && curAnim.looped;
	}
	
	public void play( Animation anim ) {
		play( anim, false );
	}

	public void play( Animation anim, boolean force ) {
		
		if (!force && (curAnim != null) && (curAnim == anim) && (curAnim.looped)) {
			return;
		}
		
		curAnim = anim;

		if (listener != null) {
			listener.onComplete(curAnim);
		}
	}
	
	public static class Animation {
		
		public float delay;
		public RectF[] frames;
		public boolean looped;
		
		public Animation( int fps, boolean looped ) {
			this.delay = 1f / fps;
			this.looped = looped;
		}
		
		public Animation frames( RectF... frames ) {
			this.frames = frames;
			return this;
		}
		
		public Animation frames(TextureFilm film, Object... frames ) {
			this.frames = new RectF[frames.length];
			for (int i=0; i < frames.length; i++) {
				this.frames[i] = film.get( frames[i] );
			}
			return this;
		}
		
		public Animation clone() {
			return new Animation( Math.round( 1 / delay ), looped ).frames( frames );
		}
	}
	
	public interface Listener {
		void onComplete( Animation anim );
	}

	//from Image and Visual

	public float width,height;
	public boolean flipHorizontal = false;

	public float alpha = 1f;

	public SmartTexture texture;
	protected RectF frame;

	public float alpha() {
		return alpha;
	}

	public void alpha(float alpha) {
		this.alpha = alpha;
	}

	public void texture( Object tx ) {
		texture = tx instanceof SmartTexture ? (SmartTexture)tx : TextureCache.get( tx );
		frame( new RectF( 0, 0, 1, 1 ) );
	}

	public void frame( RectF frame ) {
		this.frame = frame;

		width = frame.width() * texture.width;
		height = frame.height() * texture.height;
	}

	public void frame( int left, int top, int width, int height ) {
		frame( texture.uvRect( left, top, left + width, top + height ) );
	}

	public RectF frame() {
		return new RectF( frame );
	}

	public void kill() {
	}

	public void killAndErase(){
		kill();
	}

}
