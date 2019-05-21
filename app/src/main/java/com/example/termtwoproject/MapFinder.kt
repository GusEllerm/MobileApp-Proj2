package com.example.termtwoproject

import android.os.AsyncTask
import org.json.JSONArray
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class MapFinder(val callback : (List<Int>) -> Unit): AsyncTask<URL, Void, List<Int>>() {


    override fun doInBackground(vararg urls: URL): List<Int> {
        val result = getJson(urls.first())
        return getIdList(result)

    }


    override fun onPostExecute(result: List<Int>) {
        super.onPostExecute(result)
        callback(result)
    }


    private fun getJson(url : URL): JSONArray {
        val connection = url.openConnection() as HttpURLConnection
        try {
            val json = BufferedInputStream(connection.inputStream).readBytes().toString(Charset.defaultCharset())
            return JSONArray(json)
        } finally {
            connection.disconnect()
        }
    }


    private fun getIdList(jsonArray : JSONArray): List<Int> {
        val ids = mutableListOf<Int>()
        for (i in 0..(jsonArray.length() - 1)) {
            ids.add(jsonArray.getInt(i))
        }
        return ids
    }



}