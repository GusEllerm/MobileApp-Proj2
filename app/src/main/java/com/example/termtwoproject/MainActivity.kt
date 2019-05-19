package com.example.termtwoproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.room.Room
import com.example.termtwoproject.Database.Drawing
import com.example.termtwoproject.Database.DrawingsDatabase
import com.example.termtwoproject.models.GpsMap
import com.example.termtwoproject.models.PostModel

import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.NumberFormatException
import java.net.URL

// Main activity - has navigation to DrawActivity, Gallery
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Set button to start Draw Activity
        val toDrawActivity: Button = findViewById(R.id.DrawActivityButtom)
        val deleteAll: Button = findViewById(R.id.deleteData)
        var addData: Button = findViewById(R.id.addData)
        val getOnlineMapsButton : Button = findViewById(R.id.getOnlineMapsButton)

        // NOTE: //TODO we need to run all queries on a background thread - I am allowing queries on main thread for testing
        // .fallbackToDestructiveMigration() -> lets the room database delete everything in the event of a re configuration
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        deleteAll.setOnClickListener {
            database.drawingDao().deleteAll()
            Toast.makeText(this, "Database cleared", Toast.LENGTH_LONG).show()}
        addData.setOnClickListener {
            val rnds = (0..10).random()
            database.drawingDao().insert(
                Drawing(
                    title = "Test$rnds",
                    lineColor = "Red",
                    mapType = "testmaptype",
                    folderName = "TestFolder",
                    category = 1
                )
            )
            Toast.makeText(this, "item added", Toast.LENGTH_LONG).show()
        }

        val homeButton: Button = findViewById(R.id.homeButton)
        homeButton.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java))}


        toDrawActivity.setOnClickListener { startActivity(Intent(this, DrawActivity::class.java)) }



        getOnlineMapsButton.setOnClickListener {
            val id = 2
            MapDownloader {
                val url = URL("http://192.168.1.100:4567/api/gps_map")
                val model = PostModel(url, it, false, "POST")
                MapApiHandler {

                }.execute(model)
            }.execute(URL("http://192.168.1.100:4567/api/gps_map?id=$id"))


        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


}
