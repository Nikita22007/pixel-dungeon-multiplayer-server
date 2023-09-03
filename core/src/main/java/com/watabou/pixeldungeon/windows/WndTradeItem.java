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

import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteGlowing;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class WndTradeItem extends WndOptions {

	private static final float GAP		= 2;
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 16;

	private static final String TXT_SALE		= "FOR SALE: %s - %dg";
	private static final String TXT_BUY			= "Buy for %dg";
	private static final String TXT_SELL		= "Sell for %dg";
	private static final String TXT_SELL_1		= "Sell 1 for %dg";
	private static final String TXT_SELL_ALL	= "Sell all for %dg";
	private static final String TXT_CANCEL		= "Never mind";

	private static final String TXT_SOLD	= "You've sold your %s for %dg";
	private static final String TXT_BOUGHT	= "You've bought %s for %dg";

	private final Item item;
	private final Heap heap;
	private final boolean buy;
	private final boolean canBuy;

	public WndTradeItem(final Item item, @NotNull Hero owner) {

		super(owner);
		this.item = item;
		this.heap = null;
		buy = false;
		canBuy = false;

		WndOptionsParams params = createDescription( item, false );

		if (item.quantity() == 1) {
			params.options.add( Utils.format( TXT_SELL, item.price() ) );
		} else {
			int priceAll = item.price();
			params.options.add(Utils.format( TXT_SELL_1, priceAll / item.quantity() ));
			params.options.add(Utils.format( TXT_SELL_ALL, priceAll ));
		}

		params.options.add(Utils.format( TXT_CANCEL ));
		sendWnd(params);
	}

	public WndTradeItem( final Heap heap, boolean canBuy, Hero hero ) {

		super(hero);
		Item item = heap.peek();
		this.item = item;
		this.heap = heap;
		buy = true;
		this.canBuy = canBuy;

		WndOptionsParams params = createDescription( item, true );

		int price = price( item );

		if (canBuy) {
			params.options.add(Utils.format( TXT_BUY, price ));
			params.options.add(TXT_CANCEL);
		}
		sendWnd(params);
	}

	@Override
	public void hide() {

		super.hide();

		Shopkeeper.sell(getOwnerHero());
	}

	private WndOptionsParams createDescription(Item item, boolean forSale ) {

		WndOptionsParams params = new WndOptionsParams();
		params.icon =  item.image();
		params.iconGlowing = item.glowing();
		params.title = forSale ?
				Utils.format( TXT_SALE, item.toString(), price( item ) ) :
				Utils.capitalize( item.toString() );

		params.titleColor = null;
		if (item.levelKnown) {
			if (item.level() < 0) {
				params.titleColor = ( ItemSlot.DEGRADED );
			} else if (item.level() > 0) {
				params.titleColor = ( item.isBroken() ? ItemSlot.WARNING : ItemSlot.UPGRADED );
			}
		}
		params.message = item.info(getOwnerHero());

		return params;
	}

	private void sell( Item item ) {

		if (item.isEquipped(getOwnerHero()) && !((EquipableItem)item).doUnequip(getOwnerHero(), false )) {
			return;
		}
		item.detachAll( getOwnerHero().belongings.backpack );

		int price = item.price();

		new Gold( price ).doPickUp(getOwnerHero());
		GLog.i( TXT_SOLD, item.name(), price );
	}

	private void sellOne( Item item ) {

		if (item.quantity() <= 1) {
			sell( item );
		} else {
			item = item.detach( getOwnerHero().belongings.backpack );
			if (item == null) {
				return;
			}
			int price = item.price();

			new Gold( price ).doPickUp(getOwnerHero());
			GLog.i( TXT_SOLD, item.name(), price );
		}
	}

	private int price( Item item ) {

		int price = item.price() * 5 * (Dungeon.depth / 5 + 1);
		if (getOwnerHero().buff( RingOfHaggler.Haggling.class ) != null && price >= 2) {
			price /= 2;
		}
		return price;
	}

	private void buy( Heap heap ) {

		Item item = heap.pickUp();

		int price = price( item );
		getOwnerHero().gold -= price;

		GLog.i( TXT_BOUGHT, item.name(), price );

		if (!item.doPickUp(getOwnerHero())) {
			Dungeon.level.drop( item, heap.pos );
		}
	}

	@Override
	protected void onSelect(int index) {
		if (index == -1) {
			hide();
			return;
		}
		if (!buy) {
			if (item.quantity() <= 1) {
				if (index == 0) {
					sell(item);
				}
			} else {
				if (index == 0) {
					sellOne(item);
				} else if (index == 1) {
					sell(item);
				}
			}
			hide();
			return;
		} else {
			if (canBuy) {
				if (index == 0) {
					buy(heap);
				}
				hide();
				return;
			}
		}
	}
}
