package com.example.termtwoproject


import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.row_drawing.view.*

class GpsMapViewHolder(drawingRow : View) : RecyclerView.ViewHolder(drawingRow) {
    val mapCategoryText = drawingRow.findViewById<TextView>(R.id.rowCategoryText)
    val mapTitleText = drawingRow.findViewById<TextView>(R.id.rowTitleText)
    val mapVotesText = drawingRow.findViewById<TextView>(R.id.rowVotesText)

    //private val mapView = drawingRow.findViewById<MapView>(R.id.mapView)


    //val map = drawingRow.findViewById<MapView>(R.id.rowMap)



}