package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;


public class Client extends Thread {
    public static final String CHARSET = "UTF-8";

    protected static OutputStreamWriter writeStream;
    protected static InputStreamReader readStream;
    protected static Socket socket = null;
    protected static Client client;
    protected static ParceThread parceThread = null;
    protected static NetworkPacket packet = new NetworkPacket();


    public static boolean connect(String server, int port) {
        packet.clearData();
        if (parceThread==null){
            parceThread=new ParceThread();
        }
        try {
            socket = new Socket(server, port);
            writeStream = new OutputStreamWriter(
                    socket.getOutputStream(),
                    Charset.forName(CHARSET).newEncoder()
            );
            readStream = new InputStreamReader(
                    socket.getInputStream(),
                    Charset.forName(CHARSET).newDecoder()
            );
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
            while (!socket.isClosed()) sleep(1000);
        } catch (Exception e) {
            GLog.n(e.getStackTrace().toString());
        }
        disconnect();
    }

    public static void flush(){
        try {
            synchronized (packet){
                synchronized (writeStream) {
                    writeStream.write(packet.data.toString());
                    writeStream.write('\n');
                    writeStream.flush();
                    packet.clearData();
                }
            }
        } catch (IOException e){
            GLog.h("IOException. Message: {0}", e.getMessage());
            disconnect();
        }
    }
    //methods
    public static void sendHeroClass(HeroClass heroClass){
        packet.packAndAddHeroClass(heroClass.name().toLowerCase());
        flush();
    }
}
