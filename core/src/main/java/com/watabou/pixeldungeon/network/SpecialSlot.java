package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.items.Item;

public class SpecialSlot {
    int id;
    String sprite = "items.png";
    int image_id = 127;
    Item item = null;

    public SpecialSlot(int id, String sprite, int image_id, Item item) {
        this.id = id;
        this.sprite = sprite;
        this.image_id = image_id;
        this.item = item;
    }
}
