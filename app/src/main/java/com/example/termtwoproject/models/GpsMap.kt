package com.example.termtwoproject.models

import android.util.JsonWriter
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter


data class GpsMap(val id : Int, val title : String, val type : String, val fragmentAmount : Int, val category : String,
                  var votes : Int, val fragments : List<Fragment>) {


    fun toJSON() : JSONObject {
        val rootObject = JSONObject()
        rootObject.put("id", id)
        rootObject.put("title", title)
        rootObject.put("mapType", type)
        rootObject.put("fragmentAmount", fragmentAmount)
        rootObject.put("category", category)
        rootObject.put("votes", votes)
        val fragmentArray = JSONArray()
        fragments.forEach {
            val jsonFragment = JSONObject()
            jsonFragment.put("lineColour", it.lineColour)
            jsonFragment.put("zoom", it.zoom)
            val jsonCoordinates = JSONArray()
            it.coordinates.forEach { seq ->
                val jsonCoord = JSONObject()
                jsonCoord.put("sequence", seq.sequence)
                jsonCoord.put("longitude", seq.longitude)
                jsonCoord.put("latitude", seq.latitude)
                jsonCoordinates.put(jsonCoord)
            }
            jsonFragment.put("coordinates", jsonCoordinates)
            fragmentArray.put(jsonFragment)
        }
        rootObject.put("fragments", fragmentArray)
        return rootObject
    }



    companion object {
        fun getGpsMapFromJSON(json : JSONObject) : GpsMap {
            val id : Int = json.getInt("id")
            val mapType : String = json.getString("mapType")
            val mapTitle : String = json.getString("mapTitle")
            val category : String = json.getString("category")
            val votes : Int = json.getInt("votes")

            //get fragments and coordinates
            val numbersOfFragments : Int = json.getInt("fragmentAmount")
            val fragments = json.getJSONArray("fragments")
            val fragmentList = mutableListOf<Fragment>()
            for (i in 0..(numbersOfFragments - 1)) {
                val currentFragment = fragments.getJSONObject(i)
                val lineColour : String = currentFragment.getString("lineColour")
                val zoom : Int = currentFragment.getInt("zoom")

                val coordinateJSON = currentFragment.getJSONArray("coordinates")
                val coordinateList = mutableListOf<Coordinate>()
                for (j in 0..(coordinateJSON.length() - 1)) {
                    val currentCoordinate = coordinateJSON.getJSONObject(j)
                    coordinateList.add(Coordinate(
                        currentCoordinate.getDouble("longitude"),
                        currentCoordinate.getDouble("latitude"),
                        currentCoordinate.getInt("sequence")
                    ))
                }
                fragmentList.add(Fragment(lineColour, zoom, coordinateList))
            }
            return GpsMap(id, mapTitle, mapType, numbersOfFragments, category, votes, fragmentList)
        }
    }

}




