package com.watabou.pixeldungeon.network;

import android.util.Log;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.windows.WndStory;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.watabou.pixeldungeon.network.NetworkPacket.addToArray;
import static com.watabou.pixeldungeon.network.NetworkPacket.packItem;
import static com.watabou.pixeldungeon.network.Server.clients;

public class SendData {

    //---------------------------Level

    public static void addToSendLevelVisitedState(Level level, int ID) {
        if (clients[ID] != null) {
            clients[ID].packet.packAndAddLevelCells(level); //todo optimize this
        }
    }

    public static void sendLevel(Level level, int ID) {
        if (clients[ID] != null) {
            clients[ID].packet.packAndAddLevel(level);
            clients[ID].flush();
        }
    }

    public static void SendLevelReset(int ID) {
        if (clients[ID] != null) {
            clients[ID].packet.packAndAddServerAction("reset_level");
            clients[ID].flush();
        }
    }

    public static void sendLevelCell(Level level, int cell) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.packAndAddLevelCell(level, cell);
            clients[i].flush();
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
            clients[ID].packet.packAndAddIronKeysCount(count);
            clients[ID].flush();
        }
    }

    public static void sendDepth(int depth) {
        for (int i = 0; i < clients.length; i++) {
            sendDepth(i, depth);
        }
    }

    public static void sendDepth(int ID, int depth) {
        if (clients[ID] != null) {
            clients[ID].packet.packAndAddDepth(depth);
            clients[ID].flush();
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
        sendInterLevelScene(ID, type, true);
    }

    public static void sendInterLevelScene(int ID, String type, boolean reset_level) {
        if (clients[ID] != null) {
            clients[ID].flush();
            {
                if (clients[ID].clientHero == null) {
                    return;
                }
            }
            clients[ID].packet.packAndAddInterLevelSceneType(type, reset_level);
            clients[ID].flush();
        }
    }

    public static void sendInterLevelSceneFadeOut(int ID) {
        if (clients[ID] != null) {
            clients[ID].flush();
            if (clients[ID].clientHero == null) {
                return;
            }
            clients[ID].packet.packAndAddInterlevelSceneState("fade_out");
            clients[ID].flush();
        }
    }

    //-----------------------------Windows
    public static void sendWindow(int ID, String type, int windowID, @Nullable JSONObject args) {
        if (clients[ID] != null) {
            clients[ID].packet.packAndAddWindow(type, windowID, args);
            clients[ID].flush();
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

    public static void sendAllChars(int ID) {
        if (clients[ID] != null) {
            clients[ID].addAllCharsToSend();
            clients[ID].flush();
        }
    }

    public static void sendHeroNewID(Hero hero, int ID) {
        if (clients[ID] != null) {
            clients[ID].packet.addNewHeroID(hero.id());
            clients[ID].flush();
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

    public static void sendCharSpriteState(Actor actor, CharSprite.State state, boolean remove) {
        String stateName = state.name().toLowerCase();
        JSONObject stateObj = new JSONObject();
        try {
            stateObj.put("state", stateName);
            stateObj.put("is_removing", remove);
            stateObj.put("actor_id", actor.id());
        } catch (JSONException ignored) {

        }
        sendActor(actor);
    }

    public static void flush(Hero hero) {
        if (hero.networkID >= 0) {
            flush(hero.networkID);
        }
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

    public static void addToSendShowStatus(Float x, Float y, Integer key, String text, int color, boolean ignorePosition) {
        JSONObject data = new JSONObject();
        try {
            data.put("action_type", "show_status");
            data.put("x", x);
            data.put("y", y);
            data.put("key", key);
            data.put("text", text);
            data.put("color", color);
            data.put("ignore_position", ignorePosition);
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

    public static void sendRemoveItemFromInventory(Char owner, List<Integer> path) {
        sendInventoryItemAction(owner, null, path, "remove");
    }

    public static void sendUpdateItemCount(Char owner, Item item, int count, List<Integer> path) {
        sendUpdateItemFull(owner, item, path);
    }

    public static void sendUpdateItemFull(Item item) {
        for (Hero hero : Dungeon.heroes) {
            if (hero == null) {
                continue;
            }
            List<Integer> path = hero.belongings.pathOfItem(item);
            if ((path == null) || (path.isEmpty())) {
                continue;
            }
            sendUpdateItemFull(hero, item, path);
            break;
        }
    }

    public static void sendUpdateItemFull(Char owner, Item item, List<Integer> path) {
        if ((owner == null) || !(owner instanceof Hero)) {
            return;
        }
        JSONObject itemObj = (item == null) ? null : packItem(item, (Hero) owner);
        sendInventoryItemAction(owner, itemObj, path, "update");
    }

    public static void sendNewInventoryItem(Char owner, Item item, List<Integer> path) {
        if ((owner == null) || !(owner instanceof Hero)) {
            return;
        }
        JSONObject itemObj = (item == null) ? null : packItem(item, (Hero) owner);
        //todo optimize
        sendInventoryItemAction(owner, itemObj, path, "place");
    }

    private static void sendInventoryItemAction(Char owner, JSONObject itemObj, List<Integer> path, String action) {
        if (!(owner instanceof Hero)) {
            return;
        }
        Hero hero = (Hero) owner;
        if (hero.networkID < 0) {
            return;
        }
        JSONArray slot = new JSONArray(path);
        JSONObject action_obj = new JSONObject();
        try {
            action_obj.put("action_type", "add_item_to_bag");
            action_obj.put("slot", slot);
            action_obj.put("item", (itemObj == null) ? JSONObject.NULL : itemObj);
            action_obj.put("update_mode", action);
        } catch (JSONException ignored) {
        }
        if (clients[hero.networkID] != null) {
            clients[hero.networkID].packet.addAction(action_obj);
        }
    }

    public static void sendHeapRemoving(Heap heap) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.addHeapRemoving(heap);
            clients[i].flush();
        }
    }

    public static void sendHeap(Heap heap) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.addHeap(heap);
            clients[i].flush();
        }
    }

    public static void sendPlant(int pos, Plant plant) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.packAndAddPlant(pos, plant);
            clients[i].flush();
        }
    }

    public static void sendActorRemoving(Actor actor) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.packAndAddActorRemoving(actor);
            clients[i].flush();
        }
    }

    public static void sendBuff(Buff buff) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            clients[i].packet.packAndAddBuff(buff);
            clients[i].flush();
        }
    }

    public static void sendFlashChar(CharSprite sprite, float flashTime) {
    }

    public static void sendCustomActionForAll(JSONObject action_obj){
        for (int i = 0; i < clients.length; i++) {
            sendCustomAction(action_obj, i);
        }
    }
    public static void sendCustomAction(JSONObject action_obj, Hero hero) {
        if (hero.networkID <= -1){
            return;
        }
        int networkID = hero.networkID;
        if (clients[networkID] != null) {
            clients[networkID].packet.addAction(action_obj);
            clients[networkID].flush();
        }
    }
    public static void sendCustomAction(JSONObject action_obj, int networkID) {
        if (clients[networkID] != null) {
            clients[networkID].packet.addAction(action_obj);
            clients[networkID].flush();
        }
    }
}
