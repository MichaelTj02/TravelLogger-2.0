package com.example.iat359_final_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import java.io.IOException

class StepCounterActivity : AppCompatActivity(), SensorEventListener, OnMapReadyCallback {
    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private lateinit var mapView: MapView
    private var map: GoogleMap? = null
    private var stepListener: SensorEventListener? = null
    private lateinit var stepCounterTextView: TextView
    private var isCounterStarted = false
    private var totalSteps = 0
    private var stepOffset = 0
    private lateinit var sessionTitleEditText: EditText
    private lateinit var db: Database
    private var currentPolyline: Polyline? = null
    private val pathCoordinates = arrayListOf<LatLng>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter)

        val finish: Button = findViewById(R.id.btnFinish)
        sessionTitleEditText = findViewById(R.id.sessionTitleEditText)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterTextView = findViewById(R.id.stepCounterText)

        db = Database(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        checkLocationPermission()

        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER && isCounterStarted) {
                    if (stepOffset == 0) {
                        stepOffset = event.values[0].toInt()
                    }
                    val currentSteps = event.values[0].toInt() - stepOffset
                    stepCounterTextView.text = "Step Count: $currentSteps steps"
                    totalSteps = currentSteps
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }

        finish.setOnClickListener {
            finishSession()
            startActivity(Intent(this@StepCounterActivity, MainActivity::class.java))
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (stepCounter != null && stepListener != null) {
            sensorManager.registerListener(stepListener, stepCounter, SensorManager.SENSOR_DELAY_UI)
        }
        mapView.onResume()

        if (!isCounterStarted) {
            isCounterStarted = true
            stepOffset = 0
            if (stepCounter != null && stepListener != null) {
                sensorManager.registerListener(stepListener, stepCounter, SensorManager.SENSOR_DELAY_UI)
            }
        }

        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stepListener?.let { sensorManager.unregisterListener(it) }
        mapView.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == stepCounter && isCounterStarted) {
            if (stepOffset == 0) {
                stepOffset = event.values[0].toInt()
            }
            val currentSteps = event.values[0].toInt() - stepOffset
            stepCounterTextView.text = "Steps: $currentSteps"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    private fun getCurrentSteps(): Int = totalSteps

    private fun finishSession() {
        if (!isCounterStarted) return

        isCounterStarted = false
        val totalFinishSessionSteps = getCurrentSteps()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val locationClient = LocationServices.getFusedLocationProviderClient(this)
            locationClient.getLastLocation().addOnSuccessListener(this) { location ->
                if (location != null) {
                    val city = getCityFromLocation(location.latitude, location.longitude)
                    val sessionTitle = sessionTitleEditText.text.toString()

                    // Keep insert order aligned with Database.insertData(location, steps, sessionTitle).
                    db.insertData(city, totalFinishSessionSteps.toString(), sessionTitle)

                    stepCounterTextView.text = "Session finished. Steps: $totalFinishSessionSteps"
                    stepListener?.let { sensorManager.unregisterListener(it) }

                    val currentMap = map
                    if (pathCoordinates.isNotEmpty() && currentMap != null) {
                        currentMap.addPolyline(
                            PolylineOptions()
                                .addAll(pathCoordinates)
                                .width(12f)
                                .color(Color.BLUE)
                                .geodesic(true)
                        )
                    }
                    resetSteps()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun resetSteps() {
        totalSteps = 0
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        googleMap.setOnMapClickListener { latLng ->
            if (isCounterStarted) {
                pathCoordinates.add(latLng)
                currentPolyline?.remove()
                currentPolyline = googleMap.addPolyline(
                    PolylineOptions()
                        .addAll(pathCoordinates)
                        .width(12f)
                        .color(Color.RED)
                        .geodesic(true)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        stopLocationUpdates()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            showUserLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showUserLocation()
                val currentMap = map
                if (currentMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    currentMap.isMyLocationEnabled = true
                }
            } else {
                Log.d("StepCounterActivity", "Permission denied")
            }
        }
    }

    private fun showUserLocation() {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("StepCounterActivity", "Location permission denied")
            return
        }

        locationClient.getLastLocation()
            .addOnSuccessListener(this) { location ->
                val currentMap = map
                if (location != null && currentMap != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    currentMap.addMarker(MarkerOptions().position(userLatLng).title("You are here"))
                    currentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM_LEVEL))
                } else {
                    Log.d("StepCounterActivity", "Location is null or mMap is null")
                }
            }
            .addOnFailureListener(this) { e ->
                Log.e("StepCounterActivity", "Error getting location", e)
            }
    }

    private fun getCityFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this)
        var city = ""
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                city = addresses[0].locality ?: ""
            }
        } catch (_: IOException) {
        }
        return city
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setInterval(5000)
            .setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    private fun updatePathOnMap() {
        val currentMap = map
        if (currentMap != null && pathCoordinates.isNotEmpty()) {
            currentPolyline?.remove()
            currentPolyline = currentMap.addPolyline(
                PolylineOptions()
                    .addAll(pathCoordinates)
                    .width(12f)
                    .color(Color.RED)
                    .geodesic(true)
            )
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val lastLocation = locationResult.lastLocation
                    if (lastLocation != null) {
                        pathCoordinates.add(LatLng(lastLocation.latitude, lastLocation.longitude))
                        updatePathOnMap()
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(getLocationRequest(), locationCallback!!, null)
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 1
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 123
        private const val DEFAULT_ZOOM_LEVEL = 15f
    }
}
