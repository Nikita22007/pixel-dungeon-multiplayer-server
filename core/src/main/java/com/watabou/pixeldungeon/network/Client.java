package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.io.*;
import java.net.*;

public class Client extends Thread {
    public static ObjectInputStream readStream;
    public static ObjectOutputStream writeStream;
    protected static Socket socket = null;
    protected static Client client;

    public static boolean connect(String server, int port) {
        try {
            socket = new Socket(server, port);
            writeStream = new ObjectOutputStream(socket.getOutputStream());
            readStream = new ObjectInputStream(socket.getInputStream());
            client = new Client();
            client.start();
            return socket.isConnected();
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (Exception e) {
        }
        socket = null;
        readStream = null;
        writeStream = null;
        PixelDungeon.switchScene(TitleScene.class);
    }

    public void run() {
        if (!socket.isConnected()) {
            disconnect();
            return;
        }
        try {
            while (!socket.isClosed()) ;
        } catch (Exception e) {
            GLog.n(e.getStackTrace().toString());
        }
        disconnect();
    }
}
