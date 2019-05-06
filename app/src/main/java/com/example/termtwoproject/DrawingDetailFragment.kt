package com.example.termtwoproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_drawing_detail.*
import kotlinx.android.synthetic.main.drawing_detail.view.*

/**
 * A fragment representing a single Drawing detail screen.
 * This fragment is either contained in a [DrawingListActivity]
 * in two-pane mode (on tablets) or a [DrawingDetailActivity]
 * on handsets.
 */
class DrawingDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Drawing? = null

    // ahha //TODO below I am using !!, this is because I need a non-nullable context type - there needs to be a better way of doing this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(activity!!.applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries().build()

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                Log.d("here", "$it")
                item = database.drawingDao().getDrawingById(it.getString(ARG_ITEM_ID).toLong())
                activity?.toolbar_layout?.title = item?.title
            }
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.drawing_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.drawing_detail.text = "${it.title}, mapType: ${it.mapType}, lineColor: ${it.lineColor}, ID: ${it.id}"
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
