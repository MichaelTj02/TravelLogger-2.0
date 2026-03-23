package com.example.iat359_final_project.feature.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.iat359_final_project.R
import com.example.iat359_final_project.data.local.Database
import com.example.iat359_final_project.feature.logs.ViewLogsActivity
import com.example.iat359_final_project.feature.tracking.MapsActivity
import com.example.iat359_final_project.feature.tracking.StepCounterActivity
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var stepListener: SensorEventListener? = null
    private var stepCounterTextView: TextView? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isCounterStarted = false
    private var totalSteps = 0
    private var finalTotalSteps = 0
    private var stepOffset = 0
    private lateinit var db: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCheckLogs: ImageButton = findViewById(R.id.btnCheckLogs)
        val btnViewMap: ImageButton = findViewById(R.id.btnViewMap)
        val btnStartSession: ImageButton = findViewById(R.id.btnStartSession)
        val btnLocInfo: ImageButton = findViewById(R.id.btnViewInformation)

        db = Database(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        btnCheckLogs.setOnClickListener {
            startActivity(Intent(this@MainActivity, ViewLogsActivity::class.java))
        }

        btnViewMap.setOnClickListener {
            startActivity(Intent(this@MainActivity, MapsActivity::class.java))
        }

        btnStartSession.setOnClickListener {
            startActivity(Intent(this@MainActivity, StepCounterActivity::class.java))
        }

        btnLocInfo.setOnClickListener {
            performWebSearch()
        }

        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER && isCounterStarted) {
                    if (stepOffset == 0) {
                        stepOffset = event.values[0].toInt()
                    }
                    val currentSteps = event.values[0].toInt() - stepOffset
                    stepCounterTextView?.text = "Steps: $currentSteps"
                    totalSteps = currentSteps
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION
        )

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            MY_PERMISSIONS_REQUEST_RECORD_AUDIO
        )

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                startListening()
            }

            override fun onError(error: Int) {
                startListening()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processVoiceCommand(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }
        })

        startVoiceRecognition()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == stepCounter && isCounterStarted) {
            if (stepOffset == 0) {
                stepOffset = event.values[0].toInt()
            }
            val currentSteps = event.values[0].toInt() - stepOffset
            finalTotalSteps = currentSteps
            stepCounterTextView?.text = "Steps: $currentSteps"
        }
    }

    override fun onResume() {
        super.onResume()
        if (accelerometer != null && gyroscope != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (stepCounter != null && stepListener != null) {
            sensorManager.registerListener(stepListener, stepCounter, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    override fun onPause() {
        super.onPause()
        if (accelerometer != null && gyroscope != null) {
            sensorManager.unregisterListener(this)
        }
        stepListener?.let { sensorManager.unregisterListener(it) }
        speechRecognizer.stopListening()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION -> Unit
            MY_PERMISSIONS_REQUEST_RECORD_AUDIO -> Unit
        }
    }

    private fun performWebSearch() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this) { location ->
            if (location != null) {
                val city = getCityFromLocation(location.latitude, location.longitude)
                val query = "https://www.google.com/search?q=$city weather today"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(query)))
            }
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

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something")
        }
        speechRecognizer.startListening(intent)
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something")
        }
        speechRecognizer.startListening(intent)
    }

    private fun processVoiceCommand(spokenText: String) {
        val command = spokenText.lowercase(Locale.getDefault())
        when {
            command.contains("check logs") -> {
                startActivity(Intent(this@MainActivity, ViewLogsActivity::class.java))
            }
            command.contains("view map") -> {
                startActivity(Intent(this@MainActivity, MapsActivity::class.java))
            }
            command.contains("start session") -> {
                startActivity(Intent(this@MainActivity, StepCounterActivity::class.java))
            }
            command.contains("view information") -> {
                performWebSearch()
            }
            else -> {
                Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 1
        private const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2
    }
}
