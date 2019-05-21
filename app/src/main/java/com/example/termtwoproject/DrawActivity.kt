package com.example.termtwoproject

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.util.Log
import androidx.room.Room
import com.example.termtwoproject.Database.DbWorkerThread
import com.example.termtwoproject.Database.Drawing
import com.example.termtwoproject.Database.DrawingsDatabase
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class DrawActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    // Stuff for receiving location updates
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    // TODO Cant have a lateinit variable which is a primitive. Need to fix
    private var drawingID : Long = -1

    // Drawing object
    private lateinit var drawing: Drawing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Callback is used for GPS recording, It also superimposes the lines onto the map
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                lastLocation = p0!!.lastLocation

                // appending each new point to the file
                val coordsFileInput = "${lastLocation.latitude},${lastLocation.longitude}\n"
                Log.i("Appending data to file", "$coordsFileInput added to file")
                applicationContext.openFileOutput(drawing.title, Context.MODE_APPEND).use {
                    it.write(coordsFileInput.toByteArray())
                }
                val current_file = File(applicationContext.filesDir, drawing.title)
                displayCoords(current_file)
            }
        }

        createLocationRequest()
    }

    private fun getDrawing(database: DrawingsDatabase) {
        //TODO - I am cheating here, the database calls are happining on the main thread - this needs to be fixed
        drawing = database.drawingDao().getDrawingById(drawingID)
        Log.d("Drawing Loaded", "${drawing.title} has been successfully retrieved")

        // Everything is ready to set map up
        setUpMap()
    }

    private fun displayCoords(currentFile: File) {
        // Takes each line in file, converts it to a LatLng object and passes it into a list
        val list: MutableList<LatLng> = ArrayList()
        currentFile.forEachLine {
            val (lat, lng) = it.split(",")
            val value = LatLng(lat.toDouble(), lng.toDouble())
            list.add(value)
        }

        // Set up polyline options
        val polylineOptions = PolylineOptions()
            .addAll(list)
            .geodesic(true)
            .color(Color.BLUE)
            .width(30f)
            .jointType(ROUND)

        // Apply line to map
        map.addPolyline(polylineOptions)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        // set drawing id. If no extras send to DrawSettingsActivity
        if (intent.extras == null) {
            startActivity(Intent(this, DrawSettingsActivity::class.java))
        } else {
            //TODO have some sort of error checking for -1 value
            drawingID = intent.getLongExtra("drawingID", -1)
            Log.d("Drawing ID", "$drawingID - ID of drawing")
        }

        // TODO - this is running on the main thread which makes it blocking! needs to be run on a different thread
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings").allowMainThreadQueries().build()
        getDrawing(database)
    }

    override fun onMarkerClick(p0: Marker?) = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // This function executes if it recieves a RESULT_OK for a REQUEST_CHECK_SETTINGS request from createLocationRequest
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == REQUEST_CHECK_SETTINGS) {
            locationUpdateState = true
            startLocationUpdates()
        }
    }

    override fun onPause() {
        // Stops the location update requests
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        // Restarts the location update requests
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    companion object {
        // companion object used in setUpMap, createLocationRequest, onActivityResult
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
//        private const val FILE_NAME = "test2.txt"
    }

    private fun setUpMap() {
        // Checks if the permission ACCESS_FINE_LOCATION has been granted - this permission lets us get long/lat of user
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Enables a layer which displays a light blue dot on the user's location. Layer comes with a button which
        // recenter's map to users location
        map.isMyLocationEnabled = true

        // ----------------- user preference: the map type should be changeable from preferences -------------//
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN

        // get last known location
        fusedLocationClient.lastLocation.addOnSuccessListener(this) {location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                // Places custom marker
                placeMarkerOnMap(currentLatLng)
                // Sets custom user icon
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }
        }

        // Creation of file to be used to record long/lat pairs
        if (!fileExist()) {
            //If the file does not exist at launch, create it
            Log.d("File Creation", "${drawing.title} Created")
            val file = File(applicationContext.filesDir, drawing.title)
            file.createNewFile()
        } else {
            Log.d("File Exists", "File already exists")
        }
    }

    private fun fileExist(): Boolean {
        // Checks if the file has been made previously
        val file: File = applicationContext.getFileStreamPath(drawing.title)
        return file.exists()
    }

    private fun placeMarkerOnMap(location: LatLng) {
        // create markerOptions object & set user's current location as the position
        val markerOptions = MarkerOptions().position(location)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)
        ))
        map.addMarker(markerOptions)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /*looper*/)
    }

    private fun createLocationRequest() {
        // Make locationRequest, add it to LocationSettingsRequest.Builder & retrieve and handle changes based of users settings
        locationRequest = LocationRequest()
        // Specifies what rate this app would LIKE to receive updates
        locationRequest.interval = 500
        // Rate of which the app can handle updates
        locationRequest.fastestInterval = 100
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // Everything below this point is handling the case where the users location settings are off
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not correct
                // This can be corrected - try user prompt
                try {
                    // Show dialog via startResolutionForResult()
                    // Check result using onActivityResult()
                    e.startResolutionForResult(this@DrawActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore
                }
            }
        }
    }
}
