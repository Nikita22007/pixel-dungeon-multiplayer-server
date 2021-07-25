package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkPacket {
    public static final String CELLS = "cells";
    public static String MAP = "map";
    public static String ACTORS = "actors";

    enum CellState {
        VISITED,
        UNVISITED,
        MAPPED;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public JSONObject data;

    public NetworkPacket() {
        data = new JSONObject();
    }

    public synchronized void clearData() {
        data = new JSONObject();
    }

    protected CellState getCellState(boolean visited, boolean mapped) {
        if (visited)
            return CellState.VISITED;
        if (mapped)
            return CellState.MAPPED;
        return CellState.UNVISITED;
    }

    protected synchronized void addActor(JSONObject actor) {
        try {
            if (!data.has(ACTORS)) {
                data.put(ACTORS, new JSONArray());
            }
            data.accumulate(ACTORS, actor);
        } catch (JSONException e) {
        }
    }

    protected JSONObject packActor(@NotNull Actor actor) {
        int id = actor.id();
        String name = "no-name";
        int hp = 1;
        int ht = 1;
        int pos = 0;
        if (actor instanceof Char) {
            Char character = (Char) actor;
            name = character.name;
            hp = character.HP;
            ht = character.HT;
            pos = character.pos;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("hp", hp);
            object.put("max_hp", ht);
            object.put("pos", pos);
            object.put("name", name);
        } catch (JSONException e) {

        }

        return object;
    }

    public void packAndAddActor(Actor actor) {
        addActor(packActor(actor));
    }

    protected synchronized void addHero(JSONObject hero) {
        try {
            data.put("hero", hero);
        } catch (JSONException e) {
        }
    }

    protected JSONObject packHero(@NotNull Hero hero) {
        int id = hero.id();
        JSONObject object = new JSONObject();
        int class_id = 1;
        int subclass_id = 0;
        int strength = hero.STR;
        int lvl = hero.lvl;
        int exp = hero.exp;
        try {
            object.put("actor_id", id);
            object.put("class_id", class_id);
            object.put("subclass_id", subclass_id);
            object.put("strength", strength);
            object.put("lvl", lvl);
            object.put("exp", exp);
        } catch (JSONException e) {

        }

        return object;
    }

    public void pack_and_add_hero(@NotNull Hero hero) {
        addActor(packActor(hero));
        addHero(packHero(hero));
    }

    public synchronized void packAndAddLevelEntrance(int pos) {
        try {
            if (!data.has(MAP)) {
                data.put(MAP, new JSONObject());
            }
            data.getJSONObject(MAP).put("entrance", pos);
        } catch (JSONException ignored) {
        }
    }

    public synchronized void packAndAddLevelExit(int pos) {
        try {
            if (!data.has(MAP)) {
                data.put(MAP, new JSONObject());
            }
            data.getJSONObject("map").put("exit", pos);
        } catch (JSONException ignored) {
        }
    }

    protected synchronized void addCell(JSONObject cell) {
        try {
            if (!data.has(MAP)) {
                data.put(MAP, new JSONObject());
            }
            JSONObject map = data.getJSONObject(MAP);
            if (!map.has(CELLS)) {
                map.put(CELLS, new JSONArray());
            }
            map.accumulate(CELLS, cell);
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

    protected synchronized void addVisiblePositions(@NotNull JSONArray visiblePositionsArray) {
        try {
            if (!data.has(CELLS)) {
                data.put(CELLS, new JSONObject());
            }
            data.getJSONObject(CELLS).put("visible_positions", visiblePositionsArray);
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
        synchronized (data) {
            try {
                data.put("badge", badge);
            } catch (Exception ignored) {
            }
        }
    }
}
