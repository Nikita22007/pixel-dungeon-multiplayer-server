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
package com.watabou.pixeldungeon.windows;

import android.graphics.RectF;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.plants.Plant.Seed;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.GameMath;

public class WndBag extends WndTabbed {
	
	public static enum Mode {
		ALL,
		UNIDENTIFED,
		UPGRADEABLE,
		QUICKSLOT,
		FOR_SALE,
		WEAPON,
		ARMOR,
		ENCHANTABLE,
		WAND,
		SEED
	}



	
	private Listener listener;
	private WndBag.Mode mode;
	private String title;

	protected int count;

	public WndBag( Hero owner, Listener listener, Mode mode, String title ) {
		
		super();

		ownerHero=owner;

		this.listener = listener;
		this.mode = mode;  //send to Client
		this.title = title;  //send to  Client

	}
	public void SelectItem(Item item){
		if (listener!=null){
			listener.onSelect(item);
		}
	}

	public interface Listener {
		void onSelect( Item item );
	}
}
