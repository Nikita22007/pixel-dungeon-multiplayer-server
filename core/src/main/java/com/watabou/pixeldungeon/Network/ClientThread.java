package com.watabou.pixeldungeon.Network;

import com.watabou.pixeldungeon.utils.GLog;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

class ClientThread extends Thread {

    public ObjectInputStream readStream;
    public ObjectOutputStream writeStream;
    public int ThreadID;
    protected Socket clientSocket = null;

    public ClientThread(int ThreadID, Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            readStream = new ObjectInputStream(clientSocket.getInputStream());
            writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.ThreadID = ThreadID;
            this.start(); //auto start
        } catch (IOException e) {
            GLog.n(e.getMessage());
            disconnect();
        }
    }

    public void run() {
        //socket read
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
        Server.clients[ThreadID] = null;

    }
}
