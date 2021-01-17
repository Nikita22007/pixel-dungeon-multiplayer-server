package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client extends Thread {
    public static DataInputStream readStream;
    public static DataOutputStream writeStream;
    protected static Socket socket = null;
    protected static Client client;
    protected static ParceThread parceThread = null;

    public static boolean connect(String server, int port) {
        if (parceThread==null){
            parceThread=new ParceThread();
        }
        try {
            socket = new Socket(server, port);
            writeStream = new DataOutputStream(socket.getOutputStream());
            readStream = new DataInputStream(socket.getInputStream());
            client = new Client();
            client.start();
            parceThread.start();
            return socket.isConnected();
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static void disconnect() {
        try {
            socket.close();
        } catch (Exception e) {
        }
        socket = null;
        readStream = null;
        writeStream = null;
        if ( !(PixelDungeon.scene() instanceof TitleScene)){
            PixelDungeon.switchScene(TitleScene.class);
        }
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

    //IO
    //send primitives
    public static void send(int code) {
        try {
            synchronized (writeStream) {
                writeStream.writeInt(code);
                writeStream.flush();
            }
        } catch (Exception e) {
            GLog.h("Exception. Message: {0}", e.getMessage());
            disconnect();
        }
    }
    public static void send(int code, byte Data) {
        try {
            synchronized (writeStream) {
                writeStream.writeInt(code);
                writeStream.writeByte(Data);
                writeStream.flush();
            }
        } catch (Exception e) {
            GLog.h("Exception. Message: {0}", e.getMessage());
            disconnect();
        }
    }
    public static void send(int code, int Data) {
        try {
            synchronized (writeStream) {
                writeStream.writeInt(code);
                writeStream.writeInt(Data);
                writeStream.flush();
            }
        } catch (Exception e) {
            GLog.h("Exception. Message: {0}", e.getMessage());
            disconnect();
        }
    }

    public static void send(int code, String message) {
        try {
            synchronized (writeStream) {
                writeStream.writeInt(code);
                writeStream.writeInt(message.length());
                writeStream.writeChars(message);
                writeStream.flush();
            }
        } catch (Exception e) {
            GLog.h("Exception. Message: {0}", e.getMessage());
            disconnect();
        }
    }
    //send_serelliased_data
    public static void sendData(int code, byte[]  data) {
        try {
            synchronized (writeStream) {
                writeStream.writeInt(code);
                writeStream.write(data);
                writeStream.flush();
            }
        } catch (Exception e) {
            GLog.h("Exception. Message: {0}", e.getMessage());
            disconnect();
        }
    }
    /*
    public static <T> void  send(int code, T ...  data) {
        try {
            writeStream.writeObject(code);
            for (int i=0; i<data.length;i++){
                writeStream.writeObject(data[i]);
            };
            writeStream.flush();
        } catch (Exception e) {
            disconnect();
        }
    }*/
}
