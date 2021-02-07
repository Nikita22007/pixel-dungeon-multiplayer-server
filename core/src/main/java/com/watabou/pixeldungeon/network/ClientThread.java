package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.watabou.pixeldungeon.network.Codes.*;

class ClientThread extends Thread {

    public DataOutputStream writeStream;
    public DataInputStream readStream;
    public int threadID;
    protected Socket clientSocket = null;

    public ClientThread(int ThreadID, Socket clientSocket, boolean autostart) {
        this.clientSocket = clientSocket;
        try {
            writeStream = new DataOutputStream(clientSocket.getOutputStream());
            readStream = new DataInputStream(clientSocket.getInputStream());
            this.threadID = ThreadID;
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
                int code = (int) readStream.readInt();
                switch (code) {
                    //Level block
                    case (Codes.LEVEL_MAP): {
                        SendData.sendLevelMap(Dungeon.level, threadID);
                        break;
                    }
                    case (Codes.LEVEL_VISITED): {
                        SendData.sendLevelVisited(Dungeon.level, threadID);
                        break;
                    }
                    case (Codes.LEVEL_MAPPED): {
                        SendData.sendLevelMapped(Dungeon.level, threadID);
                        break;
                    }
                    //hero block
                    case (Codes.HERO_CLASS): { //create new Hero
                        int classID = (int) readStream.readInt();
                        InitPlayerHero(classID);
                        break;
                    }

                    default: {
                        GLog.n("BadCode:{0}", code);
                        throw new IOException(String.format("Bad code:{0}",code));
                        //break;
                    }
                }
            } catch (IOException e) {
                GLog.n(String.format("ThreadID:{0}; Message:{1}",threadID,e.getMessage()));
                GLog.n(e.getStackTrace().toString());
                disconnect();//  need?

            } catch (Exception e) {
                GLog.n(e.getStackTrace().toString());
            }
        }
    }

    //some functions
    protected void InitPlayerHero(int classID) {
        HeroClass curClass;
        if (classID < 1 || classID > 4) {
            if (classID != 0) { //classID==0 is random class, so it  is not error
                GLog.w("Incorrect classID:{0}; threadID:{1}", classID, threadID);
            }
            classID = Random.Int(1, 4);
        }
        switch (classID) {
            default:
                GLog.h("ClassID incorrect (in switch):{0}; threadID:{1}", classID, threadID);
            case (1):
                curClass = HeroClass.WARRIOR;
                break;
            case (2):
                curClass = HeroClass.MAGE;
                break;
            case (3):
                curClass = HeroClass.ROGUE;
                break;
            case (4):
                curClass = HeroClass.HUNTRESS;
                break;
        }

        Hero newHero = new Hero();
        newHero.live();

/*        if (true ){
            Mob mob = Bestiary.mutable( Dungeon.depth );
            mob.state = mob.WANDERING;
            mob.pos = Dungeon.level.entrance+1;
            GameScene.add(mob);
        }*///cheking that mobs sends correctly
        curClass.initHero(newHero);

        newHero.pos = Dungeon.level.entrance; //todo  FIXME
        send(Codes.LEVEL_ENTRANCE,Dungeon.level.entrance);
        send(Codes.LEVEL_EXIT,Dungeon.level.exit); //todo  Send it when exit visible
        Actor.add(newHero);
        Actor.occupyCell(newHero);
        Dungeon.observe(newHero);

        sendHero(newHero);

        sendAllChars();

        send(Codes.LEVEL_MAP, Dungeon.level.map);
        send(Codes.LEVEL_VISITED, Dungeon.level.visited);
        send(Codes.HERO_VISIBLE_AREA, Dungeon.visible);
        //TODO send all  information
        sendCode(Codes.IL_FADE_OUT);

    }

    protected void sendHero(Hero hero){
        send(HERO_STRENGTH,hero.STR);
        int id = hero.id();
        send(HERO_ACTOR_ID,id);
        send(CHAR_HP,id,hero.HP);
        send(CHAR_HT,id,hero.HT);
        send(CHAR_POS,id,hero.pos);
    }

    protected void sendNewChar(Char ch) { //all Heroes (that is not current player hero) are  nobs
        send(CHAR,ch.id());
        sendChar(ch);
    }
    protected void sendChar(Char ch){
        int id = ch.id();
        send(CHAR_HP,id,ch.HP);
        send(CHAR_HT,id,ch.HT);
        send(CHAR_POS,id,ch.pos);
        send(CHAR_NAME,id, ch.name);
        //todo SEND TEXTURE
    }
    protected void sendAllChars(){
        for (Actor  actor:Actor.all()) {
            if (actor  instanceof Char){
                sendNewChar((Char)actor);
            }
        }
    }
    //thread functions

    //send primitives
    public void sendCode(int code) {
        try {
            writeStream.writeInt(code);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    public void send(int code, boolean Data) {
        try {
            writeStream.writeInt(code);
            writeStream.writeBoolean(Data);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    public void send(int code, byte Data) {
        try {
            writeStream.writeInt(code);
            writeStream.writeByte(Data);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    public void send(int code, int Data) {
        try {
            writeStream.writeInt(code);
            writeStream.writeInt(Data);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    public void send(int code, int var1, int var2) {
        try {
            writeStream.writeInt(code);
            writeStream.writeInt(var1);
            writeStream.writeInt(var2);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    //send arrays
    public void send(int code, boolean[] DataArray) {
        try {
            writeStream.writeInt(code);
            writeStream.writeInt(DataArray.length);
            for (int i = 0; i < DataArray.length; i++) {
                writeStream.writeBoolean(DataArray[i]);
            }
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID, e.getMessage());
            disconnect();
        }
    }
    public void send(int code, byte[] DataArray) {
        try {
            writeStream.writeInt(code);
            writeStream.writeInt(DataArray.length);
            for (int i=0;i<DataArray.length;i++){
                writeStream.writeByte(DataArray[i]);
            }
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    public void send(int code, int[] DataArray) {
        try {
            writeStream.writeInt(code);
            writeStream.writeInt(DataArray.length);
            for (int i=0;i<DataArray.length;i++){
                writeStream.writeInt(DataArray[i]);
            }
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }

    public void send(int code,  int var1, String message) {
        try {
            writeStream.writeInt(code);
            writeStream.writeInt(var1);
            writeStream.writeInt(message.length());
            writeStream.writeChars(message);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    public void send(int code, String message) {
        try {
            writeStream.writeInt(code);
            writeStream.writeInt(message.length());
            writeStream.writeChars(message);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
           disconnect();
        }
    }
    //send_serelliased_data
    public void sendData(int code, byte[]  data) {
        try {
            writeStream.writeInt(code);
            writeStream.write(data);
            writeStream.flush();
        } catch (Exception e) {
            GLog.h("Exception in threadID {0}. Message: {1}", threadID,e.getMessage());
            disconnect();
        }
    }
    //send to all
    public static <T> void sendAll(int code){
        for  (int i=0;i<Server.clients.length;i++){
            Server.clients[i].sendCode(code);
        }
    }

    public static void sendAll(int code, int  data){
        for  (int i=0;i<Server.clients.length;i++){
            Server.clients[i].send(code, data);
        }
    }

    public void disconnect() {
        try {
            clientSocket.close(); //it creates exception when we will wait client data
        } catch (Exception e) {
        }
        readStream = null;
        writeStream = null;
        clientSocket = null;
        Server.clients[threadID] = null;
        GLog.n("player "+threadID+" disconnected");
    }
}
