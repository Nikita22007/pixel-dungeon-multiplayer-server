package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.CustomItem;

import static com.watabou.pixeldungeon.network.Client.flush;
import static com.watabou.pixeldungeon.network.Client.packet;

public class SendData {
    //---------------------------Hero
    public static void SendHeroClass(HeroClass heroClass) {
        Client.sendHeroClass(heroClass);
    }

    public static void SendCellListenerCell(Integer cell) {
        packet.packAndAddCellListenerCell(cell);
        flush();
    }

    public static void SendItemAction(CustomItem item, Hero hero, String action) {
        packet.packAndAddUsedAction(item, action, hero);
        flush();
    }

    public static void sendWindowResult(int id, int result) {
        if (-1 == id){
            return; //internal window
        }
        packet.packAndAddWindowsResult(id, result, null);
        flush();
    }
}
