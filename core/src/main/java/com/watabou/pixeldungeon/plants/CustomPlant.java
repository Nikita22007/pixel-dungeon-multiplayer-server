package com.watabou.pixeldungeon.plants;

public class CustomPlant extends Plant {
    String desc;

    public CustomPlant(int imageID, int pos, String name, String desc) {
        image = imageID;
        this.pos = pos;
        plantName = name;
        this.desc = desc;
    }

    @Override
    public String desc() {
        return desc;
    }
}
