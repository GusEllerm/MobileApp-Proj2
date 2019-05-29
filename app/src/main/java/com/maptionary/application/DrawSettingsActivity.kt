package com.maptionary.application

import android.content.Intent
import android.os.Bundle
//import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.maptionary.application.Database.DbWorkerThread
import com.maptionary.application.Database.Drawing
import com.maptionary.application.Database.DrawingsDatabase

class DrawSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_settings)

        // Start the database
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").build()

        // Background thread for Database calls
        val mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()

        val mapTypeSpinner = findViewById<Spinner>(R.id.mapTypeSpinner)
        mapTypeSpinner.adapter = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item,
            AppConstants.MAP_TYPES)

        val lineColorSpinner = findViewById<Spinner>(R.id.lineColorSpinner)
        lineColorSpinner.adapter = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item,
            AppConstants.COLOURS)

        val drawingNameField = findViewById<EditText>(R.id.drawingNameField)

        val startDrawingButton = findViewById<Button>(R.id.startDrawingButton)
        startDrawingButton.setOnClickListener {
            // Save entry to database
            val task = Runnable {
                database.drawingDao().insert(
                    Drawing(
                        title = drawingNameField.text.toString(),
                        mapType = mapTypeSpinner.selectedItem.toString(),
                        lineColor = lineColorSpinner.selectedItem.toString(),
                        folderName = drawingNameField.text.toString() + "_folder",
                        category = "0"
                    )
                )

                val id = database.drawingDao().getLastDrawing().id
                val intent = Intent(this, DrawActivity::class.java)
//                Log.e("ID", "ID of last drawing is $id")
                intent.putExtra("drawingID", id)
                startActivity(intent)
            }
            mDbWorkerThread.postTask(task)
        }
    }
}

