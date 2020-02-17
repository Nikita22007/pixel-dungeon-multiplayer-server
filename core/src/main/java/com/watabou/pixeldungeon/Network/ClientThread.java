package com.watabou.pixeldungeon.Network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

class ClientThread extends Thread {

    public Socket clientSocket = null;
    public int ThreadID;
    
    public ClientThread(int ThreadID, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.ThreadID=ThreadID;
        this.start(); //auto start
    }

    public void run() {
        //socket read
    }

    public void disconnect(){
        try {
            clientSocket.close(); //it creates exception when we will wait client data
        }catch(Exception e){}
        clientSocket=null;
        Server.clients[ThreadID]=null;

    }
}
