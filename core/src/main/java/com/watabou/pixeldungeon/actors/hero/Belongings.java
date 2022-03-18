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
package com.watabou.pixeldungeon.actors.hero;

import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.HeroHelp;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.network.SpecialSlot;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.watabou.pixeldungeon.network.SendData.sendIronKeysCount;
import static com.watabou.pixeldungeon.network.SendData.sendNewInventoryItem;

public class Belongings implements Iterable<Item> {

	public static final int BACKPACK_SIZE	= 19;
	
	public Hero owner;
	
	public Bag backpack;	

	protected KindOfWeapon weapon = null;
	protected Armor armor = null;
	protected Ring ring1 = null;
	protected Ring ring2 = null;
	public int IronKeyCount_visual = 0; //this is count keys of  this depth. This  is "IronKey.curDepthQuantity"
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Bag() {{
			name = "backpack";
			size = BACKPACK_SIZE;
		}};
		backpack.owner = owner;
	}
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String RING1		= "ring1";
	private static final String RING2		= "ring2";
	
	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, getWeapon());
		bundle.put( ARMOR, getArmor());
		bundle.put( RING1, getRing1());
		bundle.put( RING2, getRing2());
	}
	
	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		setWeapon((KindOfWeapon)bundle.get( WEAPON ));
		if (getWeapon() != null) {
			getWeapon().activate( owner );
		}
		
		setArmor((Armor)bundle.get( ARMOR ));
		
		setRing1((Ring)bundle.get( RING1 ));
		if (getRing1() != null) {
			getRing1().activate( owner );
		}
		
		setRing2((Ring)bundle.get( RING2 ));
		if (getRing2() != null) {
			getRing2().activate( owner );
		}
	}

	public ArrayList<SpecialSlot> getSpecialSlots() {
		ArrayList<SpecialSlot> slots = new ArrayList<>(4);
		slots.add(new SpecialSlot(0, "items.png", ItemSpriteSheet.WEAPON, getWeapon()));
		slots.add(new SpecialSlot(1, "items.png", ItemSpriteSheet.ARMOR, getArmor()));
		slots.add(new SpecialSlot(2, "items.png", ItemSpriteSheet.RING, getRing1()));
		slots.add(new SpecialSlot(3, "items.png", ItemSpriteSheet.RING, getRing2()));
		return slots;
	}

	public Bag[] getBags() {
        return new Bag[]{
                backpack,
                getItem(SeedPouch.class),
                getItem(ScrollHolder.class),
                getItem(WandHolster.class),
                getItem(Keyring.class)
        };
    }

	public Item getItemInSlot(List<Integer> slot) {
		if (slot.get(0) < 0) {
			SpecialSlot spec_slot = getSpecialSlots().get(-slot.get(0) - 1);
			slot.remove(0);
			if (slot.isEmpty()) {
				return spec_slot.item;
			} else {
				return ((Bag) spec_slot.item).getItemInSlot(slot);
			}
		}
		return backpack.getItemInSlot(slot);
	}

	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				return (T)item;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Key> T getKey( Class<T> kind, int depth ) {
		
		for (Item item : backpack) {
			if (item.getClass() == kind && ((Key)item).depth == depth) {
				return (T)item;
			}
		}
		
		return null;
	}
	
	public void countIronKeys() {

		int keyscount =0;
		
		for (Item item : backpack) {
			if (item instanceof IronKey && ((IronKey)item).depth == Dungeon.depth) {
				keyscount ++;
			}
		}
		if (keyscount!=IronKeyCount_visual) {
			IronKeyCount_visual=keyscount;
			sendIronKeysCount(HeroHelp.getHeroID(owner), keyscount);
		}
	}
	
	public void identify() {
		for (Item item : this) {
			item.identify();
		}
	}
	
	public void observe() {
		if (getWeapon() != null) {
			getWeapon().identify();
			Badges.validateItemLevelAquired(getWeapon());
		}
		if (getArmor() != null) {
			getArmor().identify();
			Badges.validateItemLevelAquired(getArmor());
		}
		if (getRing1() != null) {
			getRing1().identify();
			Badges.validateItemLevelAquired(getRing1());
		}
		if (getRing2() != null) {
			getRing2().identify();
			Badges.validateItemLevelAquired(getRing2());
		}
		for (Item item : backpack) {
			item.cursedKnown = true;
		}
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, getArmor(), getWeapon(), getRing1(), getRing2());
	}
	
	public Item randomUnequipped() {
		return Random.element( backpack.items );
	}
	
	public void resurrect( int depth ) {
		for (Item item : backpack.items.toArray( new Item[0])) {
			if (item instanceof Key) {
				if (((Key)item).depth == depth) {
					item.detachAll( backpack );
				}
			} else if (item.unique) {
				// Keep unique items
			} else if (!item.isEquipped( owner )) {
				item.detachAll( backpack );
			}
		}
		
		if (getWeapon() != null) {
			getWeapon().cursed = false;
			getWeapon().activate( owner );
		}
		
		if (getArmor() != null) {
			getArmor().cursed = false;
		}
		
		if (getRing1() != null) {
			getRing1().cursed = false;
			getRing1().activate( owner );
		}
		if (getRing2() != null) {
			getRing2().cursed = false;
			getRing2().activate( owner );
		}
	}
	
	public int charge( boolean full) {
		
		int count = 0;
		
		for (Item item : this) {
			if (item instanceof Wand) {
				Wand wand = (Wand)item;
				if (wand.curCharges < wand.maxCharges) {
					wand.curCharges = full ? wand.maxCharges : wand.curCharges + 1;
					count++;
					
					wand.updateQuickslot();
				}
			}
		}
		
		return count;
	}
	
	public int discharge() {
		
		int count = 0;
		
		for (Item item : this) {
			if (item instanceof Wand) {
				Wand wand = (Wand)item;
				if (wand.curCharges > 0) {
					wand.curCharges--;
					count++;
					
					wand.updateQuickslot();
				}
			}
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator(); 
	}

	public List<Integer> pathOfItem(@NotNull Item item) {
		assert (item != null) : "path of null item";
		List<SpecialSlot> specialSlots = getSpecialSlots();
		for (int i = 0; i < specialSlots.size(); i++) {
			if (specialSlots.get(i) == null) {
				continue;
			}
			if (specialSlots.get(i).item == item) {
				List<Integer> slot = new ArrayList<>(2);
				slot.add(-i - 1);
				return slot;
			}
			if (specialSlots.get(i).item instanceof Bag) {
				List<Integer> path = ((Bag) specialSlots.get(i).item).pathOfItem(item);
				if (path != null) {
					path.add(0, -i - 1);
					return path;
				}
			}
		}
		return backpack.pathOfItem(item);
	}

	public KindOfWeapon getWeapon() {
		return weapon;
	}

	public KindOfWeapon setWeapon(KindOfWeapon weapon) {
		List<Integer> path = new ArrayList<Integer>(1);
		path.add(-1);
		sendNewInventoryItem(owner, weapon, path);
		return (this.weapon = weapon);
	}

	public Armor getArmor() {
		return armor;
	}

	public Armor setArmor(Armor armor) {
		List<Integer> path = new ArrayList<Integer>(1);
		path.add(-2);
		sendNewInventoryItem(owner, armor, path);
		return (this.armor = armor);
	}

	public Ring getRing1() {
		return ring1;
	}

	public Ring setRing1(Ring ring1) {
		List<Integer> path = new ArrayList<Integer>(1);
		path.add(-4);
		sendNewInventoryItem(owner, ring1, path);
		return (this.ring1 = ring1);
	}

	public Ring getRing2() {
		return ring2;
	}

	public Ring setRing2(Ring ring2) {
		List<Integer> path = new ArrayList<Integer>(1);
		path.add(-4);
		sendNewInventoryItem(owner, ring2, path);
		return (this.ring2 = ring2);
	}

	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {getWeapon(), getArmor(), getRing1(), getRing2()};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
			case 0:
				equipped[0] = setWeapon(null);
				break;
			case 1:
				equipped[1] = setArmor(null);
				break;
			case 2:
				equipped[2] = setRing1(null);
				break;
			case 3:
				equipped[3] = setRing2(null);
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
