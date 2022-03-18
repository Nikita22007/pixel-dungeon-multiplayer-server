package com.watabou.pixeldungeon.items

import com.watabou.pixeldungeon.actors.hero.Hero
import com.watabou.pixeldungeon.items.bags.CustomBag
import com.watabou.pixeldungeon.network.SendData.SendItemAction
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

open class CustomItem() : Item() {
    protected var descString: String? = null

    protected var actionsList: ArrayList<String> = ArrayList();

    protected var identified = false;

    constructor(obj: JSONObject) : this() {
        cursedKnown = true // todo check it
        update(obj)

    }

    override fun update(obj: JSONObject) {
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
                "quantity", "count" -> {
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
                "identified" -> {
                    identified = obj.getBoolean(token)
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

    override fun isIdentified(): Boolean {
        return identified;
    }

    override fun execute(hero: Hero, action: String) {
        SendItemAction(this, hero, action)
        //super.execute(hero, action)
    }

}