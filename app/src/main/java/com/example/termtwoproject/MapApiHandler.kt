package com.example.termtwoproject

import android.os.AsyncTask
import com.example.termtwoproject.models.GpsMap
import com.example.termtwoproject.models.PostModel
import com.google.android.gms.common.util.JsonUtils
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


class MapApiHandler(val callback : (Int) -> Unit): AsyncTask<PostModel, Void, Int>() {

    override fun doInBackground(vararg model : PostModel): Int {
        val m = model.first()
        if (m.vote) {
            // method is a adding or removing a vote
            val result = voteOnMap(m.url, m.method)
            println(result)
        } else {
            // method is for saving or updating a gps map
            val result = postOrPatchMap(m)
            println(result)
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

    private fun postOrPatchMap(model : PostModel) : JSONObject {
        with(model.url.openConnection() as HttpURLConnection) {
            requestMethod = model.method
            setRequestProperty("Content-Type", "application/json")
            val postData : ByteArray = model.map!!.toJSON().toString().toByteArray(Charset.defaultCharset())

            val outputStream : DataOutputStream = DataOutputStream(outputStream)
            outputStream.write(postData)
            outputStream.flush()
            val json = BufferedInputStream(inputStream).readBytes().toString(Charset.defaultCharset())
            return JSONObject(json)
        }
    }
}