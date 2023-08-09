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
package com.watabou.pixeldungeon.items;

import androidx.annotation.Nullable;

import com.nikita22007.multiplayer.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.SnipersMark;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.nikita22007.multiplayer.server.effects.Degradation;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.nikita22007.multiplayer.server.sprites.MissileSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.watabou.pixeldungeon.DungeonTilemap.tileCenterToWorld;
import static com.watabou.pixeldungeon.network.SendData.sendRemoveItemFromInventory;
import static com.watabou.pixeldungeon.network.SendData.sendUpdateItemCount;
import static com.watabou.pixeldungeon.network.SendData.sendUpdateItemFull;

public abstract class Item implements Bundlable {

	private static final String TXT_PACK_FULL	= "Your pack is too full for the %s";
	
	private static final String TXT_BROKEN		= "Because of frequent use, your %s has broken.";
	private static final String TXT_GONNA_BREAK	= "Because of frequent use, your %s is going to break soon.";
	
	private static final String TXT_TO_STRING		= "%s";
	private static final String TXT_TO_STRING_X		= "%s x%d";
	private static final String TXT_TO_STRING_LVL	= "%s%+d";
	private static final String TXT_TO_STRING_LVL_X	= "%s%+d x%d";
	
	private static final float DURABILITY_WARNING_LEVEL	= 1/6f;
	
	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 0.5f;

	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";
	
	public String defaultAction;
	
	protected String name = "smth";
	protected int image = 0;
	

	public boolean stackable = false;
	private int quantity = 1;
	
	private int level = 0;
	private int durability = maxDurability();
	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;
	
	public boolean unique = false;
	
	private static Comparator<Item> itemComparator = new Comparator<Item>() {	
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};

	public boolean isKnown(){
		return true;
	}

	// actions
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<String>();
		actions.add( AC_DROP );
		actions.add( AC_THROW );
		return actions;
	}
	
	public boolean doPickUp( Hero hero ) {
		if (collect( hero.belongings.backpack )) {
			
			GameScene.pickUp( this );
			Sample.INSTANCE.play( Assets.SND_ITEM );
			hero.spendAndNext( TIME_TO_PICK_UP );
			return true;
			
		} else {
			return false;
		}
	}
	
	public void doDrop( Hero hero ) {	
		hero.spendAndNext( TIME_TO_DROP );			
		Dungeon.level.drop( detachAll( hero.belongings.backpack ), hero.pos );
	}
	
	public void doThrow( Hero hero ) {
		GameScene.selectCell( hero,  thrower );
	}
	
	public void execute( Hero hero, String action ) {
		
		curUser = hero;
		curItem = this;
		
		if (action.equals( AC_DROP )) {
			
			doDrop( hero );
			
		} else if (action.equals( AC_THROW )) {
			
			doThrow( hero );
			
		}
		sendUpdateItemFull(this);
		SendData.flush(hero);
	}
	
	public void execute( Hero hero ) {
		execute( hero, defaultAction );
	}
	
	protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
	}

	public boolean collect(Bag container) {
		return collect(container, new ArrayList<Integer>(2)) != null;
	}

	public List<Integer> collect(Bag container, List<Integer> path) {

		ArrayList<Item> items = container.items;
		{
			int index = items.indexOf(this);
			if (index >= 0) {
				path.add(index);
				return path;
			}
		}

		for (Item item:items) {
			if (item instanceof Bag && ((Bag)item).grab( this )) {
				path.add(items.indexOf(item) );
				return collect( (Bag)item, path);
			}
		}
		
		if (stackable) {
			Class<?>c = getClass();
			for (Item item:items) {
				if (item.getClass() == c) {
					item.setQuantity(item.getQuantity() + getQuantity(), false);
					path.add(items.indexOf(item));
					sendUpdateItemCount(container.owner, item, item.getQuantity(), path);
					return path;
				}
			}	
		}
		
		if (items.size() < container.size) {
			
			//if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			if (container.owner != null && (container.owner instanceof Hero) && container.owner.isAlive()) {
				Badges.validateItemLevelAquired(this);
			}
			
			items.add( this );
			Collections.sort( items, itemComparator );

			path.add(items.indexOf(this));
			SendData.sendNewInventoryItem(container.owner, this, path);

			return path;
			
		} else {
			
			GLog.n( TXT_PACK_FULL, name() );
			return null;
			
		}
	}
	public boolean collect(Hero hero) {
		return collect( hero.belongings.backpack );
	}

	/*public boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}*/
	
	public final Item detach( Bag container ) {
		
		if (getQuantity() <= 0) {
			
			return null;
			
		} else
		if (getQuantity() == 1) {

			return detachAll( container );
			
		} else {
			
			setQuantity(getQuantity() - 1);
			
			try { 
				Item detached = getClass().newInstance();
				detached.onDetach( container);
				return detached;
			} catch (Exception e) {
				return null;
			}
		}
	}

	public List<Integer> getSlot(Hero owner) {
		return owner.belongings.pathOfItem(this);
	}

	public final Item detachAll(Bag container) {
		Hero owner = null;
		if (container.owner instanceof Hero) {
			owner = (Hero) container.owner;
		}
		for (Item item : container.items) {
			if (item == this) {
				if (owner != null) {
					sendRemoveItemFromInventory(owner, getSlot(owner));
				}
				container.items.remove(this);
				item.onDetach(container);
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag) item;
				if (bag.contains(this)) {
					return detachAll(bag);
				}
			}
		}
		return this;
	}
	
	protected void onDetach( Bag container) {
		onDetach();
	}

	protected void onDetach() {

	}
	public int level() {
		return level;
	}
	
	public void level( int value ) {
		level = value;
	}
	
	public int effectiveLevel() {
		return isBroken() ? 0 : level;
	}
	
	public Item upgrade() {
		
		cursed = false;
		cursedKnown = true;
		
		level++;
		fix();

		sendUpdateItemFull(this);
		return this;
	}
	
	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}

		return this;
	}
	
	public Item degrade() {
		
		this.level--;	
		fix();

		sendUpdateItemFull(this);
		return this;
	}
	
	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}
		
		return this;
	}
	
	public void use(Hero user) {                  //degrade
		if (level > 0 && !isBroken()) {
			int threshold = (int)(maxDurability() * DURABILITY_WARNING_LEVEL);
			if (durability >= threshold && threshold > (durability-1) && levelKnown) {
				GLog.w( TXT_GONNA_BREAK, name() );
			}
			durability -= 1;
			if (isBroken()) {
				getBroken();
				if (levelKnown) {
					GLog.n( TXT_BROKEN, name() );
					user.interrupt();

					PointF point = tileCenterToWorld(user.pos - Level.WIDTH);
					//PointF point = sprite.center().offset( 0, -16 );
					if (this instanceof Weapon) {
						Degradation.weapon( point );
					} else if (this instanceof Armor) {
						Degradation.armor( point );
					} else if (this instanceof Ring) {
						Degradation.ring( point );
					} else if (this instanceof Wand) {
						Degradation.wand( point );
					}
					Sample.INSTANCE.play( Assets.SND_DEGRADE );
				}
			}
			SendSelfUpdate(user);
		}
	}
	
	public boolean isBroken() {
		return durability <= 0;
	}
	
	public void getBroken() {	
	}
	
	public void fix() {
		durability = maxDurability();
	}
	
	public void polish() {
		if (durability < maxDurability()) {
			durability++;
		}
	}
	
	public int durability() {
		return durability;
	}
	
	public int maxDurability( int lvl ) {
		return 1;
	}
	
	final public int maxDurability() {
		return maxDurability( level );
	}
	
	public int visiblyUpgraded() {
		return levelKnown ? level : 0;
	}
	
	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}
	
	public boolean visiblyBroken() {
		return levelKnown && isBroken();
	}
	
	public boolean isUpgradable() {
		return true;
	}
	
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	public boolean isEquipped( Hero hero ) {
		return false;
	}
	
	public Item identify() {
		
		levelKnown = true;
		cursedKnown = true;

		sendUpdateItemFull(this);
		return this;
	}
	
	public static void evoke( Hero hero ) {
		hero.getSprite().emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}
	
	@Override
	public String toString() {
		
		if (levelKnown && level != 0) {
			if (getQuantity() > 1) {
				return Utils.format( TXT_TO_STRING_LVL_X, name(), level, getQuantity());
			} else {
				return Utils.format( TXT_TO_STRING_LVL, name(), level );
			}
		} else {
			if (getQuantity() > 1) {
				return Utils.format( TXT_TO_STRING_X, name(), getQuantity());
			} else {
				return Utils.format( TXT_TO_STRING, name() );
			}
		}
	}
	
	public String name() {
		return name;
	}
	
	public final String trueName() {
		return name;
	}
	
	public int image() {
		return image;
	}

	private ItemSprite.Glowing glowing = null;

	@Nullable
	public final ItemSprite.Glowing glowing() {
		return glowing;
	}

	public void setGlowing(ItemSprite.Glowing glowing) {
		this.glowing = glowing;
	}

	//always need hero.
	//This method used only in windows,
	//then we can give hero because every window gets hero
	//If item need Hero to create info, then item override this function
	//  else this function call overrided method.
	//Thanks to this trick, I didn't have to edit all classes of items.
	public String info(Hero hero){
		return info();
	}

	public String info() {
		return desc();
	}
	
	public String desc() {
		return "";
	}
	
	public int quantity() {
		return getQuantity();
	}
	
	public void quantity( int value ) {
		setQuantity(value);
	}
	
	public int price() {
		return 0;
	}
	
	public int considerState( int price ) {
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (levelKnown) {
			if (level > 0) {
				price *= (level + 1);
				if (isBroken()) {
					price /= 2;
				}
			} else if (level < 0) {
				price /= (1 - level);
			}
		}
		if (price < 1) {
			price = 1;
		}
		
		return price;
	}
	
	public static Item virtual( Class<? extends Item> cl ) {
		try {
			
			Item item = (Item)cl.newInstance();
			item.setQuantity(0);
			return item;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public Item random() {
		return this;
	}
	
	public String status() {
		return getQuantity() != 1 ? Integer.toString(getQuantity()) : null;
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String DURABILITY		= "durability";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( QUANTITY, getQuantity());
		bundle.put( LEVEL, level );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		if (isUpgradable()) {
			bundle.put( DURABILITY, durability );
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		setQuantity(bundle.getInt( QUANTITY ));
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );
		
		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}
		
		cursed	= bundle.getBoolean( CURSED );
		
		if (isUpgradable()) {
			durability = bundle.getInt( DURABILITY );
		}
	}
	
	public void cast( final Hero user, int dst ) {
		
		final int cell = Ballistica.cast( user.pos, dst, false, true );
		user.getSprite().zap( cell );
		user.busy();
		
		Sample.INSTANCE.play( Assets.SND_MISS, 0.6f, 0.6f, 1.5f );
		
		Char enemy = Actor.findChar( cell );
		
		// FIXME!!!
		float delay = TIME_TO_THROW;
		if (this instanceof MissileWeapon) {
			delay *= ((MissileWeapon)this).speedFactor( user );
			if (enemy != null) {
				SnipersMark mark = user.buff( SnipersMark.class );
				if (mark != null) {
					if (mark.object == enemy.id()) {
						delay *= 0.5f;
					}
					user.remove( mark );
				}
			}
		}
		final float finalDelay = delay;
		
		MissileSprite.
			reset( user.pos, cell, this, new Callback() {
				@Override
				public void call() {
					Item.this.detach( user.belongings.backpack ).onThrow( cell );
					user.spendAndNext( finalDelay );
				}
			} );
	}
	
	public static Hero curUser = null;
	protected static Item curItem = null;
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {	
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				curItem.cast( curUser, target );
			}
		}
		@Override
		public String prompt() {
			return "Choose direction of throw";
		}
	};

	protected int getQuantity() {
		return quantity;
	}

	protected void setQuantity(int quantity) {
		setQuantity(quantity, true);
	}
	protected void setQuantity(int quantity,  boolean send) {
		this.quantity = quantity;
		if (send){
			sendUpdateItemFull(this);
		}
	}
	protected void SendSelfUpdate(){
		SendSelfUpdate(null);
	}
	protected void SendSelfUpdate(Hero heroToFlush){
		sendUpdateItemFull(this);
		if (heroToFlush != null){
			SendData.flush(heroToFlush);
		}
	}
}
