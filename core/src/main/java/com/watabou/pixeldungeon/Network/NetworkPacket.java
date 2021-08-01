package com.watabou.pixeldungeon.network;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

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

    public AtomicReference<JSONObject> dataRef;

    public NetworkPacket() {
        dataRef = new AtomicReference<>(new JSONObject());
    }

    public void clearData() {
        synchronized (dataRef) {
            dataRef.set(new JSONObject());
        }
    }

    public void packAndAddHeroClass(String heroClass) {
        synchronized (dataRef) {
            try {
                dataRef.get().put("hero_class", heroClass);
            } catch (Exception ignored) {
            }
        }
    }

    public void packAndAddCellListenerCell(Integer cell) {
        synchronized (dataRef) {
            try {
                if (cell == null) {
                    dataRef.get().put("cell_listener", -1);
                } else {
                    dataRef.get().put("cell_listener", cell.intValue());
                }
            } catch (Exception ignored) {
            }
        }
    }
}
