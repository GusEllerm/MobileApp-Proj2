package com.example.termtwoproject

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback

class GpsMapViewHolder(drawingRow : View) : RecyclerView.ViewHolder(drawingRow), OnMapReadyCallback {
    val mapCategoryText = drawingRow.findViewById<TextView>(R.id.rowCategoryText)
    val mapTitleText = drawingRow.findViewById<TextView>(R.id.rowTitleText)
    val mapVotesText = drawingRow.findViewById<TextView>(R.id.rowVotesText)
    private val mapView = drawingRow.findViewById<MapView>(R.id.mapView)
    lateinit var currentMap : GoogleMap

    init {
        if (mapView != null) {
            mapView.onCreate(null)
            mapView.onResume()
            mapView.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        currentMap = googleMap
    }

}