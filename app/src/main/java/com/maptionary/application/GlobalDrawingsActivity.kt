package com.maptionary.application

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.URL

class GlobalDrawingsActivity : AppCompatActivity() {

    private lateinit var gpsMapAdapter : GpsMapAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_drawings)

        val categoryPicker = findViewById<Spinner>(R.id.categoryPicker)
        val orderPicker = findViewById<Spinner>(R.id.orderPicker)

        categoryPicker.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            ArrayList<String>(AppConstants.CATEGORIES.keys))
        orderPicker.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            ArrayList<String>(AppConstants.ORDER_TYPES.keys))
        orderPicker.setSelection(0)


        gpsMapAdapter = GpsMapAdapter(this)
        val drawingsView = findViewById<RecyclerView>(R.id.globalMapsView)
        val layoutManager = LinearLayoutManager(this)
        drawingsView.layoutManager = layoutManager
        drawingsView.adapter = gpsMapAdapter

        updateList(orderPicker, categoryPicker)


        orderPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateList(orderPicker, categoryPicker)
            }

        }
    }


    private fun updateList(orderPicker : Spinner, categoryPicker : Spinner) {
        val query : String = "?order=${AppConstants.ORDER_TYPES[orderPicker.selectedItem]}&" +
                "category=${AppConstants.CATEGORIES[categoryPicker.selectedItem]}"
        val url = URL(AppConstants.GPS_LIST_END + query)
        MapFinder {
            gpsMapAdapter.setMapIds(ArrayList(it))
        }.execute(url)
    }

}