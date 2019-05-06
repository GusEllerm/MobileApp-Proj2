package com.example.termtwoproject.DrawingListDetailActivity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import com.example.termtwoproject.Database.Drawing
import com.example.termtwoproject.Database.DrawingsDatabase
import com.example.termtwoproject.Database.DbWorkerThread
import com.example.termtwoproject.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_drawing_detail.*
import kotlinx.android.synthetic.main.drawing_detail.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * A fragment representing a single Drawing detail screen.
 * This fragment is either contained in a [DrawingListActivity]
 * in two-pane mode (on tablets) or a [DrawingDetailActivity]
 * on handsets.
 */
class DrawingDetailFragment : Fragment(), OnMapReadyCallback {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Drawing? = null
    private lateinit var thread: DbWorkerThread
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val database = Room.databaseBuilder(activity!!.applicationContext, DrawingsDatabase::class.java, "drawings").build()
        val thread = DbWorkerThread("dbWorkerThread")
        thread.start()


        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                Log.d("here", "$it")

                runBlocking {
                    val job =  launch(Dispatchers.Default) {
                        item = database.drawingDao().getDrawingById(it.getString(ARG_ITEM_ID).toLong())
                        activity?.toolbar_layout?.title = item?.title
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        thread = DbWorkerThread("dbWorkerThread")
        thread.start()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.drawing_detail, container, false)

        thread = DbWorkerThread("dbWorkerThread")
        thread.start()


        // Show the fields of drawing object
//        item?.let {
//            rootView.drawing_detail.text = "${it.title}, mapType: ${it.mapType}, lineColor: ${it.lineColor}, ID: ${it.id}"
//        }

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
