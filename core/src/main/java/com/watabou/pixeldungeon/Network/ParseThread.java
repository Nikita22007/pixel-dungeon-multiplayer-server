package com.watabou.pixeldungeon.network;

import android.util.Log;

import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.CustomMob;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.CustomItem;
import com.watabou.pixeldungeon.items.bags.CustomBag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSprite;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.ui.SpecialSlot;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static com.watabou.pixeldungeon.Dungeon.hero;
import static com.watabou.pixeldungeon.Dungeon.level;
import static com.watabou.pixeldungeon.network.Client.readStream;
import static com.watabou.pixeldungeon.network.Client.socket;
import static com.watabou.pixeldungeon.scenes.GameScene.emitter;
import static com.watabou.pixeldungeon.scenes.GameScene.updateMap;

public class ParseThread extends Thread {

    private BufferedReader reader;

    protected final AtomicReference<String> data = new AtomicReference<>();

    private static ParseThread activeThread;

    public static ParseThread getActiveThread() {
        if (activeThread == null) {
            return activeThread;
        }
        if (!activeThread.isAlive() || activeThread.isInterrupted()) {
            return null;
        }
        return activeThread;
    }

    @Override
    public void run() {
        activeThread = this;
        if (readStream != null) {
            reader = new BufferedReader(readStream);
        }
        while (!socket.isClosed()) {
            try {
                synchronized (data) {
                    if (data.get() == null) {
                        data.set(reader.readLine());
                    }
                }
            } catch (IOException e) {
                GLog.n(e.getMessage());

                PixelDungeon.switchScene(TitleScene.class);
//                PixelDungeon.scene().add(new WndError("Disconnected"));
                return;
            }
        }
    }

    private void parse() throws IOException, JSONException, InterruptedException {
        String json = reader.readLine();
        parse(json);
    }

    public void parseIfHasData() {
        if (data.get() != null) {
            String json = data.get();
            data.set(null);
            try {
                parse(json);
            } catch (IOException e) {
                GLog.n(e.getMessage());

                PixelDungeon.switchScene(TitleScene.class);
//                PixelDungeon.scene().add(new WndError("Disconnected"));
                return;
            } catch (InterruptedException e) {
                return;
            } catch (JSONException e) {
                Log.w("parsing", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void parse(String json) throws IOException, JSONException, InterruptedException {
        if (json == null)
            throw new IOException("EOF");
        JSONObject data;
        try {
            data = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("Parsing", "Malformed JSON." + e.toString());
            e.printStackTrace();
            return;
        }
        //Log.w("data", data.toString(4));
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                        /*case Codes.SERVER_FULL: {
                            PixelDungeon.switchScene(TitleScene.class);
                            // TODO   PixelDungeon.scene().add(new WndError("Server full"));
                            return;
                        }*/
                //level block
                case "map": {
                    parseLevel(data.getJSONObject(token));
                    break;
                }
                //UI block
                case "interlevel_scene": {
                    //todo can cause crash
                    JSONObject ilsObj = data.getJSONObject(token);
                    if (ilsObj.has("state")) {
                        String stateName = data.getJSONObject(token).getString("state").toUpperCase();
                        InterlevelScene.Phase phase = InterlevelScene.Phase.valueOf(stateName);
                        InterlevelScene.phase = phase;
                    }
                    if (ilsObj.has("type")) {
                        String modeName = ilsObj.getString("type").toUpperCase(Locale.ENGLISH);
                        if (modeName.equals("CUSTOM")) {
                            modeName = "NONE";
                        }
                        InterlevelScene.Mode mode = InterlevelScene.Mode.valueOf(modeName);
                        InterlevelScene.mode = mode;
                    }

                    if (ilsObj.has("message")) {
                        InterlevelScene.customMessage = ilsObj.getString("message");
                    }
                    if (!(Game.scene() instanceof InterlevelScene)) {
                        Game.switchScene(InterlevelScene.class);
                    }
                    break;
                }
                //Hero block
                case "actors": {
                    parseActors(data.getJSONArray(token));
                    break;
                }
                case "hero": {
                    parseHero(data.getJSONObject(token));
                    break;
                }
                case "ui": {
                    JSONObject uiObj = data.getJSONObject(token);
                    if (uiObj.has("resume_button_visible")) {
                        hero.resume_button_visible = uiObj.getBoolean("resume_button_visible");
                    }
                    break;
                }
                case "actions": {
                    parseActions(data.getJSONArray(token));
                    break;
                }
                case "messages": {
                    parseMessages(data.getJSONArray(token));
                    break;
                }
                case "inventory": {
                    parseInventory(data.getJSONObject(token));
                    break;
                }
                default: {
                    GLog.h("Incorrect packet token: \"%s\". Ignored", token);
                    continue;
                }
            }
        }

    }

    private void parseMessages(JSONArray messages) {
        Scene scene = Game.scene();
        if (!(scene instanceof GameScene)) {
            return;
        }
        GameLog log = ((GameScene) scene).getGameLog();
        for (int i = 0; i < messages.length(); i++) {
            try {
                JSONObject messageObj = messages.getJSONObject(i);
                if (messageObj.has("color")) {
                    log.WriteMessage(messageObj.getString("text"), messageObj.getInt("color"));
                } else {
                    log.WriteMessageAutoColor(messageObj.getString("text"));
                }
            } catch (JSONException e) {
                Log.w("ParseThread", "Incorrect message");
            }
        }
    }

    private void parseInventory(JSONObject inv) {
        if (inv.has("backpack")) {
            try {
                hero.belongings.backpack = new CustomBag(inv.getJSONObject("backpack"));
            } catch (JSONException e) {
                Log.w("ParseThread", "Can't parse backpack");
            }
        }
        if (inv.has("special_slots")) {
            JSONArray slotsArr;
            try {
                slotsArr = inv.getJSONArray("special_slots");
            } catch (JSONException ignored) {
                assert false : "wtf";
                slotsArr = new JSONArray();
            }
            try {
                for (int i = 0; i < slotsArr.length(); i++) {
                    JSONObject slotObj = slotsArr.getJSONObject(i);
                    SpecialSlot slot = new SpecialSlot();
                    if (slotObj.has("id")) {
                        slot.id = slotObj.getInt("id");
                    }
                    if (slotObj.has("sprite")) {
                        slot.sprite = slotObj.getString("sprite");
                    }
                    if (slotObj.has("image_id")) {
                        slot.image_id = slotObj.getInt("image_id");
                    }
                    if (slotObj.has("item")) {
                        if (slotObj.isNull("item")) {
                            slot.item = null;
                        } else {
                            slot.item = new CustomItem(slotObj.getJSONObject("item"));
                        }
                    }
                    hero.belongings.updateSpecialSlot(slot);
                }
            } catch (JSONException e) {
                Log.w("ParseThread", "Can't parse slot");
            }
        }
    }

    protected void parseSpriteAction(JSONObject actionObj) throws JSONException {
        String action = actionObj.getString("action");
        int actorID = actionObj.getInt("actor_id");
        Actor actor = Actor.findById(actorID);
        if (actor == null) {
            GLog.h("can't resolve actor");
            return;
        }
        CharSprite sprite = ((Char) actor).sprite;
        if (sprite == null) {
            GLog.h("actor " + actorID + "has null sprite");
            return;
        }
        switch (action) {
            case "idle": {
                sprite.idle();
                break;
            }
            case "place": {
                int to = actionObj.getInt("to");
                sprite.place(to);
                break;
            }
            case "run":
            case "move": {
                int from = actionObj.getInt("from");
                int to = actionObj.getInt("to");
                Char ch = (Char) actor;
                if (ch.pos != from) {
                    GLog.h("from != pos. ID:" + actorID);
                }
                ch.move(to);
                if (ch instanceof Mob) {
                    ((Mob) ch).moveSprite(from, to);
                } else {
                    sprite.move(from, to);
                }
                break;
            }
            case "operate": {
                sprite.operate(actionObj.getInt("to"));
                break;
            }
            case "attack": {
                sprite.attack(actionObj.getInt("to"));
                break;
            }
            case "zap": {
                sprite.zap(actionObj.getInt("to"));
                break;
            }
            case "jump": {
                sprite.jump(actionObj.getInt("from"), actionObj.getInt("to"), () -> {
                });
                break;
            }
            case "die": {
                sprite.die();
                break;
            }
            default:
                GLog.n("Unexpected action: " + action + "ID: " + actorID);
        }
    }

    protected void parseActions(JSONArray actions) throws JSONException {
        for (int i = 0; i < actions.length(); i++) {
            JSONObject actionObj;
            try {
                actionObj = actions.getJSONObject(i);
            }  catch (JSONException e) {
                Log.wtf("ParseActions", "can't action from array. " + e.toString());
                e.printStackTrace();
                continue;
            }
            String type = actionObj.optString("action_type");
            switch (type) {
                case ("sprite_action"): {
                    parseSpriteAction(actionObj);
                    break;
                }
                default:
                    GLog.h("unknown action type "+type+". Ignored");
            }
        }
    }

    protected void parseCell(JSONObject cell) throws JSONException {
        int pos = cell.getInt("position");
        if ((pos < 0) || (pos >= level.LENGTH)) {
            GLog.n("incorrect cell position: \"%s\". Ignored.", pos);
            return;
        }
        for (Iterator<String> it = cell.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case "position": {
                    continue;
                }
                case "id": {
                    level.map[pos] = cell.getInt(token);
                    break;
                }
                case "state": {
                    String state = cell.getString("state");
                    level.visited[pos] = state.equals("visited");
                    level.mapped[pos] = state.equals("mapped");
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in cell. Ignored.", token);
                    break;
                }
            }
        }
    }

    protected void parseLevel(JSONObject levelObj) throws JSONException {
        boolean need_observe = false;
        for (Iterator<String> it = levelObj.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case ("cells"): {
                    JSONArray cells = levelObj.getJSONArray(token);
                    for (int i = 0; i < cells.length(); i++) {
                        JSONObject cell = cells.getJSONObject(i);
                        parseCell(cell);
                    }
                    updateMap();
                    break;
                }
                case "entrance": {
                    level.entrance = levelObj.getInt("entrance");
                    break;
                }

                case "exit": {
                    level.entrance = levelObj.getInt("exit");
                    break;
                }
                case "visible_positions": {
                    JSONArray positions = levelObj.getJSONArray(token);
                    Arrays.fill(Dungeon.visible, false);
                    for (int i = 0; i < positions.length(); i++) {
                        int cell = positions.getInt(i);
                        if ((cell < 0) || (cell >= level.LENGTH)) {
                            GLog.n("incorrect visible position: \"%s\". Ignored.", cell);
                            continue;
                        }
                        Dungeon.visible[cell] = true;
                    }
                    Dungeon.observe();
                    GameScene.setFlag(GameScene.UpdateFlags.AFTER_OBSERVE);
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in level. Ignored.", token);
                    break;
                }
            }
        }
    }


    protected void parseActorChar(JSONObject actorObj, int ID, Actor actor) throws JSONException {
        Char chr;
        if (actor == null) {
            chr = new CustomMob(ID);
            GameScene.add((Mob) chr);
        } else {
            chr = (Char) actor;
        }
        for (Iterator<String> it = actorObj.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case "id":
                    continue;
                case "erase_old":
                    continue;
                case "position": {
                    chr.pos = actorObj.getInt(token);
                    break;
                }
                case "hp": {
                    chr.HP = actorObj.getInt(token);
                    break;
                }
                case "max_hp": {
                    chr.HT = actorObj.getInt(token);
                    break;
                }
                case "name": {
                    chr.name = actorObj.getString(token);
                    break;
                }
                case "sprite_name": {
                    assert false : "sprite_name";
                    //todo
                    break;
                }
                case "animation_name": {
                    assert false : "animation_name";
                    //todo
                    break;
                }
                case "description": {
                    ((Mob) chr).setDesc(actorObj.getString(token));
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in Actor Char. Ignored.", token);
                    break;
                }
            }
        }
    }

    protected void parseActorBlob(JSONObject actorObj, int id, Actor actor) {
        GLog.n("Can't parse BLOB");
        assert false : "Can't parse BLOB";
    }

    protected void parseActorHero(JSONObject actorObj, int id, Actor actor) throws JSONException {
        if ((actor == null) || (actor instanceof Hero)) {
            actor = hero != null ? hero : new Hero();
            parseActorChar(actorObj, id, actor);
        } else {
            assert false : "resolved other actor, but waited Hero";
        }
    }


    protected void parseActors(JSONArray actors) throws JSONException {
        for (int i = 0; i < actors.length(); i++) {
            JSONObject actorObj = actors.getJSONObject(i);
            int ID = actorObj.getInt("id");
            boolean erase_old = false;
            if (actorObj.has("erase_old")) {
                erase_old = actorObj.getBoolean("erase_old");
            }
            if (!actorObj.has("type")) {
                GLog.n("Actor does not have type. Ignored");
                continue;
            }
            Actor actor = (erase_old ? null : Actor.findById(ID));
            String type = actorObj.getString("type");
            switch (type) {
                case "char":
                case "character": {
                    parseActorChar(actorObj, ID, actor);
                    break;
                }
                case "hero": {
                    parseActorHero(actorObj, ID, actor);
                    break;
                }
                case "blob": {
                    parseActorBlob(actorObj, ID, actor);
                    break;
                }
                default: {
                    GLog.n("can't resolve actor type: \"" + type + "\". ID: " + ID);
                }
            }
        }
    }

    protected void parseHero(JSONObject heroObj) throws JSONException {
        for (Iterator<String> it = heroObj.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case "actor_id": {
                    hero.changeID(heroObj.getInt(token));
                    break;
                }
                case "strength": {
                    hero.STR = heroObj.getInt(token);
                    break;
                }
                case "lvl": {
                    hero.lvl = heroObj.getInt(token);
                    break;
                }
                case "exp": {
                    hero.exp = heroObj.getInt(token);
                    break;
                }
                case "class": {
                    String className = heroObj.getString(token);
                    className = className.toUpperCase();
                    hero.heroClass = HeroClass.valueOf(className);
                    hero.sprite = new HeroSprite();
                    break;
                }
                case "ready": {
                    if (heroObj.getBoolean(token)) {
                        hero.ready();
                    } else {
                        hero.busy();
                    }
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in Hero. Ignored.", token);
                    break;
                }
            }
        }
    }

}