package com.example.termtwoproject.models

import org.json.JSONObject



data class GpsMap(val id : Int, val title : String, val type : String, val fragmentAmount : Int, val category : String,
                  val votes : Int, val fragments : List<Fragment>) {


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




