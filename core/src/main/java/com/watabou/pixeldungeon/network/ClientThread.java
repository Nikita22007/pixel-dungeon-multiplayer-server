package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
                        break;
                    }
                }
            } catch (IOException e) {
                GLog.n(e.getMessage());
                GLog.n(e.getStackTrace().toString());
                disconnect();//  need?

            } catch (Exception e) {
                GLog.n(e.getStackTrace().toString());
            }
        }
    }

    //some functions
    public void InitPlayerHero(int classID) {
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
        curClass.initHero(newHero);
        newHero.pos = Dungeon.level.entrance;
        Actor.add(newHero);
        Dungeon.observe(newHero);

        send(Codes.LEVEL_MAP, Dungeon.level.map);

        //TODO send all  information
        send(Codes.IL_FADE_OUT);

    }
    //thread functions

    //send primitives
    public void send(int code) {
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
            Server.clients[i].send(code);
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
