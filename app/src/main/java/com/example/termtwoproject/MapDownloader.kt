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

class MapDownloader(val callback : (GpsMap) -> Unit): AsyncTask<URL, Void, GpsMap>() {



    override fun doInBackground(vararg urls: URL): GpsMap {
        val result = getJsonAsObject(urls.first())
        return GpsMap.getGpsMapFromJSON(result)

    }


    override fun onPostExecute(result: GpsMap) {
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



}