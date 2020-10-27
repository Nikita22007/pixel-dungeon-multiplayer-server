package com.watabou.pixeldungeon.Network;
// based on https://developer.android.com/training/connect-devices-wirelessly/nsd.html#java

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Settings;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.*;
import java.net.*;

public class Server extends Thread {
    public static final String SERVICENAME = "MupriplayerPD"; //any nonzero string
    public static final String SERVICETYPE = "_mppd._tcp"; // _name._protocol //mppd=MultiPlayerPixelDungeon

    //primitive vars
    public static String serviceName;
    protected static int localPort;
    public static boolean started = false;

    //network
    protected static ServerSocket serverSocket;
    protected static Server serverThread;
    protected static ClientThread[] clients;

    //NSD
    public static RegListenerState regListenerState = RegListenerState.NONE;
    protected static NsdManager nsdManager;
    protected static NsdManager.RegistrationListener registrationListener;


    public static boolean startServer() {
        clients=new ClientThread[Settings.maxPlayers];
        if (started) {
            GLog.h("start when started: WTF?! WHO AND WHERE USED THIS?!");
            return false;
        }
        serviceName = SERVICENAME;
        regListenerState = RegListenerState.NONE;
        if (!initializeServerSocket()) {
            return false;
        }
        if (registrationListener == null) {
            initializeRegistrationListener();
        }
        registerService(localPort);
        while (regListenerState == RegListenerState.NONE) {}//should  we use  Sleep?

        started = (regListenerState == RegListenerState.REGISTERED);
        if (serverThread==null){
            serverThread=new Server();
        }

        serverThread.start();
        return started;
    }

    public static boolean stopServer() {
        if (!started){
            return true;
        }
        ClientThread.sendAll(Codes.SERVER_CLOSED);
        unregisterService();
        while (regListenerState!=RegListenerState.UNREGISTERED  || regListenerState!=RegListenerState.UNREGISTRATION_FAILED){}//should  we use  Sleep?
        return true;
    }

    //Server thread
        public void run() {
        while (started) { //clients  listener
            Socket client;
            try {
                client = serverSocket.accept();  //accept connect

                for (int i = 0; i < clients.length; i++) {   //search not connected
                    if (clients[i] == null) {
                        clients[i] = new ClientThread(i, client); //found
                        break;
                    } else {
                        if (i == clients.length) { //If we test last and it's connected too
                            new ObjectOutputStream(client.getOutputStream()).writeObject(Codes.SERVER_FULL);
                            client.close();
                        }
                    }
                }
            } catch (IOException e) {
                if (!(e.getMessage().equals("Socket is closed"))) {  //"Socket is closed" means  hat client disconnected
                    GLog.h("IO exception:".concat(e.getMessage()));
                }
                break;
            }
        }

    }

    //NSD
    protected static void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(SERVICENAME);
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
                regListenerState = RegListenerState.REGISTRATION_FAILED;
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
                GLog.h("Unregistration failed: WHY?");
                regListenerState = RegListenerState.UNREGISTRATION_FAILED;
            }
        };
    }




    public static enum RegListenerState {NONE, UNREGISTERED, REGISTERED, REGISTRATION_FAILED, UNREGISTRATION_FAILED}
}
