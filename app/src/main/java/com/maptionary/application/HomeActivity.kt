package com.maptionary.application

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.room.Room
import com.maptionary.application.Database.DrawingsDatabase
import com.maptionary.application.DrawingListDetailActivity.DrawingListActivity


class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val myDrawingsButton = findViewById<Button>(R.id.myDrawingsButton)

        val globalDrawingsButton = findViewById<Button>(R.id.globalDrawingsButton)


        // To do background threading make a runnable and execute it with the DbWorkerThread class (it dispatches threads)
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries().build()

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)



        myDrawingsButton.setOnClickListener {
            val intent = Intent(this, DrawingListActivity::class.java)
            startActivity(intent)
        }

        globalDrawingsButton.setOnClickListener {
            val intent = Intent(this, GlobalDrawingsActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    companion object {
        val DARK_MODE_KEY_PREF: String = "darkMode" // id of the pref
    }
}