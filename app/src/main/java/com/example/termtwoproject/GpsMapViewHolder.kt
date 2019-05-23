package com.example.termtwoproject


import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GpsMapViewHolder(drawingRow : View) : RecyclerView.ViewHolder(drawingRow) {
    val mapCategoryText = drawingRow.findViewById<TextView>(R.id.rowCategoryText)
    val mapTitleText = drawingRow.findViewById<TextView>(R.id.rowTitleText)
    val mapVotesText = drawingRow.findViewById<TextView>(R.id.rowVotesText)

    val mapPreviewImage = drawingRow.findViewById<ImageView>(R.id.rowPreviewImage)


}