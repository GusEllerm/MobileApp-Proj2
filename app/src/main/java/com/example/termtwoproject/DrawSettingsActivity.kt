package com.example.termtwoproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

class DrawSettingsActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_settings)

        val mapSettings = findViewById<Spinner>(R.id.mapTypeSpinner)
        mapSettings.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, AppConstants.MAP_TYPES)

        val drawingNameField = findViewById<EditText>(R.id.drawingNameField)



        val startDrawingButton = findViewById<Button>(R.id.startDrawingButton)
        startDrawingButton.setOnClickListener {
            val intent = Intent(this, DrawActivity::class.java)
            intent.putExtra("drawingName", drawingNameField.text.toString()) // field needs to be a string otherwise it passes null to drawActivity
            startActivity(intent)
        }
    }

}