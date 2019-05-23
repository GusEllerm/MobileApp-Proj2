package com.example.termtwoproject

import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.room.Room
import com.example.termtwoproject.Database.Drawing
import com.example.termtwoproject.Database.DrawingsDatabase
import com.example.termtwoproject.Dialogues.*
import com.example.termtwoproject.models.Coordinate
import com.example.termtwoproject.models.Fragment
import com.example.termtwoproject.models.GpsMap
import com.example.termtwoproject.models.PostModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import com.google.android.material.snackbar.Snackbar
import org.apache.commons.io.FilenameUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Collections.max


class DrawActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, CreateNewLineDialog.NewLineDialogListner,
EditLineDialog.EditDialogListener, ViewLineDialog.ViewLineDialogListener, DeleteLineDialog.DeleteLineDialogListner, UploadLineDialog.UploadLineDialogListener {


    // TODO - when we create the fragments - should be instant only gets made when data recorded

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    // Stuff for receiving location updates
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false


    // TODO Cant have a lateinit variable which is a primitive. Need to fix
    private var drawingID : Long = -1

    private lateinit var currentFragmentPath: String
    private lateinit var currentFragment: String

    // Drawing object
    private lateinit var drawing: Drawing

    // Start/Stop recording button
    private lateinit var startRecordingButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        recordLocation = false

        startRecordingButton = findViewById(R.id.StartRecording)
        startRecordingButton.setOnClickListener { view ->
            when (recordLocation) {
                true -> {
                    recordLocation = false
                    startRecordingButton.text = resources.getString(R.string.drawing_activity_buttonStateFalse)
                }
                false -> {
                    recordLocation = true
                    startRecordingButton.text = resources.getString(R.string.drawing_activity_buttonStateTrue)
                }
            }

            Snackbar.make(view, "This happened", Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .show()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun stopRecording() {
        if (recordLocation) {
            // If recording stop
            startRecordingButton.performClick()
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_LONG).show()
        }
    }

    override fun deleteFragment(fragNumber: String) {
        stopRecording()
        if (getFragmentNames().size > 1) {
            val fragNum = fragNumber.takeLast(1).toInt()
            val fileToDelete = File("${applicationContext.filesDir}$currentFragmentPath", "$fragNum.txt")

            try {
                fileToDelete.delete()
            } catch (e: Exception) {
                //TODO - what if the file cant be deleted?
            }

            // reoder and rename the remaining files so they are sequential 1 - 10
            filesRenameReorder(fragNum)

            Toast.makeText(this, "$fragNum was selected", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("Fragment amount", "There are ${getFragmentNames().size} fragments in this drawing")
            Toast.makeText(this, "You cant delete the last line! If you want to delete the drawing please do so " +
                    "from the list", Toast.LENGTH_LONG).show()
        }
    }

    private fun filesRenameReorder(removedItem: Int) {
        val fragments = getFragmentNames()
        // Make sure fragments are in order
        fragments.sort()

        // Rename files in order
        for (fragment in fragments) {
            if (fragment.toInt() > removedItem) {
                val currentFrag = File("${applicationContext.filesDir}$currentFragmentPath", "$fragment.txt")
                currentFrag.renameTo(File("${applicationContext.filesDir}$currentFragmentPath",
                    "${fragment.toInt() - 1}.txt"))
            }
        }

    }

    override fun viewFragment() {
        stopRecording()
        // The currentfragment should always be selected - and not be unselectable
        Toast.makeText(this, "View x fragments", Toast.LENGTH_SHORT).show()
    }

    override fun newFragment() {
        stopRecording()

        if (getFragmentNames().size < 9) {
            // makeFileStructure should create a new fragment, as the directory must already exist
            try {
                makeFileStructure(true)
            } finally {
                val currentFrags = getFragmentNames()
                Toast.makeText(
                    this,
                    "Line ${currentFrags[currentFrags.lastIndex].toInt()} has been created",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Log.d("Fragment amount", "There are ${getFragmentNames().size} fragments")
            Toast.makeText(this, "You cant have more than 9 lines!", Toast.LENGTH_LONG).show()
        }
    }

    override fun editFragment() {
        stopRecording()
        Toast.makeText(this, "Edit selected Fragment", Toast.LENGTH_SHORT).show()
    }

    override fun uploadGpsMap(gpsMap : GpsMap?) {
        stopRecording()
        if (gpsMap != null) {
            val url = URL(AppConstants.GPS_END)
            val model = PostModel(url, gpsMap, false, "POST")
            MapApiHandler {
                // id of new item maybe store it in database?
            }.execute(model)
        } else {
            // TODO error message?
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.New -> openNewDialog()
            R.id.View -> openViewDialog()
            R.id.Edit -> openEditDialog()
            R.id.Delete -> openDeleteDialog()
            R.id.Upload -> openUploadDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openDeleteDialog() {
        val dialog: DeleteLineDialog = DeleteLineDialog()
        var bundle: Bundle = Bundle()
        val fragments = ArrayList<Int>()
        for (fragment in getFragmentNames()) {
            fragments.add(fragment.toInt())
        }
        bundle.putIntegerArrayList("fragments",fragments)
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "Delete Line")
    }

    private fun openViewDialog() {
        val dialog: ViewLineDialog = ViewLineDialog()
        var bundle: Bundle = Bundle()
        val fragments = ArrayList<Int>()
        for (fragment in getFragmentNames()) {
            fragments.add(fragment.toInt())
        }
        bundle.putIntegerArrayList("fragments", fragments)
        bundle.putInt("currentFragment", Character.getNumericValue(currentFragment[0]))
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "View Lines")
    }

    private fun openEditDialog() {
        val dialog: EditLineDialog = EditLineDialog()
        var bundle: Bundle = Bundle()
        val fragments = ArrayList<Int>()
        for (fragment in getFragmentNames()) {
            fragments.add(fragment.toInt())
        }
        bundle.putIntegerArrayList("fragments", fragments)
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "Edit Line")
    }

    private fun openUploadDialog() {
        val dialog= UploadLineDialog()
        val bundle = Bundle()
        val fragments = loadFragments()
        val gpsMap = GpsMap(-1, drawing.title, drawing.mapType, fragments.size,
            drawing.category.toString(), 0, fragments, "")


        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * .10).toInt()
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(gpsMap.getBounds(), width, height, padding))

        val callback = GoogleMap.SnapshotReadyCallback {
            if (it != null) {
                val resizedBitMap = Bitmap.createScaledBitmap(it, 256, 256, false)
                val stream = ByteArrayOutputStream()
                resizedBitMap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val image = stream.toByteArray()
                val imageAsBase64 = Base64.encodeToString(image, Base64.DEFAULT)
                gpsMap.imageData = imageAsBase64

                val mapAsString = gpsMap.toJSON().toString()
                bundle.putString("GpsMap", mapAsString)
                dialog.arguments = bundle
                dialog.show(supportFragmentManager, "Upload Drawing")
            } else {
                // cant generate snapshot
            }
        }
        map.setOnMapLoadedCallback {
            map.snapshot(callback)
        }
    }


    /**
     * Helper functions to load fragments into a object that can be send to the database
     */
    private fun loadFragments(): List<Fragment> {
        val directory = File(applicationContext.filesDir, drawing.folderName)
        val fragments = mutableListOf<Fragment>()
        if (directory.exists()) {
            directory.listFiles().forEach {
                if (it != null) {
                    val fragment = Fragment(-1, "RED", 10, loadCoordinates(it))
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
        var sequence : Int = 1
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

    private fun openNewDialog() {
        val dialog: CreateNewLineDialog = CreateNewLineDialog()
        dialog.show(supportFragmentManager, "New Line")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_drawing, menu)
        return true
    }

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
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings")
            .allowMainThreadQueries()
            .build()

        // set global drawing object - used to retrieve id, map type, fragment amount, directory name ect
        getDrawing(database)

        // Create / read file structure - sets current fragment
        makeFileStructure()

        // Set location callback and start recording to currentFragment file
        makeLocationCallback()
    }

    private fun makeLocationCallback() {
        // Callback is used for GPS recording, It also superimposes the lines onto the map
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                if (recordLocation) {
                    lastLocation = p0!!.lastLocation

                    // appending each new point to the file
                    val coordsFileInput = "${lastLocation.latitude},${lastLocation.longitude}\n"
                    Log.i("Appending data to file", "$coordsFileInput added to file $currentFragment")

                    //TODO - test is not a valid name
                    val test = FileOutputStream(
                        File("${applicationContext.filesDir}$currentFragmentPath", currentFragment),
                        true
                    )
                    test.write(coordsFileInput.toByteArray())
                    test.close()

                    val current_file = File("${applicationContext.filesDir}$currentFragmentPath", currentFragment)
                    displayCoords(current_file)
                }
            }
        }

        createLocationRequest()
    }

    private fun makeFileStructure(fromNew: Boolean = false) {
        currentFragmentPath = "/${drawing.folderName}/"

        if (!directoryExists()) {
            // Base case - new drawing
            makedirectory()
        } else {
            val fragments = getFragmentNames()
            // TODO may need to reorganise the file names if a fragment has been deleted
            // EG fragments 123 - 2 gets deleted = 13, max = 3 next frag = 4. should be max = 2 next frag = 3
            // Add a new file if asked - if from another activity just start editing the last line
            val lastFragment: Int
            when (fromNew) {
                true -> {
                    lastFragment = max(fragments).toInt() + 1
                    Log.d("Function executed from", "Create New Fragment (fragment $lastFragment created)")
                }
                false -> {
                    lastFragment = max(fragments).toInt()
                    Log.d("Function executed from", "entering activity (fragment $lastFragment selected)")
                }
            }
            currentFragment = "$lastFragment.txt"
            makefile()
        }
//        makefile()
    }

    private fun getFragmentNames(): MutableList<String> {
        val directory = File(applicationContext.filesDir, drawing.folderName)
        val fragments : MutableList<String> = ArrayList()
        for (file in directory.listFiles()) {
            fragments.add(FilenameUtils.getBaseName(file.toString()))
        }
        return fragments
    }

    private fun directoryExists(): Boolean {
        // Checks if the directory has been made previously
        val directory = File(applicationContext.filesDir, drawing.folderName)
        return directory.exists()
    }

    private fun fileExist(): Boolean {
        // Checks if the file has been made previously
        val file = File("${applicationContext.filesDir}$currentFragmentPath", currentFragment)
        return file.exists()
    }

    private fun makedirectory() {
        if (!directoryExists()) {
            Log.d("Directory creation", "${drawing.folderName} Created")
            val file = File(applicationContext.filesDir, drawing.folderName)
            file.mkdir()

            // If directory did not exist - no fragment exists, create first fragment
            // If it is the first fragment its path is
            currentFragment = "1.txt"
            Log.d("Fragment path", "Current path is $currentFragmentPath$currentFragment")
            makefile()
        } else {
            Log.d("Directory Exists", "Directory already exists")
        }
    }

    private fun makefile() {
        if (!fileExist()) {
            //If the file does not exist at launch, create it
            Log.d("File Creation", "$currentFragmentPath Created")
            Log.d("Trying to make file", "File name = ${applicationContext.filesDir}$currentFragmentPath")
            val file = File("${applicationContext.filesDir}$currentFragmentPath", currentFragment)
            file.createNewFile()
        } else {
            Log.d("File Exists", "File already exists")
        }
    }








    /////////////////////////// Functions do not effect file structure ////////////////////////////////////
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

    override fun onPause() {
        // Stops the location update requests
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        // Restarts the location update requests
        super.onResume()
        recordLocation = false
        if (!locationUpdateState) {
            createLocationRequest()
        }
    }

    private fun getDrawing(database: DrawingsDatabase) {
        //TODO - I am cheating here, the database calls are happining on the main thread - this needs to be fixed
        drawing = database.drawingDao().getDrawingById(drawingID)
        Log.d("Drawing Loaded", "${drawing.title} has been successfully retrieved")

        // Everything is ready to set map up
        setUpMap()
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
    }

    companion object {
        // companion object used in setUpMap, createLocationRequest, onActivityResult
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private var recordLocation = false
    }

}
