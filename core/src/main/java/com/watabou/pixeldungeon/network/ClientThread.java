package com.watabou.pixeldungeon.network;

import android.util.Log;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Settings;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Iterator;

class ClientThread extends Thread {

    public static final String CHARSET = "UTF-8";

    protected OutputStreamWriter writeStream;
    protected InputStreamReader readStream;
    private BufferedReader reader;

    protected int threadID;

    protected Socket clientSocket = null;

    protected Hero clientHero;

    protected final NetworkPacket packet = new NetworkPacket();

    public ClientThread(int ThreadID, Socket clientSocket, boolean autostart) {
        this.clientSocket = clientSocket;
        try {
            writeStream = new OutputStreamWriter(
                    clientSocket.getOutputStream(),
                    Charset.forName(CHARSET).newEncoder()
            );
            readStream = new InputStreamReader(
                    clientSocket.getInputStream(),
                    Charset.forName(CHARSET).newDecoder()
            );
            this.threadID = ThreadID;
            reader = new BufferedReader(readStream);
            if (autostart) {
                this.start(); //auto start
            }
        } catch (IOException e) {
            GLog.n(e.getMessage());
            disconnect();
        }
    }

    public void run() {
        //socket read
        if (clientSocket != null && !clientSocket.isConnected()) {
            return;
        }
        while (clientSocket != null && !clientSocket.isClosed()) {
            try {
                String json = reader.readLine();
                if (json == null) {
                    disconnect();
                }
                JSONObject data = new JSONObject(json);
                for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                    String token = it.next();
                    try {
                        switch (token) {
                            //Level block
                            case ("hero_class"): {
                                InitPlayerHero(data.getString(token));
                                break;
                            }
                            case ("cell_listener"): {
                                Integer cell = data.getInt(token);
                                if (cell < 0) {
                                    cell = null;
                                }
                                if (clientHero.cellSelector != null) {
                                    if (clientHero.cellSelector.listener != null) {
                                        clientHero.cellSelector.listener.onSelect(cell);
                                    }
                                }
                                break;
                            }
                            default: {
                                GLog.n("Bad token: %s", token);
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        assert false;
                        GLog.n(String.format("JSONException in ThreadID:%s; Message:%s", threadID, e.getMessage()));
                    }
                }
            } catch (IOException e) {
                assert false;
                PixelDungeon.reportException(e);
                GLog.n(String.format("ThreadID:%s; Message:%s", threadID, e.getMessage()));
                GLog.n(e.getStackTrace().toString());
                disconnect();//  need?

            } catch (Exception e) {
                PixelDungeon.reportException(e);
                GLog.n(e.getStackTrace().toString());
                disconnect();
            }
        }
    }

    //network functions
    protected void flush() {
        try {
            synchronized (packet.dataRef) {
                if (packet.dataRef.get().length() == 0) {
                    return;
                }
                try {
                    Log.i("flush", "clientID: " + threadID + " data:" + packet.dataRef.get().toString(4));
                }catch (JSONException  ignored){}
                synchronized (writeStream) {
                    writeStream.write(packet.dataRef.get().toString());
                    writeStream.write('\n');
                    writeStream.flush();
                }
                packet.clearData();
            }
        } catch (IOException e) {
            GLog.h("IOException in threadID %s. Message: %s", threadID, e.getMessage());
            disconnect();
        }
    }

    //some functions
    protected void InitPlayerHero(String className) throws Exception {
        HeroClass curClass;
        try {
            curClass = HeroClass.valueOf(className.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (!className.equals("random")) { //classID==0 is random class, so it  is not error
                GLog.w("Incorrect class:%s; threadID:%s", className, threadID);
            }
            curClass = Random.element(HeroClass.values());
        }

        Hero newHero = new Hero();
        clientHero = newHero;
        newHero.live();

/*        if (true ){
            Mob mob = Bestiary.mutable( Dungeon.depth );
            mob.state = mob.WANDERING;
            mob.pos = Dungeon.level.entrance+1;
            GameScene.add(mob);
        }*///cheking that mobs sends correctly
        curClass.initHero(newHero);

        newHero.pos = Dungeon.GetPosNear(Dungeon.level.entrance);
        if (newHero.pos == -1) {
            newHero.pos = Dungeon.level.entrance; //todo  FIXME
        }

        packet.packAndAddLevel(Dungeon.level);
        packet.pack_and_add_hero(newHero);
        packet.addInventoryFull(newHero);

        synchronized (Dungeon.heroes) { //todo fix it. It is not work
            for (int i = 0; i < Settings.maxPlayers; i++) {
                if (Dungeon.heroes[i] == null) {
                    Dungeon.heroes[i] = newHero;
                    newHero.networkID = i;
                    break;
                }
            }

            if (newHero.networkID == -1) {
                throw new Exception("Can not find place for hero");
            }

            Actor.add(newHero);
            Actor.occupyCell(newHero);
            Dungeon.observe(newHero, false);
        }
        addAllCharsToSend();
        packet.packAndAddVisiblePositions(Dungeon.visible);
        //TODO send all  information

        flush();


        packet.packAndAddInterlevelSceneState("fade_out", null);

        flush();
    }

    protected void addCharToSend(@NotNull Char ch) {
        synchronized (packet) {
            packet.packAndAddActor(ch, ch == clientHero);
        }
        //todo SEND TEXTURE
    }

    protected void addAllCharsToSend() {
        for (Actor actor : Actor.all()) {
            if (actor instanceof Char) {
                addCharToSend((Char) actor);
            }
        }
    }

    public void addBadgeToSend(String badgeName, int badgeLevel) {
        packet.packAndAddBadge(badgeName, badgeLevel);
    }

    //send primitives
    @Deprecated
    public void sendCode(int code) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    @Deprecated
    public void send(int code, boolean Data) {

        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    @Deprecated
    public void send(int code, byte Data) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    @Deprecated
    public void send(int code, int Data) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    //send arrays
    @Deprecated
    public void send(int code, boolean[] DataArray) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    @Deprecated
    public void send(int code, byte[] DataArray) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    @Deprecated
    public void send(int code, int[] DataArray) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    @Deprecated
    public void send(int code, int var1, String message) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    @Deprecated
    public void send(int code, String message) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    //send_serelliased_data
    @Deprecated
    public void sendData(int code, byte[] data) {
        assert false : "removed_code";
        throw new RuntimeException("removed code");
    }

    //send to all
    @Deprecated
    public static <T> void sendAll(int code) {
        for (int i = 0; i < Server.clients.length; i++) {
            Server.clients[i].sendCode(code);
        }
    }

    @Deprecated
    public static void sendAll(int code, int data) {
        for (int i = 0; i < Server.clients.length; i++) {
            Server.clients[i].send(code, data);
        }
    }

    public void disconnect() {
        try {
            clientSocket.close(); //it creates exception when we will wait client data
        } catch (Exception ignore) {
        }
        readStream = null;
        writeStream = null;
        clientSocket = null;
        Server.clients[threadID] = null;
        Dungeon.removeHero(threadID);
        GLog.n("player " + threadID + " disconnected");
    }
}