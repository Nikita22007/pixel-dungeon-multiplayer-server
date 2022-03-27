package com.nikita22007.pixeldungeonmultiplayer

import org.json.JSONArray
import java.lang.UnsupportedOperationException
import java.util.ArrayList

// only namespace for public static methods
class Utils private constructor() {
    companion object {
        @JvmStatic
        fun parseArrayOfPath(arrayOfPath: JSONArray): List<List<Int>> {
            val result: MutableList<List<Int>> = ArrayList()
            for (i in 0 until arrayOfPath.length()) {
                result.add(parsePath(arrayOfPath.getJSONArray(i)))
            }
            return result
        }

        private fun parsePath(pathArray: JSONArray): List<Int> {
            val result: MutableList<Int> = ArrayList()
            for (i in 0 until pathArray.length()) {
                result.add(pathArray.getInt(i))
            }
            return result
        }

    }

    init {
        // disallow creating new exemplars of class
        throw UnsupportedOperationException()
    }
}