package com.maptionary.application


import android.os.AsyncTask
import com.maptionary.application.models.GpsMap
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
        } catch (e : Exception) {
            //Error a toast?
        } finally {
            connection.disconnect()
        }
        return JSONObject(AppConstants.ERROR_JSON_STRING_GPSMAP)
    }



}