package com.maptionary.application

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.maptionary.application.models.PostModel
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
            MapApiHandler {
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
            val bytes = Base64.decode(it.imageData, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            holder.mapPreviewImage.setImageBitmap(bitmap)
        }.execute(url)


    }


    fun setMapIds(ids : ArrayList<Int>) {
        mapIds.clear()
        mapIds.addAll(ids)
        notifyDataSetChanged()
    }

}