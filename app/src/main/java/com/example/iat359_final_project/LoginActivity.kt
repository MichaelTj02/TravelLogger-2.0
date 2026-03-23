package com.example.iat359_final_project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpText: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (!checkAudioPermission()) {
            requestAudioPermission()
        }

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        usernameEditText = findViewById(R.id.editTextUserLogin)
        passwordEditText = findViewById(R.id.editTextPassLogin)

        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener { loginUser() }

        signUpText = findViewById(R.id.signUpText)
        signUpText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            MY_PERMISSIONS_REQUEST_RECORD_AUDIO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser() {
        val sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE)
        val username = sharedPrefs.getString("username", DEFAULT)
        val password = sharedPrefs.getString("password", DEFAULT)

        if (usernameEditText.text.toString() == username && passwordEditText.text.toString() == password) {
            Toast.makeText(this, "Login Success", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            sharedPrefs.edit()
                .putString("username", DEFAULT)
                .putString("password", DEFAULT)
                .commit()

            Toast.makeText(this, "Credentials Reset", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2
        const val DEFAULT = "not available"
    }
}
