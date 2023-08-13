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
package com.nikita22007.multiplayer.server.effects;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.network.SendData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Visual effect. Shows item sprite, colored in item enchantment color, above the {@link Char}'s head
 */
public class Enchanting {
    public static void show(@NotNull Char ch, @NotNull Item item) {
        JSONObject actionObj = new JSONObject();
        try {
            actionObj.put("action_type", "enchanting_visual");
            actionObj.put("target", ch.id());
            actionObj.put("item", item.toJsonObject(ch instanceof Hero ? (Hero) ch : null));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        SendData.sendCustomActionForAll(actionObj);
    }
}