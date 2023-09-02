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
package com.watabou.pixeldungeon.actors;

import com.nikita22007.multiplayer.noosa.Camera;
import com.nikita22007.multiplayer.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.actors.buffs.Light;
import com.watabou.pixeldungeon.actors.buffs.MindVision;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Shadows;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Speed;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.Door;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.RatSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Char extends Actor {

	protected static final String TXT_HIT		= "%s hit %s";
	protected static final String TXT_KILL		= "%s killed you...";
	protected static final String TXT_DEFEAT	= "%s defeated %s";
	
	private static final String TXT_YOU_MISSED	= "%s %s your attack";
	private static final String TXT_SMB_MISSED	= "%s %s %s's attack";
	
	private static final String TXT_OUT_OF_PARALYSIS	= "The pain snapped %s out of paralysis";
	public boolean[] fieldOfView = new boolean[Level.LENGTH];

	public int pos = 0;
	
	private CharSprite sprite = new RatSprite();
	
	public String name = "mob";
	
	private int HT;
	private int HP;
	
	protected float baseSpeed	= 1;
	
	public boolean paralysed	= false;
	public boolean rooted		= false;
	public boolean flying		= false;
	public int invisible		= 0;
	
	public int viewDistance	= 8;
	
	private Set<Buff> buffs = new CopyOnWriteArraySet<Buff>();
	
	@Override
	protected boolean act() {
		Dungeon.level.updateFieldOfView( this );
		return false;
	}
	
	private static final String POS			= "pos";
	private static final String TAG_HP		= "HP";
	private static final String TAG_HT		= "HT";
	private static final String BUFFS		= "buffs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		bundle.put( POS, pos );
		bundle.put( TAG_HP, getHP());
		bundle.put( TAG_HT, getHT());
		bundle.put( BUFFS, buffs );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		pos = bundle.getInt( POS );
		setHP(bundle.getInt( TAG_HP ));
		setHT(bundle.getInt( TAG_HT ));
		
		for (Bundlable b : bundle.getCollection( BUFFS )) {
			if (b != null) {
				((Buff)b).attachTo( this );
			}
		}
	}
	
	public boolean attack( Char enemy ) {

		boolean[] visibleFight = Dungeon.visibleForHeroes(pos, enemy.pos);
		
		if (hit( this, enemy, false )) {

			for (int i = 0; i < visibleFight.length; i++) {
				if (visibleFight[i]) {
					GLog.iWithTarget(i, TXT_HIT, name, enemy.name);
				}
			}
			
			// FIXME
			int dr = this instanceof Hero && ((Hero)this).rangedWeapon != null && ((Hero)this).subClass == HeroSubClass.SNIPER ? 0 :
				Random.IntRange( 0, enemy.dr() );
			
			int dmg = damageRoll();
			int effectiveDamage = Math.max( dmg - dr, 0 );
			
			effectiveDamage = attackProc( enemy, effectiveDamage );
			effectiveDamage = enemy.defenseProc( this, effectiveDamage );
			enemy.damage( effectiveDamage, this );


			for (int i = 0; i < visibleFight.length; i++) {
				if (visibleFight[i]) {
					Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ), Dungeon.heroes[i] );
				}
			}

			if (enemy instanceof Hero) {
				((Hero)enemy).interrupt();
				if (effectiveDamage > enemy.getHT() / 4) {
					Camera.shake( GameMath.gate( 1, effectiveDamage / (enemy.getHT() / 4), 5), 0.3f );
				}
			}
			
			enemy.getSprite().bloodBurstA( getSprite().center(), effectiveDamage );
			enemy.getSprite().flash();
			
			if (false) {//(!enemy.isAlive() && visibleFight) {
				if (enemy instanceof Hero) {
					
					if (((Hero)enemy).killerGlyph != null) {
						
					// FIXME
					//	Dungeon.fail( Utils.format( ResultDescriptions.GLYPH, Dungeon.hero.killerGlyph.name(), Dungeon.depth ) );
					//	GLog.n( TXT_KILL, Dungeon.hero.killerGlyph.name() );
						
					} else {
						if (Bestiary.isBoss( this )) {
							Dungeon.fail( Utils.format( ResultDescriptions.BOSS, name, Dungeon.depth ) );
						} else {
							Dungeon.fail( Utils.format( ResultDescriptions.MOB, 
								Utils.indefinite( name ), Dungeon.depth ) );
						}
						
						GLog.n( TXT_KILL, name );
					}
					
				} else {
					GLog.i( TXT_DEFEAT, name, enemy.name );
				}
			}
			
			return true;
			
		} else {
			String defense = enemy.defenseVerb();
			enemy.getSprite().showStatus(CharSprite.NEUTRAL, defense); //"status" contains target cell, so it can be checked on client

			for (int ID = 0; ID < visibleFight.length; ID++) {
				if (visibleFight[ID]) {
					Hero currHero = Dungeon.heroes[ID];
					if (this == currHero) {
						GLog.iWithTarget(ID, TXT_YOU_MISSED, enemy.name, defense);
					} else {
						GLog.iWithTarget(ID, TXT_SMB_MISSED, enemy.name, defense, name);
					}

					Sample.INSTANCE.play(Assets.SND_MISS, currHero);
				}
			}
			return false;
			
		}
	}
	
	public static boolean hit( Char attacker, Char defender, boolean magic ) {
		float acuRoll = Random.Float( attacker.attackSkill( defender ) );
		float defRoll = Random.Float( defender.defenseSkill( attacker ) );
		return (magic ? acuRoll * 2 : acuRoll) >= defRoll;
	}
	
	public int attackSkill( Char target ) {
		return 0;
	}
	
	public int defenseSkill( Char enemy ) {
		return 0;
	}
	
	public String defenseVerb() {
		return "dodged";
	}
	
	public int dr() {
		return 0;
	}
	
	public int damageRoll() {
		return 1;
	}
	
	public int attackProc( Char enemy, int damage ) {
		return damage;
	}
	
	public int defenseProc( Char enemy, int damage ) {
		return damage;
	}
	
	public float speed() {
		return buff( Cripple.class ) == null ? baseSpeed : baseSpeed * 0.5f;
	}
	
	public void damage( int dmg, Object src ) {
		
		if (getHP() <= 0) {
			return;
		}
		
		Buff.detach( this, Frost.class );
		
		Class<?> srcClass = src.getClass();
		if (immunities().contains( srcClass )) {
			dmg = 0;
		} else if (resistances().contains( srcClass )) {
			dmg = Random.IntRange( 0, dmg );
		}
		
		if (buff( Paralysis.class ) != null) {
			if (Random.Int( dmg ) >= Random.Int(getHP())) {
				Buff.detach( this, Paralysis.class );
				boolean[] visible = Dungeon.visibleForHeroes(pos);
				for (int ID = 0; ID < visible.length; ID++) {
					if (visible[ID]) {
						GLog.i(TXT_OUT_OF_PARALYSIS, name, Dungeon.heroes[ID]);
					}
				}
			}
		}
		
		setHP(getHP() - dmg);
		if (dmg > 0 || src instanceof Char) {
			getSprite().showStatus( getHP() > getHT() / 2 ?
				CharSprite.WARNING : 
				CharSprite.NEGATIVE,
				Integer.toString( dmg ) );
		}
		if (getHP() <= 0) {
			die( src );
		}
	}
	
	public void destroy() {
		setHP(0);
		Actor.remove( this );
		Actor.freeCell( pos );
	}
	
	public void die( Object src ) {
		getSprite().die();
		destroy();
	}

	public boolean isAlive() {
		return getHP() > 0;
	}
	
	@Override
	protected void spend( float time ) {
		
		float timeScale = 1f;
		if (buff( Slow.class ) != null) {
			timeScale *= 0.5f;
		}
		if (buff( Speed.class ) != null) {
			timeScale *= 2.0f;
		}
		
		super.spend( time / timeScale );
	}
	
	public Set<Buff> buffs() {
		return buffs;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Buff> Set<T> buffs( Class<T> c ) {
		Set<T> filtered = new CopyOnWriteArraySet<T>();
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				filtered.add( (T)b );
			}
		}
		return filtered;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Buff> T buff( Class<T> c ) {
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				return (T)b;
			}
		}
		return null;
	}
	
	public boolean isCharmedBy( Char ch ) {
		int chID = ch.id();
		for (Buff b : buffs) {
			if (b instanceof Charm && ((Charm)b).object == chID) {
				return true;
			}
		}
		return false;
	}
	
	public void add( Buff buff ) {
		
		buffs.add( buff );
		Actor.add( buff );
		
		if (getSprite() != null) {
			if (buff instanceof Poison) {
				
				CellEmitter.center( pos ).burst( PoisonParticle.SPLASH, 5 );
				getSprite().showStatus( CharSprite.NEGATIVE, "poisoned" );
				
			} else if (buff instanceof Amok) {
				
				getSprite().showStatus( CharSprite.NEGATIVE, "amok" );

			} else if (buff instanceof Slow) {

				getSprite().showStatus( CharSprite.NEGATIVE, "slowed" );
				
			} else if (buff instanceof MindVision) {
				
				getSprite().showStatus( CharSprite.POSITIVE, "mind" );
				getSprite().showStatus( CharSprite.POSITIVE, "vision" );
				
			} else if (buff instanceof Paralysis) {

				getSprite().add( CharSprite.State.PARALYSED );
				getSprite().showStatus( CharSprite.NEGATIVE, "paralysed" );
				
			} else if (buff instanceof Terror) {
				
				getSprite().showStatus( CharSprite.NEGATIVE, "frightened" );
				
			} else if (buff instanceof Roots) {
				
				getSprite().showStatus( CharSprite.NEGATIVE, "rooted" );
				
			} else if (buff instanceof Cripple) {

				getSprite().showStatus( CharSprite.NEGATIVE, "crippled" );
				
			} else if (buff instanceof Bleeding) {

				getSprite().showStatus( CharSprite.NEGATIVE, "bleeding" );
				
			} else if (buff instanceof Vertigo) {

				getSprite().showStatus( CharSprite.NEGATIVE, "dizzy" );
				
			} else if (buff instanceof Sleep) {
				getSprite().idle();
			}
			
			  else if (buff instanceof Burning) {
				getSprite().add( CharSprite.State.BURNING );
			} else if (buff instanceof Levitation) {
				getSprite().add( CharSprite.State.LEVITATING );
			} else if (buff instanceof Frost) {
				getSprite().add( CharSprite.State.FROZEN );
			} else if (buff instanceof Invisibility) {
				if (!(buff instanceof Shadows)) {
					getSprite().showStatus( CharSprite.POSITIVE, "invisible" );
				}
				getSprite().add( CharSprite.State.INVISIBLE );
			}
		}
	}
	
	public void remove( Buff buff ) {
		
		buffs.remove( buff );
		Actor.remove( buff );
		
		if (buff instanceof Burning) {
			getSprite().remove( CharSprite.State.BURNING );
		} else if (buff instanceof Levitation) {
			getSprite().remove( CharSprite.State.LEVITATING );
		} else if (buff instanceof Invisibility && invisible <= 0) {
			getSprite().remove( CharSprite.State.INVISIBLE );
		} else if (buff instanceof Paralysis) {
			getSprite().remove( CharSprite.State.PARALYSED );
		} else if (buff instanceof Frost) {
			getSprite().remove( CharSprite.State.FROZEN );
		} 
	}
	
	public void remove( Class<? extends Buff> buffClass ) {
		for (Buff buff : buffs( buffClass )) {
			remove( buff );
		}
	}
	
	
	
	@Override
	protected void onRemove() {
		for (Buff buff : buffs.toArray( new Buff[0] )) {
			buff.detach();
		}
		super.onRemove();
	}
	
	public void updateSpriteState() {
		for (Buff buff:buffs) {
			if (buff instanceof Burning) {
				getSprite().add( CharSprite.State.BURNING );
			} else if (buff instanceof Levitation) {
				getSprite().add( CharSprite.State.LEVITATING );
			} else if (buff instanceof Invisibility) {
				getSprite().add( CharSprite.State.INVISIBLE );
			} else if (buff instanceof Paralysis) {
				getSprite().add( CharSprite.State.PARALYSED );
			} else if (buff instanceof Frost) {
				getSprite().add( CharSprite.State.FROZEN );
			} else if (buff instanceof Light) {
				getSprite().add( CharSprite.State.ILLUMINATED );
			}
		}
	}
	
	public int stealth() {
		return 0;
	}
	
	public void move( int step ) {
		
		if (Level.adjacent( step, pos ) && buff( Vertigo.class ) != null) {
			step = pos + Level.NEIGHBOURS8[Random.Int( 8 )];
			if (!(Level.passable[step] || Level.avoid[step]) || Actor.findChar( step ) != null) {
				return;
			}
		}
		
		if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
			Door.leave( pos );
		}
		
		pos = step;
		
		if (flying && Dungeon.level.map[pos] == Terrain.DOOR) {
			Door.enter( pos );
		}

		if (!(this instanceof Hero)) {
			getSprite().visible = Dungeon.visibleforAnyHero(pos);
		}
	}
	
	public int distance( Char other ) {
		return Level.distance( pos, other.pos );
	}
	
	public void onMotionComplete() {
		next();
	}
	
	public void onAttackComplete() {
		next();
	}
	
	public void onOperateComplete() {
		next();
	}
	
	private static final HashSet<Class<?>> EMPTY = new HashSet<Class<?>>();
	
	public HashSet<Class<?>> resistances() {
		return EMPTY;
	}
	
	public HashSet<Class<?>> immunities() {
		return EMPTY;
	}

	public int getHP() {
		return HP;
	}

	public void setHP(int HP) {
		this.HP = HP;
		sendSelf();
	}

	public int getHT() {
		return HT;
	}

	public int setHT(int HT) {
		this.HT = HT;
		sendSelf();
		return HT;
	}

	public void sendSelf(){
		if ( !all().contains(this) ){
			return;
		}
		SendData.sendActor(this);
	}

	public CharSprite getSprite() {
		return sprite;
	}

	public void setSprite(CharSprite sprite) {
		this.sprite = sprite;
		sendSelf();
	}

	public JSONObject getEmoJsonObject() {
		if (sprite == null){
			return new JSONObject();
		}
		return sprite.getEmoJsonObject();
	}
	public static final class GodPunishment{
		@SuppressWarnings("InstantiationOfUtilityClass")
		public static final GodPunishment INSTANCE = new GodPunishment();
	}
}
