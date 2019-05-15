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
        val debugButton: Button = findViewById(R.id.debug)
        val debugDelete: Button = findViewById(R.id.debug_delete)
        val deleteAll: Button = findViewById(R.id.deleteData)
        var addData: Button = findViewById(R.id.addData)
        val getOnlineMapsButton : Button = findViewById(R.id.getOnlineMapsButton)

        // NOTE: //TODO we need to run all queries on a background thread - I am allowing queries on main thread for testing
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries().build()

        deleteAll.setOnClickListener {
            database.drawingDao().deleteAll()
            Toast.makeText(this, "Database cleared", Toast.LENGTH_LONG).show()}
        addData.setOnClickListener {
            val rnds = (0..10).random()
            database.drawingDao().insert(
                Drawing(
                    title = "Test$rnds",
                    fragmentAmount = 0,
                    lineColor = "Red",
                    mapType = "testmaptype"
                )
            )
            Toast.makeText(this, "item added", Toast.LENGTH_LONG).show()
        }

        val homeButton: Button = findViewById(R.id.homeButton)
        homeButton.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java))}

        val mapIdText : EditText = findViewById(R.id.mapIdText)

        toDrawActivity.setOnClickListener { startActivity(Intent(this, DrawActivity::class.java)) }

        // If you try and print the file when it is deleted the app will crash as a null pointer exception
        debugButton.setOnClickListener { File(applicationContext.filesDir, FILE_NAME).forEachLine { println(it) } }
        debugDelete.setOnClickListener { File(applicationContext.filesDir, FILE_NAME).delete() }


        getOnlineMapsButton.setOnClickListener {
            // Only works locally atm
            var mapId : Int = -1
            try {
                mapId = Integer.parseInt(mapIdText.text.toString())
            } catch (e: NumberFormatException) {
                // can't parse keep it empty
            }
            var query = ""
            if (mapId > 0) query = "?id=$mapId"
            val url = URL(AppConstants.GPS_END + query)

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

    companion object {
        // This needs to be the same as the FileName used in Draw Activity
        private const val FILE_NAME = "test2.txt"
    }


}
