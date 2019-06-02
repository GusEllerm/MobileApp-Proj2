package com.maptionary.application.DrawingListDetailActivity

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import com.maptionary.application.Database.Drawing
import com.maptionary.application.Database.DrawingsDatabase
import com.maptionary.application.Database.DbWorkerThread
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_drawing_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.maptionary.application.R
import com.maptionary.application.models.Coordinate
import com.maptionary.application.models.GpsMap
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.lang.IllegalStateException


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
    private lateinit var drawing: Drawing
    private lateinit var thread: DbWorkerThread
    private lateinit var mMap: GoogleMap
    private lateinit var mMapView: MapView

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
//                Log.d("here", "$it")

                runBlocking {
                    val job =  launch(Dispatchers.Default) {
                        drawing = database.drawingDao().getDrawingById(it.getString(ARG_ITEM_ID).toLong())
                        activity?.toolbar_layout?.title = drawing.title
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false)

        val fragments = loadFragments()
        val frag_names = getFragmentNames()
        val gpsMap = GpsMap(-1, drawing.title, drawing.mapType, fragments.size,
            drawing.category, 0, fragments, "")


        try {

            val mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val lineColor_string = mSharedPreferences.getString(getString(R.string.color_prefrence), "#2980B9")
            Log.d("Current", lineColor_string)
            val lineColor = Color.parseColor(lineColor_string)

            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val padding = (width * .10).toInt()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(gpsMap.getBounds(), width, height, padding))

            for (fragment in frag_names) {
                val currentFile = File("${context?.filesDir}/${drawing.folderName}", "$fragment.txt")
                val list: MutableList<LatLng> = ArrayList()
                currentFile.forEachLine {
                    val (lat, lng) = it.split(",")
                    val value = LatLng(lat.toDouble(), lng.toDouble())
                    list.add(value)
                }

                val polylineOptions = PolylineOptions()
                    .addAll(list)
                    .geodesic(true)
                    .color(lineColor)
                    .width(30f)
                    .jointType(JointType.ROUND)
                // Apply line to map
                val polyline = mMap.addPolyline(polylineOptions)
            }
        } catch (e: IllegalStateException) {
            // No points in file
        }

    }

    private fun getFragmentNames(): MutableList<String> {
        val directory = File(context?.filesDir, drawing.folderName)
        val fragments : MutableList<String> = ArrayList()
        for (file in directory.listFiles()) {
            fragments.add(FilenameUtils.getBaseName(file.toString()))
        }
        return fragments
    }

    private fun loadFragments(): List<com.maptionary.application.models.Fragment> {
        val directory = File(context?.filesDir, drawing.folderName)
        val fragments = mutableListOf<com.maptionary.application.models.Fragment>()
        if (directory.exists()) {
            directory.listFiles().forEach {
                if (it != null) {
                    val fragment = com.maptionary.application.models.Fragment(-1, "RED", 10, loadCoordinates(it))
                    if (fragment.coordinates.isNotEmpty()) {
                        fragments.add(fragment)
                    }
                }
            }
        }
        return fragments
    }

    private fun loadCoordinates(file : File) : List<Coordinate> {
        val coords = mutableListOf<Coordinate>()
        var sequence = 1
        file.forEachLine {
            val strCoords = it.split(",")
            if (strCoords.size == 2) {
                val longitude = strCoords[1].toDouble()
                val latitude = strCoords[0].toDouble()
                coords.add(Coordinate(-1, longitude, latitude, sequence))
                sequence ++
            }
        }
        return coords
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(com.maptionary.application.R.layout.drawing_detail, container, false)
        thread = DbWorkerThread("dbWorkerThread")
        thread.start()
        try {
            MapsInitializer.initialize(this.activity)
            mMapView = rootView.findViewById(com.maptionary.application.R.id.mapFragment) as MapView
            mMapView.onCreate(savedInstanceState)
            mMapView.getMapAsync(this)
        } finally {
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        thread = DbWorkerThread("dbWorkerThread")
        thread.start()
        mMapView.onResume()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        /**
         * The fragment argument representing the drawing ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
