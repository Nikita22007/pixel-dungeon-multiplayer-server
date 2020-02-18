package com.watabou.pixeldungeon.Network;

public class ServerClientThread extends ClientThread {  //debug
    public  ServerClientThread(){
        super(0,null);

    }
    public <T> void  send(int code, T ...  data) {
    }
    public void disconnect(){};
}
