package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.actors.hero.HeroClass;

import static com.watabou.pixeldungeon.network.Client.packet;

public class SendData {
    //---------------------------Hero
    public static void SendHeroClass(HeroClass heroClass) {
        Client.sendHeroClass(heroClass);
    }

    public static void SendCellListenerCell(Integer cell) {
        packet.packAndAddCellListenerCell(cell);
    }
}
