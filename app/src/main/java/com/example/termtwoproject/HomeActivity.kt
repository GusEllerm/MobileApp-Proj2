package com.example.termtwoproject

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText


class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val myDrawingsButton = findViewById<Button>(R.id.myDrawingsButton)


        myDrawingsButton.setOnClickListener {
            val intent = Intent(this, DrawSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}