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
package com.watabou.pixeldungeon.scenes;

import com.nikita22007.multiplayer.client.ui.Banner;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.EmoIcon;
import com.watabou.pixeldungeon.effects.Ripple;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.network.Server;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.DiscardedItemSprite;
import com.watabou.pixeldungeon.sprites.HeroSprite;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.StatusPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.WndBag;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.watabou.noosa.Game.timeTotal;

public class GameScene extends PixelScene {     //only client, exclude static
	
	private static final String TXT_WELCOME			= "Welcome to the level %d of Pixel Dungeon!";
	private static final String TXT_WELCOME_BACK	= "Welcome back to the level %d of Pixel Dungeon!";
	private static final String TXT_NIGHT_MODE		= "Be cautious, since the dungeon is even more dangerous at night!";
	
	private static final String TXT_CHASM	= "Your steps echo across the dungeon.";
	private static final String TXT_WATER	= "You hear the water splashing around you.";
	private static final String TXT_GRASS	= "The smell of vegetation is thick in the air.";
	private static final String TXT_SECRETS	= "The atmosphere hints that this floor hides many secrets.";
	
	static GameScene scene;
	
	private SkinnedBlock water;
	private DungeonTilemap tiles;
	

	//graphics
	private Group terrain = new Group();
	private Group ripples;
	private Group plants;
	private Group heaps;
	private Group mobs;
	private Group emitters;
	private Group effects;
	private Group gases;
	private Group spells;
	private Group emoicons;

	@Override
	public void create() {
		super.create();
		init();

		scene = this;

		Server.startServerStepLoop();

		fadeIn();
		//todo
	}
	public void init() {
		Music.INSTANCE.play( Assets.TUNE, true );
		Music.INSTANCE.volume( 1f );
		
		//PixelDungeon.lastClass( Dungeon.hero.heroClass.ordinal() );

		Camera.main.zoom( defaultZoom + PixelDungeon.zoom() );
		

		add( terrain );
		
		water = new SkinnedBlock( 
			Level.WIDTH * DungeonTilemap.SIZE, 
			Level.HEIGHT * DungeonTilemap.SIZE,
			Dungeon.level.waterTex() );
		terrain.add( water );
		
		ripples = new Group();
		terrain.add( ripples );
		
		tiles = new DungeonTilemap();
		terrain.add( tiles );
		
		Dungeon.level.addVisuals( this );
		
		plants = new Group();
		add( plants );

		heaps = new Group();
		add( heaps );
		
		int size = Dungeon.level.heaps.size();
		for (int i=0; i < size; i++) {
			addHeapSprite( Dungeon.level.heaps.valueAt( i ) );
		}

		emitters = new Group();
		effects = new Group();
		emoicons = new Group();

		mobs = new Group();
		add( mobs );
		
		for (Mob mob : Dungeon.level.mobs) {
			addMobSprite( mob );
			if (Statistics.amuletHeroID>-1) {
				mob.beckon( Dungeon.heroes[Statistics.amuletHeroID].pos );
			}
		}
		
		add( emitters );
		add( effects );
		
		gases = new Group();
		add( gases );
		
		for (Blob blob : Dungeon.level.blobs.values()) {
			blob.emitter = null;
			addBlobSprite( blob );
		}
		
		spells = new Group();
		add( spells );

		add( emoicons );

		for ( Hero heroobj:Dungeon.heroes) {
			if (heroobj == null){
				continue;
			}
			HeroSprite hero;
			hero = new HeroSprite(heroobj);
			hero.place( heroobj.pos );
			hero.updateArmor();
			mobs.add( hero );
		}

		StatusPane sb = new StatusPane();
		sb.camera = uiCamera;
		sb.setSize( uiCamera.width, 0 );
		add( sb );
		
		ArrayList<Item> dropped = Dungeon.droppedItems.get( Dungeon.depth );
		if (dropped != null) {
			for (Item item : dropped) {
				int pos = Dungeon.level.randomRespawnCell();
				if (item instanceof Potion) {
					((Potion)item).shatter( pos );
				} else if (item instanceof Plant.Seed) {
					Dungeon.level.plant( (Plant.Seed)item, pos );
				} else {
					Dungeon.level.drop( item, pos );
				}
			}
			Dungeon.droppedItems.remove( Dungeon.depth );
		}

	}
	
	public void destroy() {
		
		scene = null;
		Badges.saveGlobal();
		
		super.destroy();
	}
	
	@Override
	public synchronized void pause() {
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
		} catch (IOException e) {
			//
		}
	}

	public static final double PING_TIME = 2.0;
	private double lastPingTime = 0;
	@Override
	public synchronized void update() {

		super.update();

		Actor.process();

		if (timeTotal - lastPingTime >= PING_TIME) {
			for (Hero hero : Dungeon.heroes) {
				if (hero == null) {
					continue;
				}
				lastPingTime = timeTotal;
				hero.resendReady();
			}
		}

		for (Hero hero : Dungeon.heroes) {
			if (hero == null) {
				continue;
			}
			if (hero.cellSelector == null) {
				continue;
			}
			hero.cellSelector.enabled = hero.getReady();
		}

	}

	private void addHeapSprite( Heap heap ) {
		ItemSprite sprite = heap.sprite = (ItemSprite)heaps.recycle( ItemSprite.class );
		sprite.revive();
		sprite.link( heap );
		heaps.add( sprite );
	}
	
	private void addDiscardedSprite( Heap heap ) {
		heap.sprite = (DiscardedItemSprite)heaps.recycle( DiscardedItemSprite.class );
		heap.sprite.revive();
		heap.sprite.link( heap );
		heaps.add( heap.sprite );
	}
	
	private void addBlobSprite( final Blob gas ) {
		if (gas.emitter == null) {
			gases.add( new BlobEmitter( gas ) );
		}
	}
	
	private void addMobSprite( Mob mob ) {
		CharSprite sprite = mob.sprite();
		sprite.visible = Dungeon.visible[mob.pos];
		mobs.add( sprite );
		sprite.link( mob );
	}

	public void addHeroSprite(Hero hero){
		CharSprite sprite  = hero.getSprite();
		sprite.visible = true;
		mobs.add(sprite);
		sprite.link(hero);
	}

	// -------------------------------------------------------

	public static void add( Blob gas ) {
		Actor.add( gas );
		if (scene != null) {
			scene.addBlobSprite( gas );
		}
	}
	
	public static void add( Heap heap ) {
		if (scene != null) {
			scene.addHeapSprite( heap );
		}
	}
	
	public static void discard( Heap heap ) {
		if (scene != null) {
			scene.addDiscardedSprite( heap );
		}
	}

	public static void add( Mob mob ) {
		Dungeon.level.mobs.add( mob );
		Actor.add( mob );
		Actor.occupyCell( mob );
		scene.addMobSprite( mob );
	}
	
	public static void add( Mob mob, float delay ) {
		Dungeon.level.mobs.add( mob );
		Actor.addDelayed( mob, delay );
		Actor.occupyCell( mob );
		scene.addMobSprite( mob );
	}
	
	public static void add( EmoIcon icon ) {
		if (scene == null) {
			return;
		}
		if (scene.emoicons == null) {
			return;
		}
		scene.emoicons.add( icon );
	}
	
	public static void effect( Visual effect ) {
		scene.effects.add( effect );
	}
	
	public static Ripple ripple( int pos ) {
		Ripple ripple = (Ripple)scene.ripples.recycle( Ripple.class );
		ripple.reset( pos );
		return ripple;
	}
	
	public static SpellSprite spellSprite() {
		return (SpellSprite)scene.spells.recycle( SpellSprite.class );
	}
	
	public static Emitter emitter() {
		if (scene != null) {
			Emitter emitter = (Emitter)scene.emitters.recycle( Emitter.class );
			emitter.revive();
			return emitter;
		} else {
			return null;
		}
	}
	
	public static void pickUp( Item item ) { }
	
	public static void updateMap() {
		if (scene != null) {
			scene.tiles.updated.set( 0, 0, Level.WIDTH, Level.HEIGHT );
		}
	}
	
	public static void updateMap( int cell ) {
		if (scene != null) {
			scene.tiles.updated.union( cell % Level.WIDTH, cell / Level.WIDTH );
		}
	}
	
	public static void discoverTile( int pos, int oldValue ) {
		SendData.sendActionDiscoverTile(pos, oldValue);
	}
	
	public static void show( Window wnd ) {
		cancelCellSelector(wnd.getOwnerHero());
		if (wnd.getOwnerHero() == null) {
			scene.add(wnd);
		}
	}

	public static void flash( int color ) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("action_type", "game_scene_flash");
			obj.put("color", color);
			obj.put("light", true);
		} catch (JSONException ignored) {

		}
		SendData.sendCustomActionForAll(obj);
	}

	public static void gameOver(@NotNull Hero hero) {
		Banner.show(hero, BannerSprites.Type.GAME_OVER, 0x000000, 1f);
		Sample.INSTANCE.play(Assets.SND_DEATH);
	}

	public static void bossSlain() {
		for (Hero hero : Dungeon.heroes) {
			if (hero == null) {
				continue;
			}
			Banner.show(hero, BannerSprites.Type.BOSS_SLAIN, 0xFFFFFF, 0.3f, 5f);
		}
		Sample.INSTANCE.play(Assets.SND_BOSS);
	}

	public static void handleCell(Hero hero, int cell ) {
		hero.cellSelector.select( cell );
	}
	
	public static void selectCell( @NotNull Hero hero,  CellSelector.Listener listener ) {
		hero.cellSelector.listener = listener;
	}
	
	private static boolean cancelCellSelector(Hero hero) {
		if (hero == null) {
			return true;
		}
		if (hero.cellSelector.listener != null && hero.cellSelector.listener != hero.defaultCellListener) {
			hero.cellSelector.cancel();
			return true;
		} else {
			return false;
		}
	}
	
	public static WndBag selectItem( @NotNull Hero owner,WndBag.Listener listener, WndBag.Mode mode, String title ) {
		cancelCellSelector(owner);
		
		WndBag wnd = new WndBag(owner, listener, mode, title );
		scene.add( wnd );
		
		return wnd;
	}

	static boolean cancel(final Hero hero) {
		if (hero.curAction != null || hero.restoreHealth) {
			
			hero.curAction = null;
			hero.restoreHealth = false;
			return true;
			
		} else {
			
			return cancelCellSelector(hero);
			
		}
	}
	
	public static void ready(@NotNull Hero hero) {
		selectCell(hero, hero.defaultCellListener );
	}

		public String prompt() {
			return null;
		}

}
