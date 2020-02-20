package com.watabou.pixeldungeon.network;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;
import java.util.List;


public class Scanner { //Todo write this
    protected static final String SERVICE_TYPE = "_mppd._tcp"; // _name._protocol //mppd=MultiPlayerPixelDungeon

    protected static List<ServerInfo> serverList = new ArrayList<>();
    //NSD
    protected static enum ListenerState {STARTED, STOPPED, START_FAIL, STOP_FAIL, NULL}
    protected static ListenerState state;
    protected static NsdManager.DiscoveryListener discoveryListener;
    protected static NsdManager.ResolveListener resolveListener;
    protected static NsdManager nsdManager;

    public static boolean start() {
        initializeResolveListener();
        initializeDiscoveryListener();
        state = ListenerState.NULL;
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        while (state == ListenerState.NULL) {
        }
        return state == ListenerState.STARTED;
    }

    public static List<ServerInfo> getServerList() {
        return serverList;
    }

    public static boolean stop() {
        nsdManager.stopServiceDiscovery(discoveryListener);
        return true;
    }

    public static void initializeResolveListener() {
        if (resolveListener != null) {
            return;
        }
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                GLog.n("Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                GLog.p("Resolve Succeeded. " + serviceInfo);
                ServerInfo server = new ServerInfo(
                        serviceInfo.getServiceName(), //name
                        serviceInfo.getHost(),
                        serviceInfo.getPort(),
                        -1, -1, false
                );
                serverList.add(server);
            }
        };
    }

    //public static boolean

    public static void initializeDiscoveryListener() {
        if (discoveryListener != null) {
            return;
        }
        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                //GLog.p("Service discovery success" + service);
                if (service.getServiceType().equals(SERVICE_TYPE)) {
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                GLog.n("service lost: " + service);
            }

            //========Control
            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                GLog.p("Service discovery started");
                state = ListenerState.STARTED;
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                GLog.p("Discovery stopped: " + serviceType);
                state = ListenerState.STOPPED;
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                GLog.n("Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
                state = ListenerState.START_FAIL;
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                GLog.n("Discovery failed: Error code:" + errorCode);
                state = ListenerState.STOP_FAIL;

                //nsdManager.stopServiceDiscovery(this);//infinity Loop?
            }
        };
    }
}
