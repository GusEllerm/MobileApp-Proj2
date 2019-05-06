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

        // To do background threading make a runnable and execute it with the DbWorkerThread class (it dispatches threads)
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries().build()


        myDrawingsButton.setOnClickListener {
            val intent = Intent(this, DrawingListActivity::class.java)
            startActivity(intent)
        }

    }
}