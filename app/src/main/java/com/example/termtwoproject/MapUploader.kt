package com.example.termtwoproject

import android.os.AsyncTask
import com.example.termtwoproject.models.GpsMap
import com.example.termtwoproject.models.PostModel
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


class MapUploader(val callback : (Int) -> Unit): AsyncTask<PostModel, Void, Int>() {

    override fun doInBackground(vararg model : PostModel): Int {
        val m = model.first()
        if (m.vote) {
            // method is a adding or removing a vote
            val result = voteOnMap(m.url, m.method)
            println(result)
        } else {
            // method is for saving or updating a gps map
        }
        return 1
    }


    override fun onPostExecute(result: Int) {
        super.onPostExecute(result)
        callback(result)
    }


    private fun voteOnMap(url : URL, method : String): JSONObject {
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = method
            val json = BufferedInputStream(inputStream).readBytes().toString(Charset.defaultCharset())
            return JSONObject(json)
        }

    }
}