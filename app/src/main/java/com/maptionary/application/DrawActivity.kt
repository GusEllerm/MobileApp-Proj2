package com.maptionary.application

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
//import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.maptionary.application.Database.Drawing
import com.maptionary.application.Database.DrawingsDatabase
import com.maptionary.application.Dialogues.*
import com.maptionary.application.models.Coordinate
import com.maptionary.application.models.Fragment
import com.maptionary.application.models.GpsMap
import com.maptionary.application.models.PostModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import com.maptionary.application.AppConstants.MAP_TYPES_MAP
import org.apache.commons.io.FilenameUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Collections.max
import kotlin.math.absoluteValue


class DrawActivity : AppCompatActivity(), SensorEventListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, CreateNewLineDialog.NewLineDialogListner,
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

    private lateinit var line_sig: TextView


    private var mSensorManager : SensorManager? = null
    private var mAccelerometer : Sensor? = null
    private var hitBounds : Int = 0
    private var eventsSinceShake : Int = 0
    private var totalShakes : Double = 0.0

    private var uploadDialogOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        recordLocation = false

        line_sig = findViewById(R.id.lineNum)


        startRecordingButton = findViewById(R.id.StartRecording)
        startRecordingButton.setOnClickListener { _ ->
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
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && !uploadDialogOpen) {
            // working with y values for the shake
            val minAccel : Float = AppConstants.GRAVITY - AppConstants.MAX_ACCELERATION
            val maxAccel : Float = AppConstants.GRAVITY + AppConstants.MAX_ACCELERATION
            val curAccel : Float = event.values[1].absoluteValue
            if (hitBounds != 0) eventsSinceShake += 1
            if (curAccel < minAccel) {
                if (hitBounds == 1) totalShakes += 0.5
                hitBounds = -1
            }
            else if (curAccel > maxAccel) {
                if (hitBounds == -1) totalShakes += 0.5
                hitBounds = 1
            }
            if (totalShakes >= AppConstants.MAX_SHAKES) {
                // open upload
                openUploadDialog()
                hitBounds = 0
                totalShakes = 0.0
                eventsSinceShake = 0
            } else if (eventsSinceShake >= AppConstants.MAX_EVENTS) {
                // reset
                hitBounds = 0
                totalShakes = 0.0
                eventsSinceShake = 0
            }
        }
    }



    private fun updateLineNum() {
        val text = "Line ${currentFragment[0]}"
        line_sig.text = text
    }

    private fun stopRecording() {
        if (recordLocation) {
            // If recording stop
            startRecordingButton.performClick()
            Toast.makeText(this, getString(R.string.toast_recording_stopped), Toast.LENGTH_LONG).show()
        }
    }

    override fun deleteFragment(fragNumber: String) {
        stopRecording()
        // TODO - make it so you cant delete the line you are currently editing

        val fragNum = fragNumber.takeLast(1).toInt()

        if (fragNum.toString() != currentFragment[0].toString()) {
            if (getFragmentNames().size > 1) {
                val fileToDelete = File("${applicationContext.filesDir}$currentFragmentPath", "$fragNum.txt")

                try {
                    fileToDelete.delete()
                } catch (e: Exception) {
                    //TODO - what if the file cant be deleted?
                }

                // reoder and rename the remaining files so they are sequential 1 - 10
                filesRenameReorder(fragNum)
                updateLineNum() // For UI
                val text = String.format(getString(R.string.toast_fragment_selected), fragNum)
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            } else {
//            Log.d("Fragment amount", "There are ${getFragmentNames().size} fragments in this drawing")
                Toast.makeText(this, getString(R.string.toast_error_deleting_last_line), Toast.LENGTH_LONG).show()
            }
        } else {
            // Trying to delete the line that they are editing
            Toast.makeText(this, getString(R.string.toast_deleting_editing_line), Toast.LENGTH_LONG).show()
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
        if (currentFragment[0].toInt() > removedItem) {
            currentFragment = "${currentFragment[0].toString().toInt() - 1}.txt"
        }
    }

    override fun viewFragment(selectedFragments: MutableList<Int>, unselectedFragments: MutableList<Int>) {
        stopRecording()
        viewFragments = selectedFragments
        if (unselectedFragments.size > 0) {
//            Log.d("asd", "THIS HERE ${unselectedFragments[0]}")
            // Need to get rid of line from drawing
            for (unselectedLine in unselectedFragments) {
                current_polylines[unselectedLine]?.remove()
                current_polylines.remove(unselectedLine)
            }
        }
        // The currentfragment should always be selected - and not be unselectable
        Toast.makeText(this, getString(R.string.toast_view_fragments), Toast.LENGTH_SHORT).show()
    }

    override fun newFragment() {
        stopRecording()

        if (getFragmentNames().size < 9) {
            // makeFileStructure should create a new fragment, as the directory must already exist
            try {
                makeFileStructure(true)
            } finally {
                val currentFrags = getFragmentNames()
                val text = String.format(getString(R.string.toast_line_created), currentFrags[currentFrags.lastIndex].toInt())
                Toast.makeText(
                    this,
                    text,
                    Toast.LENGTH_SHORT
                ).show()
                currentFragment = "${currentFrags[currentFrags.lastIndex]}.txt" // Set currentfragment to new fragment
                updateLineNum() // Update UI
            }
        } else {
//            Log.d("Fragment amount", "There are ${getFragmentNames().size} fragments")
            Toast.makeText(this, getString(R.string.toast_max_lines), Toast.LENGTH_LONG).show()
        }
    }

    override fun editFragment(fragNumber: Int) {
        stopRecording()
        currentFragment = "$fragNumber.txt"
        updateLineNum()
        val text = String.format(getString(R.string.toast_line_edit), fragNumber)
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }


    override fun exitDialog(dialog : DialogInterface?) {
        dialog?.dismiss()
        uploadDialogOpen = false
    }

    override fun uploadGpsMap(gpsMap : GpsMap?) {
        stopRecording()
        uploadDialogOpen = false
        if (gpsMap != null) {
            val url = URL(AppConstants.GPS_END)
            val model = PostModel(url, gpsMap, false, "POST")
            MapApiHandler {
                // id of new item maybe store it in database?
                if (it >= 0) {

                    val not_intent = Intent(this, GlobalDrawingsActivity::class.java)
                    val not_intent_pending = PendingIntent.getActivity(this, 0, not_intent, 0)


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val name = getString(R.string.channel_name)
                        val descriptionText = getString(R.string.channel_description)
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val channel = NotificationChannel(getString(R.string.channel_name), name, importance).apply {
                            description = descriptionText
                        }
                        // Register the channel with the system
                        val notificationManager: NotificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    val builder = NotificationCompat.Builder(this, getString(R.string.channel_name))
                        .setSmallIcon(R.mipmap.launcher_icon)
                        .setContentTitle("This worked")
                        .setContentText(getString(R.string.toast_success_uploading_map))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(not_intent_pending)
                        .setAutoCancel(true)

                    with(NotificationManagerCompat.from(this)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(R.string.channel_name, builder.build())
                    }
                } else {
                    Toast.makeText(this, getString(R.string.toast_error_uploading_map), Toast.LENGTH_SHORT).show()
                }
            }.execute(model)
        } else {
           Toast.makeText(this, getString(R.string.toast_error_parsing_map), Toast.LENGTH_LONG).show()
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
        val dialog = DeleteLineDialog()
        val bundle = Bundle()
        val fragments = ArrayList<Int>()
        for (fragment in getFragmentNames()) {
            fragments.add(fragment.toInt())
        }
        bundle.putIntegerArrayList("fragments",fragments)
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "Delete Line")
    }

    private fun openViewDialog() {
        val dialog = ViewLineDialog()
        val bundle = Bundle()
        val fragments = ArrayList<Int>()
        for (fragment in getFragmentNames()) {
            fragments.add(fragment.toInt())
        }
        bundle.putIntegerArrayList("fragments", fragments)
        val viewFragmentsIntArray = arrayListOf<Int>()
        for (item in viewFragments) {
            viewFragmentsIntArray.add(item)
        }
        bundle.putIntegerArrayList("viewFragments", viewFragmentsIntArray)
        bundle.putInt("currentFragment", Character.getNumericValue(currentFragment[0]))
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "View Lines")
    }

    private fun openEditDialog() {
        val dialog  = EditLineDialog()
        val bundle = Bundle()
        val fragments = ArrayList<Int>()
        for (fragment in getFragmentNames()) {
            fragments.add(fragment.toInt())
        }
        bundle.putIntegerArrayList("fragments", fragments)
        bundle.putInt("currentFragment", Character.getNumericValue(currentFragment[0]))
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "Edit Line")
    }

    private fun openUploadDialog() {
        uploadDialogOpen = true
        val dialog = UploadLineDialog()
        val bundle = Bundle()
        val fragments = loadFragments()
        val gpsMap = GpsMap(-1, drawing.title, drawing.mapType, fragments.size,
            drawing.category, 0, fragments, "")


        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * .10).toInt()
        if (fragments.isNotEmpty()) map.animateCamera(CameraUpdateFactory.newLatLngBounds(gpsMap.getBounds(), width, height, padding))
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

    private fun openNewDialog() {
        val dialog = CreateNewLineDialog()
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
//            Log.d("Drawing ID", "$drawingID - ID of drawing")
        }

        // TODO - this is running on the main thread which makes it blocking! needs to be run on a different thread
        val database = Room.databaseBuilder(applicationContext, DrawingsDatabase::class.java, "drawings")
            .allowMainThreadQueries()
            .build()

        // set global drawing object - used to retrieve id, map type, fragment amount, directory name ect
        getDrawing(database)

        // Create / read file structure - sets current fragment
        makeFileStructure()
        viewFragments.clear()
        viewFragments.add(Character.getNumericValue(currentFragment[0]))

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
//                    Log.i("Appending data to file", "$coordsFileInput added to file $currentFragment")

                    //TODO - test is not a valid name
                    val test = FileOutputStream(
                        File("${applicationContext.filesDir}$currentFragmentPath", currentFragment),
                        true
                    )
                    test.write(coordsFileInput.toByteArray())
                    test.close()
                }
                displayCoords()
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
            // EG fragments 123 - 2 gets deleted = 13, max = 3 next frag = 4. should be max = 2 next frag = 3
            // Add a new file if asked - if from another activity just start editing the last line
            val lastFragment: Int
            when (fromNew) {
                true -> {
                    lastFragment = max(fragments).toInt() + 1
//                    Log.d("Function executed from", "Create New Fragment (fragment $lastFragment created)")
                }
                false -> {
                    lastFragment = max(fragments).toInt()
//                    Log.d("Function executed from", "entering activity (fragment $lastFragment selected)")
                }
            }
            currentFragment = "$lastFragment.txt"
            updateLineNum() // Update UI
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
            val file = File(applicationContext.filesDir, drawing.folderName)
            file.mkdir()

            // If directory did not exist - no fragment exists, create first fragment
            // If it is the first fragment its path is
            currentFragment = "1.txt"
            updateLineNum() // Update UI
            makefile()
        } else {
//            Log.d("Directory Exists", "Directory already exists")
        }
    }

    private fun makefile() {
        if (!fileExist()) {
            val file = File("${applicationContext.filesDir}$currentFragmentPath", currentFragment)
            file.createNewFile()
        } else {
//            Log.d("File Exists", "File already exists")
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

    private fun displayCoords() {
        // Takes each line in file, converts it to a LatLng object and passes it into a list

        // Set line color to prefrence
        val mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val lineColor_string = mSharedPreferences.getString(getString(R.string.color_prefrence), "#2980B9")
        val lineColor = Color.parseColor(lineColor_string)

        for (fragment in getFragmentNames()) {
            val fragment_int = fragment.toInt()
            if (viewFragments.contains(fragment_int)) {
                val currentFile = File("${applicationContext.filesDir}$currentFragmentPath", "$fragment.txt")
                val list: MutableList<LatLng> = ArrayList()
                currentFile.forEachLine {
                    val (lat, lng) = it.split(",")
                    val value = LatLng(lat.toDouble(), lng.toDouble())
                    list.add(value)
                }

                // add polyline to map so we can get rid of it when we dont want to view it
                Log.d("test", "///////////////////////////////////////////////////////////////")
                current_polylines.forEach { Log.d("test", ("${it.key} ${it.value}")) }
                Log.d("test", "///////////////////////////////////////////////////////////////")

                if (current_polylines[fragment_int] == null) {
                    val polylineOptions = PolylineOptions()
                        .addAll(list)
                        .geodesic(true)
                        .color(lineColor)
                        .width(30f)
                        .jointType(ROUND)
                    // Apply line to map
                    val polyline = map.addPolyline(polylineOptions)

                    // add polyline to map
                    current_polylines[fragment_int] = polyline
                }

                // Update the current line that you are editing
                if (fragment == currentFragment[0].toString()) {
                    current_polylines[fragment_int]?.remove()
                    current_polylines.remove(fragment_int)

                    val polylineOptions = PolylineOptions()
                        .addAll(list)
                        .geodesic(true)
                        .color(lineColor)
                        .width(30f)
                        .jointType(ROUND)
                    // Apply line to map
                    val polyline = map.addPolyline(polylineOptions)

                    // add polyline to map
                    current_polylines[fragment_int] = polyline
                }
            }
        }
    }

    override fun onPause() {
        // Stops the location update requests
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        mSensorManager!!.unregisterListener(this)
    }

    public override fun onResume() {
        // Restarts the location update requests
        super.onResume()
        recordLocation = false
        if (!locationUpdateState) {
            createLocationRequest()
        }
        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI)

    }

    private fun getDrawing(database: DrawingsDatabase) {
        //TODO - I am cheating here, the database calls are happining on the main thread - this needs to be fixed
        drawing = database.drawingDao().getDrawingById(drawingID)

        // Everything is ready to set map up
        //TODO - make sure that this runs after drawing is set
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
        map.mapType = MAP_TYPES_MAP[drawing.mapType]!!.toInt()
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
        private var viewFragments = mutableListOf<Int>()
        private var current_polylines = mutableMapOf<Int, Polyline>()
    }

}
