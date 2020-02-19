package com.watabou.pixeldungeon.Network;

import com.watabou.pixeldungeon.scenes.GameScene;

public class ServerClientThread extends ClientThread {  //debug
    public  ServerClientThread(){
        super(0,null);

    }
    public <T> void  send(int code, T ...  data) {
        switch (code){
            case Codes.BOSS_SLAIN:{GameScene.ClientBossSlain();break;}
        }

    }
    public void disconnect(){};
}
