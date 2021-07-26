package com.watabou.pixeldungeon.network;

import android.util.JsonToken;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.CustomMob;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

import static com.watabou.pixeldungeon.Dungeon.hero;
import static com.watabou.pixeldungeon.Dungeon.level;
import static com.watabou.pixeldungeon.network.Client.readStream;
import static com.watabou.pixeldungeon.network.Client.socket;
import static com.watabou.pixeldungeon.network.Codes.*;

public class ParceThread extends Thread {

    private Scanner scanner;
    private BufferedReader reader;

    @Override
    public void run() {
        if (readStream != null){
            scanner = new Scanner(readStream);
            reader = new BufferedReader(readStream);
        }
        while (!socket.isClosed()){
            try {
                String json = reader.readLine();
                if (json == null)
                    throw new IOException("EOF");
                JSONObject data = new JSONObject(json);
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
                            InterlevelScene.phase = InterlevelScene.Phase.valueOf(data.getJSONObject(token).getString("type").toUpperCase());
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
                        // Control block
                       /* case READY: {
                            if (readStream.readBoolean()) {
                                hero.ready();
                            } else {
                                hero.busy();
                            }
                        }*/
                        case "ui": {
                            JSONObject uiObj = data.getJSONObject(token);
                            if (uiObj.has("resume_button_visible")) {
                                hero.resume_button_visible = uiObj.getBoolean("resume_button_visible");
                            }
                            break;
                        }
                        default: {
                            GLog.h("Incorrect packet token: \"{0}\". Ignored", token);
                            continue;
                        }
                    }
                }
            } catch (JSONException e) {
                GLog.n(e.getMessage());
            } catch(IOException e) {
                GLog.n(e.getMessage());

                PixelDungeon.switchScene(TitleScene.class);
//                PixelDungeon.scene().add(new WndError("Disconnected"));
                return;
            }
        }
    }

    protected void parseCell(JSONObject cell) throws JSONException{
        int pos = cell.getInt("position");
        if ((pos<0) || (pos >= level.LENGTH)){
            GLog.n("incorrect cell position: \"{0}\". Ignored.", pos);
            return;
        }
        for (Iterator<String> it = cell.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token){
                case "position": {
                    continue;
                }
                case "id":{
                    level.map[pos] = cell.getInt(token);
                    break;
                }
                case "state":{
                    String state = cell.getString("state");
                    level.visited[pos] = state.equals("visited");
                    level.mapped[pos] = state.equals("mapped");
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"{0}\" in level. Ignored.", token);
                    break;
                }
            }
        }
    }

    protected void parseLevel(JSONObject levelObj) throws JSONException{
        for (Iterator<String> it = levelObj.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case ("cells"):{
                    JSONArray cells = levelObj.getJSONArray(token);
                    for (int i = 0; i < cells.length(); i++){
                        JSONObject cell = cells.getJSONObject(i);
                        parseCell(cell);
                    }
                }
                case "entrance":{
                    level.entrance = levelObj.getInt("entrance");
                    break;
                }

                case "exit":{
                    level.entrance = levelObj.getInt("exit");
                    break;
                }
                case "visible_positions":{
                    JSONArray positions = levelObj.getJSONArray(token);
                    Arrays.fill(Dungeon.visible, false);
                    for (int i = 0; i < positions.length(); i++){
                        int cell = positions.getInt(i);
                        if ((cell<0) || (cell >= level.LENGTH)){
                            GLog.n("incorrect visible position: \"{0}\". Ignored.", cell);
                            continue;
                        }
                        Dungeon.visible[cell] = true;
                    }
                    Dungeon.observe();
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"{0}\" in level. Ignored.", token);
                    break;
                }
            }
        }
    }

    protected void parseActors(JSONArray actors) throws JSONException{
        for (int i = 0; i < actors.length(); i++) {
            JSONObject actor = actors.getJSONObject(i);
            int ID = actor.getInt("id");
            boolean erase_old = false;
            if (actor.has("erase_old")) {
                erase_old = actor.getBoolean("erase_old");
            }
            //boolean erase_old =  readStream.readBoolean();
            Char chr;
            if (erase_old || Actor.findById(ID) == null) {
                chr = new CustomMob(ID); //todo
                GameScene.add_without_adding_sprite((Mob)chr);
            } else {
              chr = (Char) Actor.findById(ID); //fixme
            }
            for (Iterator<String> it = actor.keys(); it.hasNext(); ) {
                String token = it.next();
                switch (token){
                    case "id": continue;
                    case "erase_old": continue;
                    case "position":{
                        chr.pos = actor.getInt(token);
                        break;
                    }
                    case "hp":{
                        chr.HP = actor.getInt(token);
                        break;
                    }
                    case "max_hp":{
                        chr.HT = actor.getInt(token);
                        break;
                    }
                    case "name":{
                        chr.name = actor.getString(token);
                        break;
                    }
                    case "animation_name":{
                        assert false;
                        //todo
                        break;
                    }
                    default: {
                        GLog.n("Unexpected token \"{0}\" in level. Ignored.", token);
                        break;
                    }
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
                case "strength":{
                    hero.STR = heroObj.getInt(token);
                    break;
                }
                case "lvl":{
                    hero.lvl = heroObj.getInt(token);
                    break;
                }
                case "exp":{
                    hero.exp = heroObj.getInt(token);
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"{0}\" in level. Ignored.", token);
                    break;
                }
            }
        }
    }

}
