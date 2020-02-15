package com.watabou.pixeldungeon.Network;
// based on https://developer.android.com/training/connect-devices-wirelessly/nsd.html#java

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    public static final String SERVICENAME = "MupriplayerPD"; //any nonzero string
    public static final String SERVICETYPE = "_mppd._tcp"; // _name._protocol

    //primitive vars
    public static String serviceName;
    protected static int localPort;
    public static boolean started = false;

    //network
    protected static ServerSocket serverSocket;

    //NSD
    public static RegListenerState regListenerState = RegListenerState.NONE;
    protected static NsdManager nsdManager;
    protected static NsdManager.RegistrationListener registrationListener;


    public static boolean startServer() {

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
        while (regListenerState == RegListenerState.NONE) {
        }//should  we use  Sleep?

        started = (regListenerState == RegListenerState.REGISTERED);

        return started;
    }

    public static boolean stopServer() {
        unregisterService();
        return true;
    }

    public void run() {    }

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

class clientThread extends Thread {

    DataInputStream is = null;
    PrintStream os = null;
    Socket clientSocket = null;
    List<clientThread> t;

    String name;

    public clientThread(Socket clientSocket, List<clientThread> t) {
        this.clientSocket = clientSocket;
        this.t = t;
    }

    public void run() {
        String line;

        try {
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            os.println("Enter your name: ");
            name = is.readLine();
            os.println("Hello " + name + " to our chat room.\nTo leave enter /quit in a new line");
            for (clientThread client : t)
                client.os.println("*** A new user " + name + " entered the chat room !!! ***");
            while (true) {
                line = is.readLine();
                if (line.startsWith("/quit"))
                    break;
                if (line.startsWith("<")) {
                    String tempName = line.substring(1, line.lastIndexOf(">"));
                    System.out.println(tempName);
                    boolean flag = true;
                    for (clientThread ct : t) {
                        if (ct != null) {
                            if (ct.name.equals(tempName)) {
                                ct.os.println(line);
                                flag = false;
                            }
                        } else {
                            break;
                        }
                    }
                    if (flag)
                        this.os.println("User " + tempName + " is not online");
                } else {
                    for (clientThread client : t)
                        client.os.println("<" + name + "> " + line);
                }
            }

            for (clientThread client : t)
                if (client != null && client != this)
                    client.os.println("*** The user " + name + " is leaving the chat room !!! ***");
            os.println("*** Bye " + name + " ***");

            for (clientThread client : t)
                if (client == this) client = null;

            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
        }
        ;
    }
} //may be needn't
