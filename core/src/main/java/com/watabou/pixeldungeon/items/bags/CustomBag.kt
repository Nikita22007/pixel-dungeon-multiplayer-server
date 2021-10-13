package com.watabou.pixeldungeon.items.bags

import com.watabou.pixeldungeon.items.CustomItem
import org.json.JSONArray
import org.json.JSONObject

class CustomBag(obj: JSONObject) : Bag() {
    init {
        cursedKnown = true // todo check it
        val it = obj.keys()
        while (it.hasNext()) {
            val token = it.next()
            when (token) {
                "name" -> {
                    name = obj.getString(token)
                }
                "info" -> {
                //     info = obj.getString(token);
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
                "size" -> {
                    size = obj.getInt(token)
                }
                "items" -> {
                    clear()
                    addItemsFromJSONArray(obj.getJSONArray(token))
                }
            }
        }
    }

    private fun addItemsFromJSONArray(arr: JSONArray) {
        for (i in 0 until arr.length()) {
            val itemObj = arr.getJSONObject(i);
            var item = CustomItem(itemObj)
            items.add(item)
        }
    }
}