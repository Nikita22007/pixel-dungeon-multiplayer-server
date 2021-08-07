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
package com.watabou.pixeldungeon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Light;
import com.watabou.pixeldungeon.actors.buffs.Rage;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.levels.CavesBossLevel;
import com.watabou.pixeldungeon.levels.CavesLevel;
import com.watabou.pixeldungeon.levels.CityBossLevel;
import com.watabou.pixeldungeon.levels.CityLevel;
import com.watabou.pixeldungeon.levels.DeadEndLevel;
import com.watabou.pixeldungeon.levels.HallsBossLevel;
import com.watabou.pixeldungeon.levels.HallsLevel;
import com.watabou.pixeldungeon.levels.LastLevel;
import com.watabou.pixeldungeon.levels.LastShopLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.PrisonBossLevel;
import com.watabou.pixeldungeon.levels.PrisonLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.SewerBossLevel;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.StartScene;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndResurrect;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.watabou.pixeldungeon.HeroHelp.getHeroID;
import static com.watabou.pixeldungeon.network.SendData.addToSendHeroVisibleCells;
import static com.watabou.pixeldungeon.network.SendData.addToSendLevelVisitedState;

public class Dungeon {
	
	public static int potionOfStrength;
	public static int scrollsOfUpgrade;
	public static int scrollsOfEnchantment;
	public static boolean dewVial;		// true if the dew vial can be spawned
	
	public static int challenges;
	
	public static Hero[] heroes;
	public static Level level;
	
	public static int depth;
	//public static int gold;
	// Reason of death
	public static String resultDescription;

	// Hero's field of view
	public static boolean[] visible = new boolean[Level.LENGTH];
	
	public static boolean nightMode;
	
	public static SparseArray<ArrayList<Item>> droppedItems;

	public static void init() {
		//TODO SETTING MENU
		Settings.maxPlayers=4;//
		Dungeon.heroes=new Hero[Settings.maxPlayers];


		challenges = PixelDungeon.challenges();
		
		Actor.clear();
		
		PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );
		
		Scroll.initLabels();
		Potion.initColors();
		Wand.initWoods();
		Ring.initGems();
		
		Statistics.reset();
		Journal.reset();
		
		depth = 0;
		
		droppedItems = new SparseArray<ArrayList<Item>>();
		
		potionOfStrength = 0;
		scrollsOfUpgrade = 0;
		scrollsOfEnchantment = 0;
		dewVial = true;
		
		Ghost.Quest.reset();
		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();
		
		Room.shuffleTypes();
		
		QuickSlot.primaryValue = null;
		QuickSlot.secondaryValue = null;

		Badges.reset();

	}
	
	public static boolean isChallenged( int mask ) {
		return (challenges & mask) != 0;
	}

	public static Level newLevel() {

		Dungeon.level = null;
		Actor.clear();

		depth++;
		if (depth > Statistics.deepestFloor) {
			Statistics.deepestFloor = depth;
			
			if (Statistics.qualifiedForNoKilling) {
				Statistics.completedWithNoKilling = true;
			} else {
				Statistics.completedWithNoKilling = false;
			}
		}
		
		Arrays.fill( visible, false );
		
		Level level;
		switch (depth) {
		case 1:
		case 2:
		case 3:
		case 4:
			level = new SewerLevel();
			break;
		case 5:
			level = new SewerBossLevel();
			break;
		case 6:
		case 7:
		case 8:
		case 9:
			level = new PrisonLevel();
			break;
		case 10:
			level = new PrisonBossLevel();
			break;
		case 11:
		case 12:
		case 13:
		case 14:
			level = new CavesLevel();
			break;
		case 15:
			level = new CavesBossLevel();
			break;
		case 16:
		case 17:
		case 18:
		case 19:
			level = new CityLevel();
			break;
		case 20:
			level = new CityBossLevel();
			break;
		case 21:
			level = new LastShopLevel();
			break;
		case 22:
		case 23:
		case 24:
			level = new HallsLevel();
			break;
		case 25:
			level = new HallsBossLevel();
			break;
		case 26:
			level = new LastLevel();
			break;
		default:
			level = new DeadEndLevel();
			Statistics.deepestFloor--;
		}
		
		level.create();
		
		Statistics.qualifiedForNoKilling = !bossLevel(depth);
		
		return level;
	}
	
	public static void resetLevel() {
		
		Actor.clear();
		
		Arrays.fill( visible, false );
		
		level.reset();
		switchLevelToAll( level, level.entrance );
	}
	
	public static boolean shopOnLevel(int depth) {
		return depth == 6 || depth == 11 || depth == 16;
	}

	public static boolean bossLevel( int depth ) {
		return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25;
	}
	
	//@SuppressWarnings("deprecation")

	public static int GetPosNear(int pos)
	{
		for (int step:level.NEIGHBOURS9) {
			if (Actor.findChar(pos+step)==null){
				return pos+step;
			}
		}
		return -1;
	}

	public static void removeHero(int ID){
		Actor.remove(heroes[ID]);
		Actor.freeCell(heroes[ID].pos);
		heroes[ID].die(null);
	}

	public static void switchLevelToAll(final Level level,int pos ){
        switchLevel(level);
        for (Hero hero:heroes) {
            if (hero!=null){
                swichLevelChangePosition(pos,hero);
            }
        }
	}

	private static void swichLevelChangePosition(int pos, @NotNull Hero hero)
    {
        hero.pos = pos != -1 ? (Level.getNearClearCell(pos)) : Level.getNearClearCell(level.exit);

        Light light = hero.buff( Light.class );
        hero.viewDistance = light == null ? level.viewDistance : Math.max( Light.DISTANCE, level.viewDistance );

        observe(hero);
    }
	public static void switchLevel(final Level level, int pos, @NotNull Hero hero ) {
		switchLevel(level);
        swichLevelChangePosition(pos,hero);
	}

	public static void switchLevel(final Level level) {
		//todo rewrite
		//todo add cheking Hero pos is  clear
		if(Dungeon.level==level){return;}

		nightMode = new Date().getHours() < 7;

		Dungeon.level = level;
		Actor.init();

		Actor respawner = level.respawner();
		if (respawner != null) {
			Actor.add( level.respawner() );
		}
	}

	public static void dropToChasm( Item item ) {
		int depth = Dungeon.depth + 1;
		ArrayList<Item> dropped = (ArrayList<Item>)Dungeon.droppedItems.get( depth );
		if (dropped == null) {
			Dungeon.droppedItems.put( depth, dropped = new ArrayList<Item>() ); 
		}
		dropped.add( item );
	}
	
	public static boolean posNeeded() {
		int[] quota = {4, 2, 9, 4, 14, 6, 19, 8, 24, 9};
		return chance( quota, potionOfStrength );
	}
	
	public static boolean souNeeded() {
		int[] quota = {5, 3, 10, 6, 15, 9, 20, 12, 25, 13};
		return chance( quota, scrollsOfUpgrade );
	}
	
	public static boolean soeNeeded() {
		return Random.Int( 12 * (1 + scrollsOfEnchantment) ) < depth;
	}
	
	private static boolean chance( int[] quota, int number ) {
		
		for (int i=0; i < quota.length; i += 2) {
			int qDepth = quota[i];
			if (depth <= qDepth) {
				int qNumber = quota[i + 1];
				return Random.Float() < (float)(qNumber - number) / (qDepth - depth + 1);
			}
		}
		
		return false;
	}
	
	private static final String GAME_FILE = "game.dat";
	private static final String DEPTH_FILE = "depth%d.dat";

	/*
	private static final String WR_GAME_FILE	= "warrior.dat";
	private static final String WR_DEPTH_FILE	= "warrior%d.dat";
	
	private static final String MG_GAME_FILE	= "mage.dat";
	private static final String MG_DEPTH_FILE	= "mage%d.dat";
	
	private static final String RN_GAME_FILE	= "ranger.dat";
	private static final String RN_DEPTH_FILE	= "ranger%d.dat";

	private static final String HN_GAME_FILE	= "huntress.dat";
	private static final String HN_DEPTH_FILE	= "huntress%d.dat";
	*/
	private static final String VERSION		= "version";
	private static final String CHALLENGES	= "challenges";
	private static final String HERO		= "hero";
	private static final String GOLD		= "gold";
	private static final String DEPTH		= "depth";
	private static final String LEVEL		= "level";
	private static final String DROPPED		= "dropped%d";
	private static final String POS			= "potionsOfStrength";
	private static final String SOU			= "scrollsOfEnhancement";
	private static final String SOE			= "scrollsOfEnchantment";
	private static final String DV			= "dewVial";
	private static final String CHAPTERS	= "chapters";
	private static final String QUESTS		= "quests";
	private static final String BADGES		= "badges";

	private static String thisGameSaveFile;
	private static String thisGameDepthSaveFile;

	public static String gameFile( HeroClass cl ) {
	/*	switch (cl) {
		case WARRIOR:
			return WR_GAME_FILE;
		case MAGE:
			return MG_GAME_FILE;
		case ROGUE:
			return RN_GAME_FILE;
		case HUNTRESS:
			return HN_GAME_FILE;
		default:
			return GAME_FILE;
		}*/
		return GAME_FILE;
	}
	
	private static String depthFile( HeroClass cl ) {
		/*switch (cl) {
		case WARRIOR:
			return WR_DEPTH_FILE;
		case MAGE:
			return MG_DEPTH_FILE;
		case ROGUE:
			return RN_DEPTH_FILE;
		case HUNTRESS:
			return HN_DEPTH_FILE;
		default:
			return DEPTH_FILE;
		}*/
		return DEPTH_FILE;
	}
	
	public static void saveGame( String fileName ) throws IOException { //TODO FIX IT
		try {
			Bundle bundle = new Bundle();
			
			bundle.put( VERSION, Game.version );
			bundle.put( CHALLENGES, challenges );
			bundle.put( HERO, heroes[0] );
			bundle.put( GOLD, heroes[0].gold);
			bundle.put( DEPTH, depth );
			
			for (int d : droppedItems.keyArray()) {
				bundle.put( String.format( DROPPED, d ), droppedItems.get( d ) );
			}
			
			bundle.put( POS, potionOfStrength );
			bundle.put( SOU, scrollsOfUpgrade );
			bundle.put( SOE, scrollsOfEnchantment );
			bundle.put( DV, dewVial );

			Bundle quests = new Bundle();
			Ghost		.Quest.storeInBundle( quests );
			Wandmaker	.Quest.storeInBundle( quests );
			Blacksmith	.Quest.storeInBundle( quests );
			Imp			.Quest.storeInBundle( quests );
			bundle.put( QUESTS, quests );
			
			Room.storeRoomsInBundle( bundle );
			
			Statistics.storeInBundle( bundle );
			Journal.storeInBundle( bundle );
			
			QuickSlot.save( bundle );
			
			Scroll.save( bundle );
			Potion.save( bundle );
			Wand.save( bundle );
			Ring.save( bundle );
			
			Bundle badges = new Bundle();
			Badges.saveLocal( badges );
			bundle.put( BADGES, badges );
			
			OutputStream output = Game.instance.openFileOutput( fileName, Game.MODE_PRIVATE );
			Bundle.write( bundle, output );
			output.close();
			
		} catch (Exception e) {

			GamesInProgress.setUnknown( StartScene.curClass );
		}
	}
	
	public static void saveLevel() throws IOException {
		Bundle bundle = new Bundle();
		bundle.put( LEVEL, level );
		
		OutputStream output = Game.instance.openFileOutput( Utils.format( depthFile( StartScene.curClass), depth ), Game.MODE_PRIVATE );
		Bundle.write( bundle, output );
		output.close();
	}
	
	public static void saveAll() throws IOException { //fixme
		if (heroes[0]==null)  {
			return;
		}
		if (heroes[0].isAlive()) {
			
			Actor.fixTime();
			saveGame( gameFile( heroes[0].heroClass ) );
			saveLevel();
			
			GamesInProgress.set( heroes[0].heroClass, depth, heroes[0].lvl, challenges != 0 );
			
		} else if (WndResurrect.instance != null) {
			
			WndResurrect.instance.hide();
			/*Hero*/ heroes[0].reallyDie( WndResurrect.causeOfDeath );
			
		}
	}
	
	public static void loadGame( HeroClass cl ) throws IOException {
		loadGame( gameFile( cl ), true );
	}
	
	public static void loadGame( String fileName ) throws IOException {
		loadGame( fileName, false );
	}
	
	public static void loadGame( String fileName, boolean fullLoad ) throws IOException {
		
		Bundle bundle = gameBundle( fileName );
		
		Dungeon.challenges = bundle.getInt( CHALLENGES );
		
		Dungeon.level = null;
		Dungeon.depth = -1;
		
		if (fullLoad) {
			PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );
		}
		
		Scroll.restore( bundle );
		Potion.restore( bundle );
		Wand.restore( bundle );
		Ring.restore( bundle );
		
		potionOfStrength = bundle.getInt( POS );
		scrollsOfUpgrade = bundle.getInt( SOU );
		scrollsOfEnchantment = bundle.getInt( SOE );
		dewVial = bundle.getBoolean( DV );
		
		if (fullLoad) {
			
			Bundle quests = bundle.getBundle( QUESTS );
			if (!quests.isNull()) {
				Ghost.Quest.restoreFromBundle( quests );
				Wandmaker.Quest.restoreFromBundle( quests );
				Blacksmith.Quest.restoreFromBundle( quests );
				Imp.Quest.restoreFromBundle( quests );
			} else {
				Ghost.Quest.reset();
				Wandmaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
			}
			
			Room.restoreRoomsFromBundle( bundle );
		}
		
		Bundle badges = bundle.getBundle( BADGES );
		if (!badges.isNull()) {
			Badges.loadLocal( badges );
		} else {
			Badges.reset();
		}
		
		QuickSlot.restore( bundle );
		
		@SuppressWarnings("unused")
		String version = bundle.getString( VERSION );

		if (!fullLoad) {
			heroes=new Hero[1];
			heroes[0] = null; //TODO FIX LOAD
			heroes[0]= (Hero) bundle.get(HERO);

			QuickSlot.compress();

			heroes[0].gold = bundle.getInt(GOLD);
			depth = bundle.getInt(DEPTH);
		}
		Statistics.restoreFromBundle( bundle );
		Journal.restoreFromBundle( bundle );
		
		droppedItems = new SparseArray<ArrayList<Item>>();
		for (int i=2; i <= Statistics.deepestFloor + 1; i++) {
			ArrayList<Item> dropped = new ArrayList<Item>();
			for (Bundlable b : bundle.getCollection( String.format( DROPPED, i ) ) ) {
				dropped.add( (Item)b );
			}
			if (!dropped.isEmpty()) {
				droppedItems.put( i, dropped );
			}
		}
	}

	public static Level loadLevel( HeroClass cl ) throws IOException {

		Dungeon.level = null;
		Actor.clear();
		
		InputStream input = Game.instance.openFileInput( Utils.format( depthFile( cl ), depth ) ) ;
		Bundle bundle = Bundle.read( input );
		input.close();
		
		return (Level)bundle.get( "level" );
	}

	public static Level loadLevel() throws IOException {
		return loadLevel(StartScene.curClass);
	};

	public static void deleteGame( HeroClass cl, boolean deleteLevels ) {
		deleteGame(deleteLevels);
	}

	public static void deleteGame( boolean deleteLevels ) {
		
		Game.instance.deleteFile(GAME_FILE);
		
		if (deleteLevels) {
			int depth = 1;
			while (Game.instance.deleteFile( Utils.format( DEPTH_FILE, depth ) )) {
				depth++;
			}
		}
		
		GamesInProgress.delete( );
	}
	
	public static Bundle gameBundle( String fileName ) throws IOException {
		
		InputStream input = Game.instance.openFileInput( fileName );
		Bundle bundle = Bundle.read( input );
		input.close();
		
		return bundle;
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.depth = bundle.getInt( DEPTH );
		info.challenges = (bundle.getInt( CHALLENGES ) != 0);
		if (info.depth == -1) {
			info.depth = bundle.getInt( "maxDepth" );	// FIXME
		}
		Hero.preview( info, bundle.getBundle( HERO ) );
	}
	
	public static void fail( String desc ) { //todo rewritre it
		resultDescription = desc;
		for (Hero hero : heroes) {
			if (hero!=null &&  !hero.isAlive()) {
				if (hero.belongings.getItem(Ankh.class) == null) {
					Rankings.INSTANCE.submit(false);
				}
			}
		}
	}
	
	public static void win( String desc ) {
		for (Hero hero:heroes) {
		if  (hero!=null){
				hero.belongings.identify();
			}
		}
		
		if (challenges != 0) {
			Badges.validateChampion();
		}
		
		resultDescription = desc;
		Rankings.INSTANCE.submit( true );
	}
	
	public static void observeAll() {
		for (Hero hero:heroes) {
			if (hero!=null){
				observe(hero);
			}

		}
	}

	public static void observe(@NotNull Hero hero) {
	observe(hero, true);
	}

	public static void observe(@NotNull Hero hero, boolean send){

		if (level == null) {
			return;
		}

		level.updateFieldOfView( hero );
		System.arraycopy( Level.fieldOfView, 0, visible, 0, visible.length );

		BArray.or( level.visited, visible, level.visited );

		if (send) {
			int networkID = getHeroID(hero);
			addToSendLevelVisitedState(level,networkID);
			addToSendHeroVisibleCells(visible,networkID);
			SendData.flush(networkID);
		}
		//GameScene.afterObserve(); // todo client only

	}
	
	private static boolean[] passable = new boolean[Level.LENGTH];
	
	public static int findPath( Char ch, int from, int to, boolean pass[], boolean[] visible ) {
		
		if (Level.adjacent( from, to )) {
			return Actor.findChar( to ) == null && (pass[to] || Level.avoid[to]) ? to : -1;
		}
		
		if (ch.flying || ch.buff( Amok.class ) != null || ch.buff( Rage.class ) != null) {
			BArray.or( pass, Level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Level.LENGTH );
		}
		
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}
		
		return PathFinder.getStep( from, to, passable );
		
	}
	
	public static int flee( Char ch, int cur, int from, boolean pass[], boolean[] visible ) {
		
		if (ch.flying) {
			BArray.or( pass, Level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Level.LENGTH );
		}
		
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}
		passable[cur] = true;
		
		return PathFinder.getStepBack( cur, from, passable );
		
	}

}
