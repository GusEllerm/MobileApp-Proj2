package com.example.termtwoproject

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.termtwoproject.models.GpsMap
import java.net.URL

class GpsMapAdapter(private val context: Context): RecyclerView.Adapter<GpsMapViewHolder>() {

    var maps = mutableListOf<GpsMap>()
    var mapIds = mutableListOf<Int>()

    override fun getItemCount(): Int = mapIds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpsMapViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_drawing, parent, false)
        val holder = GpsMapViewHolder(view)
        view.setOnClickListener {
            //val intent = Intent(context, newclass)
            //intent.putextra
            //startactivity
        }
        return holder
    }

    override fun onBindViewHolder(holder: GpsMapViewHolder, i: Int) {
        val mapId : Int = mapIds[i]
        val query = "?id=$mapId"
        val url = URL(AppConstants.GPS_END + query)
        MapDownloader {
            holder.mapCategoryText.text = it.category
            holder.mapTitleText.text = it.title
            holder.mapVotesText.text = it.votes.toString()
        }.execute(url)

    }


    fun setMapIds(ids : ArrayList<Int>) {
        mapIds.clear()
        mapIds.addAll(ids)
        notifyDataSetChanged()
    }

}