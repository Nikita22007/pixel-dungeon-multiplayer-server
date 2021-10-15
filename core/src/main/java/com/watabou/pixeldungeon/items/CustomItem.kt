package com.watabou.pixeldungeon.items

import com.watabou.pixeldungeon.actors.hero.Hero
import com.watabou.pixeldungeon.items.bags.CustomBag
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

open class CustomItem() : Item() {
    protected var descString: String? = null

    protected var actionsList: ArrayList<String> = ArrayList();


    constructor(obj: JSONObject) : this() {
        cursedKnown = true // todo check it
        val it = obj.keys()
        while (it.hasNext()) {
            val token = it.next()
            when (token) {
                "name" -> {
                    name = obj.getString(token)
                }
                "info" -> {
                    descString = obj.getString(token);
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
                "actions" -> {
                    parseActions(obj.getJSONArray(token))
                }
                "default_action" -> {
                    val action: String = obj.getString(token);
                    defaultAction = if (action == "null") {
                        null
                    } else {
                        action
                    };
                }
            }
        }
    }

    private fun parseActions(actionsArr: JSONArray) {
        val actions = ArrayList<String>(actionsArr.length());
        for (i in 0 until actionsArr.length()) {
            val action = actionsArr.getString(i);
            actions.add(action)
        }
        actionsList = actions;
    }

    override fun desc(): String {
        return descString ?: "idk,wtf"
    }

    override fun actions(hero: Hero?): ArrayList<String> {
        return actionsList.clone() as ArrayList<String>
    }

}