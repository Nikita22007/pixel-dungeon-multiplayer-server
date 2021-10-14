package com.watabou.pixeldungeon.items

import org.json.JSONObject

open class CustomItem() : Item() {
    init {
    }

    protected var info: String = "IDK";

    constructor(obj: JSONObject): this(){
        cursedKnown = true // todo check it
        val it = obj.keys()
        while (it.hasNext()) {
            val token = it.next()
            when (token) {
                "name" -> {
                    name = obj.getString(token)
                }
                "info" -> {
                    info = obj.getString(token);
                }
                "image" -> {
                    image = obj.getInt(token);
                }
                "stackable" -> {
                    stackable = obj.getBoolean(token);
                }
                "quantity" -> {
                    quantity = obj.getInt(token)
                }
                "durability" -> {
                    durability = obj.getInt(token)
                }
                "level" -> {
                    durability = obj.getInt(token)
                }
                "cursed" -> {
                    cursed = obj.getBoolean(token)
                }
            }
        }
    }
}