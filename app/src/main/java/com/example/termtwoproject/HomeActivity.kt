package com.example.termtwoproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import androidx.room.Room


class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val myDrawingsButton = findViewById<Button>(R.id.myDrawingsButton)
//        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").build()
//        val addData = findViewById<Button>(R.id.dummy_data)
//
//        var drawing = Drawing()

//        addData.setOnClickListener { //TODO }

        myDrawingsButton.setOnClickListener {
            val intent = Intent(this, DrawSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}