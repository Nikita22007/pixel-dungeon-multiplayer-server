package com.watabou.pixeldungeon.network;

import android.util.Log;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Settings;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Set;

import static com.watabou.pixeldungeon.network.ClientThread.CHARSET;
import static com.watabou.pixeldungeon.network.Server.clients;


public class RelayThread extends Thread {
    protected OutputStreamWriter writeStream;
    protected BufferedWriter writer;
    protected InputStreamReader readStream;
    private BufferedReader reader;
    protected Socket clientSocket;
    private int getRelayPort(){
        if (!PixelDungeon.useCustomRelay()){
            return Settings.defaultRelayServerPort;
        }
        int port = PixelDungeon.customRelayPort();
       return (port != 0)? port: Settings.defaultRelayServerPort;
    }

    private String getRelayAddress(){
        if (!PixelDungeon.useCustomRelay()){
            return Settings.defaultRelayServerAddress;
        }
        String address = PixelDungeon.customRelayAddress();
        return (!"".equals(address))? address : Settings.defaultRelayServerAddress;
    }

    public void run() {
        Socket socket = null;
        String relayServerAddress = getRelayAddress();
        try {
            socket = new Socket(relayServerAddress, getRelayPort());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.clientSocket = socket;
        try {
            writeStream = new OutputStreamWriter(
                    clientSocket.getOutputStream(),
                    Charset.forName(CHARSET).newEncoder()
            );
            readStream = new InputStreamReader(
                    clientSocket.getInputStream(),
                    Charset.forName(CHARSET).newDecoder()
            );
            reader = new BufferedReader(readStream);
            writer = new BufferedWriter(writeStream, 16384);


            JSONObject name = new JSONObject();
            name.put("action", "name");
            name.put("name", PixelDungeon.serverName());
            writer.write(name.toString());
            writer.write('\n');
            writer.flush();
            while (true) {
                String json = reader.readLine();
                if (json == null){
                    GLog.h("relay thread stopped");
                    socket.close();
                    return;
                }
                JSONObject port_obj = new JSONObject(json);
                int port = port_obj.getInt("port");
                Socket client = new Socket(relayServerAddress, port);
                synchronized (clients) {
                    for (int i = 0; i <= clients.length; i++) {   //search not connected
                        if (i == clients.length) { //If we test last and it's connected too
                            //todo use new json
                            new DataOutputStream(client.getOutputStream()).writeInt(Codes.SERVER_FULL);
                            client.close();
                        } else if (clients[i] == null) {
                            clients[i] = new ClientThread(i, client, true); //found
                            break;
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            GLog.h("relay thread stopped");
            return;
        }
    }
}