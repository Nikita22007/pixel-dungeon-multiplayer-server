package com.watabou.pixeldungeon.network;

import android.util.Log;

//This class scans multicast ip
//It search servers
//If it  found server, he put this in serverList  array
//Every 15 seconds it deletes all elements from list

//TODO ALL ELEMENTS STATIC!!!

/*TODO LIST
    * Search servers
    * Clear serverList every 15seconds
 */
public class Scanner { //Todo write this

    private static int UPDATE_TIME =  15; //in seconds
    private static ServerInfo[] serverList;
    private static boolean started;

    public static ServerInfo[] getServerList(){
        return serverList;
    }

    public static void start(){
        if (started){
            Log.i("Network","Already started!");
        }
        else{
            started=true;
            //todo Start
        }
    };

    public static void stop(){
        if (started){
            started=false;
            serverList=null;
    }
        else{
            Log.i("Network","Already stopped!");
        //todo Start
    }

    };
}
