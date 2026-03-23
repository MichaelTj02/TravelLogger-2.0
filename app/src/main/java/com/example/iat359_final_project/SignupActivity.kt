package com.example.iat359_final_project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginText: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", DEFAULT)
        val password = sharedPreferences.getString("password", DEFAULT)

        if (username != DEFAULT && password != DEFAULT) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val signupButton: Button = findViewById(R.id.signupButton)
        signupButton.setOnClickListener { signUpUser() }

        loginText = findViewById(R.id.loginText)
        loginText.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        }
    }

    private fun signUpUser() {
        val sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("username", usernameEditText.text.toString())
            .putString("password", passwordEditText.text.toString())
            .commit()

        startActivity(Intent(this, LoginActivity::class.java))
    }

    companion object {
        const val DEFAULT = "not available"
    }
}
