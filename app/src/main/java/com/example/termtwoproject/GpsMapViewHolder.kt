package com.example.termtwoproject

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.SupportMapFragment

class GpsMapViewHolder(drawingRow : View, private val context : Context) : RecyclerView.ViewHolder(drawingRow), OnMapReadyCallback {
    val mapCategoryText = drawingRow.findViewById<TextView>(R.id.rowCategoryText)
    val mapTitleText = drawingRow.findViewById<TextView>(R.id.rowTitleText)
    val mapVotesText = drawingRow.findViewById<TextView>(R.id.rowVotesText)

    //private val mapView = drawingRow.findViewById<MapView>(R.id.mapView)

    var currentMap : GoogleMap? = null


    init {
        val mapFragment = (context as AppCompatActivity).supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        currentMap = googleMap
        val sydney = LatLng(-34.0, 151.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}