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
package com.watabou.pixeldungeon.sprites;

import android.graphics.Bitmap;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.nikita22007.multiplayer.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class ItemSprite extends MovieClip {

	public static final int SIZE	= 16;
	
	private static final float DROP_INTERVAL = 0.4f;
	
	protected static TextureFilm film;
	
	public Heap heap;

	@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
	private ItemSpriteGlowing glowing;
	
	private float dropInterval;
	
	public ItemSprite() {
		this( ItemSpriteSheet.SMTH, null );
	}
	
	public ItemSprite( Item item ) {
		this( item.image(), item.glowing() );
	}
	
	public ItemSprite( int image, ItemSpriteGlowing glowing ) {
		super( Assets.ITEMS );
		
		if (film == null) {
			film = new TextureFilm( texture, SIZE, SIZE );
		}
		
		view( image, glowing );
	}
	
	public void originToCenter() {
		origin.set(SIZE / 2 );
	}
	
	public void link() {
		link( heap );
	}
	
	public void link( Heap heap ) {
		this.heap = heap;
		view( heap.image(), heap.glowing() );
		place( heap.pos );
	}
	
	@Override
	public void revive() {
		super.revive();
		
		speed.set( 0 );
		acc.set( 0 );
		dropInterval = 0;
		
		heap = null;
	}
	
	public PointF worldToCamera( int cell ) {
		final int csize = DungeonTilemap.SIZE;
		
		return new PointF(
			cell % Level.WIDTH * csize + (csize - SIZE) * 0.5f,
			cell / Level.WIDTH * csize + (csize - SIZE) * 0.5f
		);
	}
	
	public void place( int p ) {
		point( worldToCamera( p ) );
	}

	public ItemSprite view( int image, ItemSpriteGlowing glowing ) {
		frame( film.get( image ) );
		this.glowing = glowing;
		if (glowing == null) {
			resetColor();
		}
		return this;
	}
	
	@Override
	public void update() {
	}

	public static void dropEffects(Heap heap) {

		if (heap == null) {
			return;
		}

		if (heap.isEmpty()) {
			return;
		}

		boolean visible = Dungeon.visibleforAnyHero(heap.pos);
		boolean[] visibleForHeroes = Dungeon.visibleForHeroes(heap.pos);

		if (!visible) {
			return; //optimization
		}

		if (heap.peek() instanceof Gold) {
			CellEmitter.center(heap.pos).burst(Speck.factory(Speck.COIN), 5);
			for (int ID = 0; ID < visibleForHeroes.length; ID++) {
				Sample.INSTANCE.play(Assets.SND_GOLD, 1, 1, Random.Float(0.9f, 1.1f));
			}
		} else {
			boolean water = Level.water[heap.pos];
			if (water) {
				GameScene.ripple(heap.pos);
			} else {
				int cell = Dungeon.level.map[heap.pos];
				water = (cell == Terrain.WELL || cell == Terrain.ALCHEMY);
			}
			for (int ID = 0; ID < visibleForHeroes.length; ID++) {
				if (visibleForHeroes[ID]) {
					Sample.INSTANCE.play(water ? Assets.SND_WATER : Assets.SND_STEP, 0.8f, 0.8f, 1.2f, Dungeon.heroes[ID]);
				}
			}
		}
	}

	public static int pick( int index, int x, int y ) {
		Bitmap bmp = TextureCache.get( Assets.ITEMS ).bitmap;
		int rows = bmp.getWidth() / SIZE;
		int row = index / rows;
		int col = index % rows;
		return bmp.getPixel( col * SIZE + x, row * SIZE + y );
	}

}
