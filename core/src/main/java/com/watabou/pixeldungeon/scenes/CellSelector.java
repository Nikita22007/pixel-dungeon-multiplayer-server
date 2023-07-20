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

import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.network.SendData;

import org.jetbrains.annotations.Nullable;

public class CellSelector{
	private Listener listener = null;
	
	public boolean enabled;
	public Hero owner;
	private float dragThreshold;
	
	public CellSelector(Hero owner ) {
		this.owner=owner;
		dragThreshold = PixelScene.defaultZoom * DungeonTilemap.SIZE / 2;
	}

	public void select( int cell ) {
		if (enabled && getListener() != null && cell != -1) {
			
			getListener().onSelect( cell );
			GameScene.ready(owner);
			
		} else {
			
			GameScene.cancel(owner);
			
		}
	}

	
	public void cancel() {
		if (getListener() != null) {
			getListener().onSelect( null );
		}
		
		GameScene.ready(owner);
	}


	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
		SendData.sendCellListenerPrompt(listener != null ? listener.prompt() : null, owner.networkID);
	}

	public interface Listener {
		void onSelect( @Nullable Integer cellz );
		@Nullable String prompt();
	}
}
