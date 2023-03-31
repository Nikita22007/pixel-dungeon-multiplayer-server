package com.watabou.pixeldungeon.network;
// based on https://developer.android.com/training/connect-devices-wirelessly/nsd.html#java

import android.app.AlertDialog;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Settings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.watabou.pixeldungeon.Dungeon.heroes;


public class Server extends Thread {
    public static final String SERVICETYPE = "_mppd._tcp."; // _name._protocol //mppd=MultiPlayerPixelDungeon

    //primitive vars
    public static String serviceName;
    protected static int localPort;
    public static boolean started = false;

    //network
    protected static ServerSocket serverSocket;
    protected static Server serverThread;
    protected static ClientThread[] clients;
    protected static RelayThread relay;

    //NSD
    public static volatile RegListenerState regListenerState = RegListenerState.NONE;
    protected static NsdManager nsdManager;
    protected static NsdManager.RegistrationListener registrationListener;
    protected static final int TIME_TO_STOP = 3000; //ms
    protected static final int TIME_TO_START_LISTENER = 10000; //ms
    protected static final int SLEEP_TIME = 100; // ms

    protected static Thread serverStepThread;

    public static boolean startServerStepLoop() {
        if ((serverStepThread != null) && (serverStepThread.isAlive())) {
            return false;
        }
        {
            serverStepThread = new Thread() {
                @Override
                public void run() {
                    //
                    try {
                        while (!interrupted()) {
                            if (Game.instance != null) {
                                if (Game.scene() instanceof GameScene) {
                                    Game.instance.server_step();
                                    sleep(0);
                                } else {
                                    sleep(500);
                                }
                            } else {
                                sleep(500);
                            }
                        }
                    } catch (InterruptedException ignored) {

                    }
                }
            };
            serverStepThread.setDaemon(true);
        }
        serverStepThread.start();
        return true;
    }

    public static boolean startServer() {
        if (started) {
            GLog.h("start when started: WTF?! WHO AND WHERE USED THIS?!");
            return false;
        }
        clients = new ClientThread[Settings.maxPlayers];
        serviceName = PixelDungeon.serverName();
        regListenerState = RegListenerState.NONE;
        if (!initializeServerSocket()) {
            return false;
        }
        if (registrationListener == null) {
            initializeRegistrationListener();
        }
        registerService(localPort);
        int timeToWait = TIME_TO_START_LISTENER;
        while (regListenerState == RegListenerState.NONE) {
            try {
                sleep(SLEEP_TIME);
                timeToWait -= SLEEP_TIME;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        started = (regListenerState == RegListenerState.REGISTERED);
        serverThread = new Server();

        serverThread.start();

        return started;
    }

    public static boolean stopServer() {
        if (!started) {
            return true;
        }
        started = false;
        if (relay != null) {
            relay.interrupt();
            relay = null;
        }
        serverStepThread.interrupt();
        //ClientThread.sendAll(Codes.SERVER_CLOSED); //todo
        unregisterService();
        int sleep_time = TIME_TO_STOP;
        try {
            while ((regListenerState != RegListenerState.UNREGISTERED && regListenerState != RegListenerState.UNREGISTRATION_FAILED) && (sleep_time > 0)) {
                //noinspection BusyWait
                Thread.sleep(SLEEP_TIME);
                sleep_time -= SLEEP_TIME;
            }
        } catch (InterruptedException ignored) {
        }
        return sleep_time >0;
    }

    public static void parseActions() {
        for (ClientThread client : com.watabou.pixeldungeon.network.Server.clients) {
            if (client == null) {
                continue;
            }
            client.parse();
        }
    }

    public static void startClientThread(Socket client) throws IOException {
        synchronized (clients) {
            for (int i = 0; i <= clients.length; i++) {   //search not connected
                if (i == clients.length) { //If we test last and it's connected too
                    //todo use new json
                    new DataOutputStream(client.getOutputStream()).writeInt(Codes.SERVER_FULL);
                    client.close();
                } else if (clients[i] == null) {
                    synchronized (heroes) {
                        Hero emptyHero = null;
                        for (Hero hero : heroes) {
                            if (hero == null) {
                                continue;
                            }
                            if (hero.networkID != -1) {
                                continue;
                            }
                            emptyHero = hero;
                            break;
                        }
                        clients[i] = new ClientThread(i, client, emptyHero); //found
                    }
                    break;
                }
            }
        }
    }

    //Server thread
    public void run() {
        if (PixelDungeon.onlineMode()) {
            relay = new RelayThread(() -> {
                PixelDungeon.instance.runOnUiThread(() -> { //only UI thread can create dialogs
                    new AlertDialog.Builder(PixelDungeon.instance)
                            .setTitle("Error")
                            .setMessage("Relay disconnected or failed to connect.")
                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.cancel, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                });
            });
            relay.start();
        }
        while (started) { //clients  listener
            Socket client;
            try {
                client = serverSocket.accept();  //accept connect
                startClientThread(client);
            } catch (IOException e) {
                if (!(e.getMessage().equals("Socket is closed"))) {  //"Socket is closed" means that client disconnected
                    GLog.h("IO exception:".concat(e.getMessage()));
                }
            }
        }
    }

    //NSD
    protected static void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICETYPE);
        serviceInfo.setPort(port);
        nsdManager = (NsdManager) Game.instance.getSystemService(Context.NSD_SERVICE);

        nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public static void unregisterService() {
        nsdManager.unregisterService(registrationListener);
    }

    protected static boolean initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            serverSocket = new ServerSocket(0);
        } catch (Exception e) {
            return false;
        }
        // Store the chosen port.
        localPort = serverSocket.getLocalPort();
        return true;
    }

    protected static void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
                regListenerState = RegListenerState.REGISTERED;
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                GLog.h("Registration failed: %d?", errorCode);
                regListenerState = RegListenerState.REGISTRATION_FAILED;
                throw new RuntimeException("NsdService registration failed: " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                regListenerState = RegListenerState.UNREGISTERED;
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                GLog.h("Unregistration failed: %d?", errorCode);
                regListenerState = RegListenerState.UNREGISTRATION_FAILED;
                throw new RuntimeException("NsdService unRegistration failed: " + errorCode);
            }
        };
    }


    public static enum RegListenerState {NONE, UNREGISTERED, REGISTERED, REGISTRATION_FAILED, UNREGISTRATION_FAILED}
}
