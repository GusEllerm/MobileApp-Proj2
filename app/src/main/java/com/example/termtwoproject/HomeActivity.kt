package com.example.termtwoproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.room.Room
import com.example.termtwoproject.Database.DrawingsDatabase
import com.example.termtwoproject.DrawingListDetailActivity.DrawingListActivity


class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val myDrawingsButton = findViewById<Button>(R.id.myDrawingsButton)

        // NOTE: //TODO we need to run all queries on a background thread - I am allowing queries on main thread for testing
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries().build()


        myDrawingsButton.setOnClickListener {
            val intent = Intent(this, DrawingListActivity::class.java)
            startActivity(intent)
        }

    }
}