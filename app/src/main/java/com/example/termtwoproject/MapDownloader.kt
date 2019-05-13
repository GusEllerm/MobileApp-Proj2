package com.example.termtwoproject

import android.net.Uri
import android.os.AsyncTask
import com.example.termtwoproject.models.Coordinate
import com.example.termtwoproject.models.Fragment
import com.example.termtwoproject.models.GpsMap
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class MapDownloader(val callback : (List<GpsMap>) -> Unit): AsyncTask<URL, Void, List<GpsMap>>() {



    override fun doInBackground(vararg urls: URL): List<GpsMap> {
        var maps = mutableListOf<GpsMap>()
        if (!urls.first().query.contains("id", true)) {
            // Getting a list of maps
            val result = getJsonAsArray(urls.first())
        } else {
            // getting a single map by id
            val result = getJsonAsObject(urls.first())
            println(result)
            maps.add(getSingleMap(result))
            println(maps[0])
        }


        return maps
    }


    override fun onPostExecute(result: List<GpsMap>) {
        super.onPostExecute(result)
        callback(result)
    }


    private fun getJsonAsObject(url : URL): JSONObject {
        val connection = url.openConnection() as HttpURLConnection
        try {
            val json = BufferedInputStream(connection.inputStream).readBytes().toString(Charset.defaultCharset())
            return JSONObject(json)
        } finally {
            connection.disconnect()
        }
    }

    private fun getJsonAsArray(url : URL): JSONArray {
        val connection = url.openConnection() as HttpURLConnection
        try {
            val json = BufferedInputStream(connection.inputStream).readBytes().toString(Charset.defaultCharset())
            return JSONArray(json)
        } finally {
            connection.disconnect()
        }
    }


    private fun getSingleMap(json : JSONObject): GpsMap {
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