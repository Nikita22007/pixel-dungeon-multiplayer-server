package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndStory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static void sendInterLevelSceneDescend(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_DESCEND);
        }
    }

    public static void sendInterLevelSceneAscend(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_ASCEND);
        }
    }

    public static void sendInterLevelSceneFall(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_FALL);
        }
    }

    public static void sendInterLevelSceneResurrect(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_RESURRECT);
        }
    }

    public static void sendInterLevelSceneReturn(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_RETURN);
        }
    }

    public static void sendInterLevelSceneOther(int ID, String data) {
        if (clients[ID] != null) {
            clients[ID].send(Codes.IL_OTHER, data);
        }
    }

    public static void sendInterLevelSceneFadeOut(int ID) {
        if (clients[ID] != null) {
            clients[ID].sendCode(Codes.IL_FADE_OUT);
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
            actionObj.put("type", "sprite_action");
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
            try {
                synchronized (clients[i].packet.dataRef) {
                    JSONObject data = clients[i].packet.dataRef.get();
                    if (!data.has("actions")) {
                        data.put("actions", new JSONArray());
                    }
                    data.getJSONArray("actions").put(actionObj);
                }
                clients[i].flush();
            } catch (JSONException ignored) {

            }
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
        }
    }
}
