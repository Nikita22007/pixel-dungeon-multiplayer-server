package com.watabou.pixeldungeon.Network;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

class ClientThread extends Thread {

    public ObjectInputStream readStream;
    public ObjectOutputStream writeStream;
    public int threadID;
    protected Socket clientSocket = null;

    public ClientThread(int ThreadID, Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            readStream = new ObjectInputStream(clientSocket.getInputStream());
            writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.threadID = ThreadID;
            this.start(); //auto start
        } catch (IOException e) {
            GLog.n(e.getMessage());
            disconnect();
        }
    }

    public void run() {
        //socket read
        try {
            int code = (int) readStream.readObject();
            switch (code) {
                //Level block
                case (Codes.LEVEL_MAP): {SendData.sendLevelMap(Dungeon.level,threadID); break;}
                case (Codes.LEVEL_VISITED): {SendData.sendLevelVisited(Dungeon.level,threadID);break;}
                case (Codes.LEVEL_MAPPED):{SendData.sendLevelMapped(Dungeon.level,threadID);break;}
                //control block
                case (Codes.CELL_SELECT):{}//todo
                default: {
                    GLog.n("BadCode:{0}", code);
                    break;
                }
            }
        } catch (Exception e) {
            disconnect();//  need?
        }

    }

    public <T> void  send(int code, T ...  data) {
        try {
            writeStream.writeObject(code);
            for (int i=0; i<data.length;i++){
                writeStream.writeObject(data[i]);
            };
            writeStream.flush();
        } catch (Exception e) {
            disconnect();
        }
    }

    public static <T> void sendAll(int code, T ...  data){
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

    }
}
