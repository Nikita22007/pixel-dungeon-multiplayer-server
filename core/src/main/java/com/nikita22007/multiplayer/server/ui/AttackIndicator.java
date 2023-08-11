/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Pixel Dungeon Multiplayer
 * Copyright (C) 2021-2023 Nikita Shaposhnikov
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
package com.nikita22007.multiplayer.server.ui;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class AttackIndicator {

	@NotNull
	private final Hero owner;

	@Nullable
	private Char lastTarget = null;
	private final ArrayList<Char> candidates = new ArrayList<Char>();

	public AttackIndicator(@NotNull Hero owner) {
		Objects.requireNonNull(owner,"Attack Indicator received null owner");
		this.owner=owner;
	}

	@Nullable
	private Char getLastTarget() {
		return lastTarget;
	}

	/**
	 * Sets {@link #lastTarget} and sends new information to client
	 * @param lastTarget new lastTarget
	 */
	private void setLastTarget(Char lastTarget) {
		this.lastTarget = lastTarget;
		SendData.sendHeroAttackIndicator(lastTarget == null? null: lastTarget.id(), owner.networkID);
	}

	/**
	 * Updates {@link #candidates}. Updates {@link #lastTarget}.
	 * <p>
	 * If {@link #lastTarget} not in {@link #candidates},  chooses random element from
	 * {@link #candidates} or {@code null} if {@link #candidates} is empty.
	 */
	private void checkEnemies() {
		
		int heroPos = owner.pos;
		candidates.clear();
		int v = owner.visibleEnemies();
		for (int i=0; i < v; i++) {
			Mob mob = owner.visibleEnemy( i );
			if (Level.adjacent( heroPos, mob.pos )) {
				candidates.add( mob );
			}
		}
		
		if (!candidates.contains(getLastTarget())) {
			if (candidates.isEmpty()) {
				setLastTarget(null);
			} else {
				setLastTarget(Random.element( candidates ));
			}
		}
	}

	protected void onClick() {
		if (lastTarget != null) {
			owner.handle(lastTarget.pos);
		}
	}
	
	public void target( Char target ) {
		setLastTarget(target);
	}

	/**
	 * {@link #checkEnemies}
	 */
	public void updateState() {
		checkEnemies();
	}
}
