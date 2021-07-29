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

    public void packAndAddHeroClass(String heroClass) {
        synchronized (data) {
        try {
            data.put("hero_class", heroClass);
        } catch (Exception ignored) {
        }
        }
    }
}
