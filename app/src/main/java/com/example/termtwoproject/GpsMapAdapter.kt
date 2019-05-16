package com.example.termtwoproject

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.termtwoproject.models.GpsMap
import com.example.termtwoproject.models.PostModel
import java.net.URL

class GpsMapAdapter(private val context: Context): RecyclerView.Adapter<GpsMapViewHolder>() {

    var mapIds = mutableListOf<Int>()

    override fun getItemCount(): Int = mapIds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpsMapViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_drawing, parent, false)
        val holder = GpsMapViewHolder(view)
        val voteButton = view.findViewById<Button>(R.id.voteButton)
        voteButton.setOnClickListener {
            val id = mapIds[holder.adapterPosition]
            val url = URL(AppConstants.GPS_VOTE_END + "?id=" + id)
            val model = PostModel(url, null, true, "POST")
            MapUploader {
                notifyItemChanged(holder.adapterPosition)
            }.execute(model)
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