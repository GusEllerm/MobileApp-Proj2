package com.example.termtwoproject

import android.os.AsyncTask
import com.example.termtwoproject.models.PostModel
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


class MapApiHandler(val callback : (Int) -> Unit): AsyncTask<PostModel, Void, Int>() {

    override fun doInBackground(vararg model : PostModel): Int {
        val m = model.first()
        var result : JSONObject
        if (m.vote) {
            // method is a adding or removing a vote
            result = voteOnMap(m.url, m.method)
        } else {
            // method is for saving or updating a gps map
            result = postOrPatchMap(m)
        }
        return result.getInt("id")
    }


    override fun onPostExecute(result: Int) {
        super.onPostExecute(result)
        callback(result)
    }


    private fun voteOnMap(url : URL, method : String): JSONObject {
        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = method
                val json = BufferedInputStream(inputStream).readBytes().toString(Charset.defaultCharset())
                return JSONObject(json)
            }
        } catch (e : Exception) {
            //toast error?
        }
        return JSONObject(AppConstants.ERROR_JSON_STRING_OBJECT)
    }

    private fun postOrPatchMap(model : PostModel) : JSONObject {
        try {
            with(model.url.openConnection() as HttpURLConnection) {
                requestMethod = model.method
                setRequestProperty("Content-Type", "application/json")
                val postData: ByteArray = model.map!!.toJSON().toString().toByteArray(Charset.defaultCharset())

                val outputStream: DataOutputStream = DataOutputStream(outputStream)
                outputStream.write(postData)
                outputStream.flush()
                val json = BufferedInputStream(inputStream).readBytes().toString(Charset.defaultCharset())
                return JSONObject(json)
            }
        } catch (e : Exception) {
            //toast error?
        }
        return JSONObject(AppConstants.ERROR_JSON_STRING_OBJECT)
    }
}