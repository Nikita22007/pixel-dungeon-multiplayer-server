package com.watabou.pixeldungeon.network;

import android.util.Log;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

public class NetworkPacket {
    public static final String CELLS = "cells";
    public static final String MAP = "map";
    public static final String ACTORS = "actors";

    enum CellState {
        VISITED,
        UNVISITED,
        MAPPED;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public volatile AtomicReference<JSONObject> dataRef;

    public NetworkPacket() {
        dataRef = new AtomicReference<>();
        dataRef.set(new JSONObject());
    }

    public synchronized void clearData() {
        synchronized (dataRef) {
            dataRef.set(new JSONObject());
        }
    }


    protected static JSONArray put_to_JSONArray(Object[] array) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        return jsonArray;
    }

    protected CellState getCellState(boolean visited, boolean mapped) {
        if (visited)
            return CellState.VISITED;
        if (mapped)
            return CellState.MAPPED;
        return CellState.UNVISITED;
    }

    protected void addActor(JSONObject actor) {
        if (actor.length() == 0) {
            return;
        }
        try {
            synchronized (dataRef) {
                JSONObject data = dataRef.get();
                if (!data.has(ACTORS)) {
                    data.put(ACTORS, new JSONArray());
                }
                data.accumulate(ACTORS, actor);
            }
        } catch (JSONException e) {
        }
    }

    protected JSONObject packActor(@NotNull Actor actor, boolean heroAsHero) {

        JSONObject object = new JSONObject();
        try {
            if (actor instanceof Char) {
                int id = actor.id();
                object.put("id", id);
                if (heroAsHero && (actor instanceof Hero)) {
                    object.put("type", "hero");
                } else {
                    object.put("type", "character");
                }
                Char character = (Char) actor;
                String name = character.name;
                int hp = character.HP;
                int ht = character.HT;
                int pos = character.pos;
                object.put("hp", hp);
                object.put("max_hp", ht);
                object.put("position", pos);
                object.put("name", name);
                if (actor instanceof Mob) {
                    String desc = ((Mob) actor).description();
                    object.put("description", desc);
                }
            } else if (actor instanceof Blob) {
                int id = actor.id();
                object.put("id", id);
                object.put("type", "blob");
                assert false : "Does not released sending blobs";
                return new JSONObject();
            } else {
                Log.w("NetworkPacket:", "pack actor. Actor class: " + actor.getClass().toString());
            }
        } catch (JSONException e) {

        }

        return object;
    }

    public void packAndAddActor(Actor actor, boolean heroAsHero) {
        addActor(packActor(actor, heroAsHero));
    }

    protected void addHero(JSONObject hero) {
        try {
            synchronized (dataRef) {

                JSONObject data = dataRef.get();
                data.put("hero", hero);
            }
        } catch (JSONException e) {
        }
    }

    protected JSONObject packHero(@NotNull Hero hero) {
        int id = hero.id();
        JSONObject object = new JSONObject();
        String class_name = hero.heroClass.name();
        int subclass_id = 0;
        int strength = hero.STR;
        int lvl = hero.lvl;
        int exp = hero.exp;
        try {
            object.put("actor_id", id);
            object.put("class", class_name);
            object.put("subclass_id", subclass_id);
            object.put("strength", strength);
            object.put("lvl", lvl);
            object.put("exp", exp);
        } catch (JSONException e) {

        }

        return object;
    }

    public void pack_and_add_hero(@NotNull Hero hero) {
        addActor(packActor(hero, true));
        addHero(packHero(hero));
    }

    public void packAndAddLevelEntrance(int pos) {
        try {
            synchronized (dataRef) {
                JSONObject data = dataRef.get();
                if (!data.has(MAP)) {
                    data.put(MAP, new JSONObject());
                }
                data.getJSONObject(MAP).put("entrance", pos);
            }
        } catch (JSONException ignored) {
        }
    }

    public void packAndAddLevelExit(int pos) {
        try {
            synchronized (dataRef) {
                JSONObject data = dataRef.get();
                if (!data.has(MAP)) {
                    data.put(MAP, new JSONObject());
                }
                data.getJSONObject("map").put("exit", pos);
            }
        } catch (JSONException ignored) {
        }
    }

    protected void addCell(JSONObject cell) {
        try {
            synchronized (dataRef) {
                JSONObject data = dataRef.get();
                if (!data.has(MAP)) {
                    data.put(MAP, new JSONObject());
                }
                JSONObject map = data.getJSONObject(MAP);
                if (!map.has(CELLS)) {
                    map.put(CELLS, new JSONArray());
                }
                map.accumulate(CELLS, cell);
            }
        } catch (JSONException ignored) {
        }
    }

    protected JSONObject packCell(int pos, int id, CellState state) {
        JSONObject cell = new JSONObject();
        try {
            cell.put("position", pos);
            cell.put("id", id);
            cell.put("state", state.toString());
        } catch (JSONException ignored) {
        }
        return cell;
    }

    protected void packAndAddCell(int pos, int id, CellState state) {
        addCell(packCell(pos, id, state));
    }

    public void packAndAddLevelCells(Level level) {
        for (int i = 0; i < level.LENGTH; i++) {
            packAndAddCell(
                    i,
                    level.map[i],
                    getCellState(level.visited[i], level.mapped[i])
            );
        }
    }

    public void packAndAddLevel(Level level) {
        packAndAddLevelEntrance(level.entrance);
        packAndAddLevelExit(level.exit);
        packAndAddLevelCells(level);
    }

    protected void addVisiblePositions(@NotNull JSONArray visiblePositionsArray) {
        try {
            synchronized (dataRef) {
                JSONObject data = dataRef.get();
                if (!data.has(MAP)) {
                    data.put(MAP, new JSONObject());
                }
                data.getJSONObject(MAP).put("visible_positions", visiblePositionsArray);
            }
        } catch (JSONException ignore) {
        }
    }

    public void packAndAddVisiblePositions(boolean[] visible) {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < visible.length; i++) {
            if (visible[i]) {
                arr.put(i);
            }
        }
        addVisiblePositions(arr);
    }

    public void packAndAddBadge(String badgeName, int badgeLevel) {
        JSONObject badge = new JSONObject();
        try {
            badge.put("name", badgeName);
            badge.put("level", badgeLevel);
        } catch (Exception ignored) {
        }
        synchronized (dataRef) {
            try {
                JSONObject data = dataRef.get();
                data.put("badge", badge);
            } catch (Exception ignored) {
            }
        }
    }

    public void packAndAddInterlevelSceneState(String state, String customMessage) {
        try {
            JSONObject stateObj = new JSONObject();
            stateObj.put("state", state);
            if (customMessage != null) {
                stateObj.put("custom_message", customMessage);
            }
            synchronized (dataRef) {
                JSONObject data = dataRef.get();
                data.put("interlevel_scene", stateObj);
            }
        } catch (JSONException ignored) {

        }
    }

    public void packAndAddInterlevelSceneState(String state) {
        packAndAddInterlevelSceneState(state, null);
    }

    @NotNull
    public JSONArray packActions(@NotNull Item item, @NotNull Hero hero) {
        String[] actions = (String[]) item.actions(hero).toArray();
        JSONArray actionsArr = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                actionsArr = new JSONArray(actions);
            } else {
                actionsArr = put_to_JSONArray(actions);
            }
        } catch (JSONException e) {
            Log.e("Packet", "JSONException inside packActions. " + e.toString());
        }
        if (actionsArr == null) {
            actionsArr = new JSONArray();
        }
        return actionsArr;
    }

    public JSONObject packItem(Item item, Hero hero) {
        JSONObject itemObj = new JSONObject();
        try {
            itemObj.put("actions", packActions(item, hero));
            //itemObj.put("sprite_sheet")
            itemObj.put("image", item.image());
            itemObj.put("name", item.name());
            itemObj.put("info", item.info(hero));
            itemObj.put("stackable", item.stackable);
            itemObj.put("quantity", item.quantity());
            itemObj.put("durability", item.durability());
            itemObj.put("max_durability", item.maxDurability());
            itemObj.put("known", item.isKnown());
            itemObj.put("cursed", item.visiblyCursed());
            itemObj.put("level", item.visiblyUpgraded());
        } catch (JSONException e) {
            Log.e("Packet", "JSONException inside packItem. " + e.toString());
        }
        return itemObj;
    }
}
