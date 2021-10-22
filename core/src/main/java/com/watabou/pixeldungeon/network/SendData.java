package com.watabou.pixeldungeon.network;

import android.util.Log;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndStory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.watabou.pixeldungeon.network.NetworkPacket.addToArray;
import static com.watabou.pixeldungeon.network.Server.clients;

public class SendData {

    //---------------------------Level

    public static void addToSendLevelVisitedState(Level level, int ID) {
        if (clients[ID] != null) {
            clients[ID].packet.packAndAddLevelCells(level); //todo optimize this
        }
    }

    //---------------------------Hero
    public static void addToSendHeroVisibleCells(boolean[] visible, int ID) {
        if (clients[ID] != null) {
            clients[ID].packet.packAndAddVisiblePositions(visible);
        }
    }

    //---------------------------UI  and mechanics
    public static void sendAllBossSlain() {
        ClientThread.sendAll(Codes.BOSS_SLAIN);
    }

    public static void sendResumeButtonVisible(int ID, boolean visible) {
        if (clients[ID] != null) {
            //  clients[ID].send(Codes.RESUME_BUTTON, visible);
        }
    }

    public static void sendIronKeysCount(int ID, int count) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.IRON_KEYS_COUNT, count);
        }
    }

    //--------------------------Control
    public static void sendHeroReady(int ID, boolean ready) {
        if (clients[ID] != null) {
            synchronized (clients[ID].packet.dataRef) {
                JSONObject data = clients[ID].packet.dataRef.get();
                JSONObject heroObj = null;
                try {
                    if (data.has("hero")) {
                        heroObj = data.getJSONObject("hero");
                    } else {
                        heroObj = new JSONObject();
                        data.put("hero", heroObj);
                    }
                    heroObj.put("ready", ready);
                } catch (JSONException ignored) {
                }
            }
            clients[ID].flush();
        }
    }

    //---------------------------Badges
    //public static void sendBadge
    public static void sendBadgeLevelReached(int ID, int bLevel) {//bLevel=BadgeLevel
        if (clients[ID] != null) {
            clients[ID].addBadgeToSend("level_reached", bLevel);
        }
    }

    public static void sendBadgeStrengthAttained(int ID, int bLevel) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.BADGE_STRENGTH_ATTAINED, bLevel);
        }
    }

    public static void sendAllBadgeBossSlain(int bLevel) {
        ClientThread.sendAll(Codes.BADGE_BOSS_SLAIN, bLevel);
    }

    public static void sendBadgeMastery(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.BADGE_MASTERY);
        }
    }

    //-----------------------------Interlevel Scene
    public static void sendInterLevelScene(int ID, String type) {
        if (clients[ID] != null) {
            clients[ID].flush();
            clients[ID].packet.packAndAddInterLevelSceneType(type);
            clients[ID].flush();
        }
    }

    public static void sendInterLevelSceneFadeOut(int ID) {
        if (clients[ID] != null) {
            clients[ID].flush();
            clients[ID].packet.packAndAddInterlevelSceneState("fade_out");
            clients[ID].flush();
        }
    }

    //-----------------------------Windows
    public static void sendWindow(int ID, int WindowID) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.SHOW_WINDOW, WindowID);
        }
    }

    public static void sendWindowStory(int storyID) {
        ClientThread.sendAll(Codes.SHOW_WINDOW, WndStory.ID());
    }

    //----------
    public static void sendActor(Actor actor) {
        for (ClientThread client : clients) {
            if (client == null) {
                continue;
            }
            client.packet.packAndAddActor(actor, actor == client.clientHero);
            client.flush();
        }
    }

    public static void sendCharSpriteAction(int actorID, String action, Integer cell_from, Integer cell_to) {
        JSONObject actionObj = new JSONObject();
        try {
            actionObj.put("action_type", "sprite_action");
            actionObj.put("action", action);
            actionObj.put("from", cell_from);
            actionObj.put("to", cell_to);
            actionObj.put("actor_id", actorID);
        } catch (JSONException ignored) {

        }
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.addAction(actionObj);
            clients[i].flush();
        }
    }

    public static void sendCharSpriteState(CharSprite.State state, boolean remove) {
        String stateName = state.name().toLowerCase();
        JSONObject stateObj = new JSONObject();
        try {
            stateObj.put("state", stateName);
            stateObj.put("remove", remove);
        } catch (JSONException ignored) {

        }
        GLog.w("can't send char sprite state");
    }

    public static void flush(int networkID) {
        if (clients[networkID] != null) {
            clients[networkID].flush();
        }
    }

    public static void sendMessageToAll(String message, int color) {
        JSONObject messageObj;
        try {
            messageObj = new JSONObject().put("text", message).put("color", color);
        } catch (JSONException e) {
            return;
        }
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.addChatMessage(messageObj);
            clients[i].flush();
        }
    }

    public static void addToSendShowStatus(float x, float y, Integer key, String text, int color) {
        JSONObject data = new JSONObject();
        try {
            data.put("action_type", "show_status");
            data.put("x", x);
            data.put("y", y);
            data.put("key", key);
            data.put("text", text);
            data.put("color", color);
        } catch (JSONException e) {
            Log.wtf("SendData", "Exception while adding showstatus", e);
            return;
        }
        for (ClientThread client : clients) {
            if (client == null) {
                continue;
            }
            AtomicReference<JSONObject> ref = client.packet.dataRef;
            synchronized (ref) {
                try {
                    addToArray(ref.get(), "actions", data);
                } catch (JSONException e) {
                    Log.w("SendData", "failed to send \"Show_status\"");
                    continue;
                }
            }
        }
    }

    public static void sendNewInventoryItem(Char owner, Item item, List<Integer> path) {
        if (!(owner instanceof Hero)) {
            return;
        }
        Hero hero = (Hero) owner;
        if (hero.networkID < 0) {
            return;
        }
        JSONObject itemObj = NetworkPacket.packItem(item, hero);
        JSONArray slot = new JSONArray(path);
        JSONObject action_obj = new JSONObject();
        try {
            action_obj.put("action_type", "add_item_to_bag");
            action_obj.put("slot", slot);
            action_obj.put("item", itemObj);
        } catch (JSONException ignored) {
        }
        if (clients[hero.networkID] != null) {
            clients[hero.networkID].packet.addAction(action_obj);
        }
    }
}
