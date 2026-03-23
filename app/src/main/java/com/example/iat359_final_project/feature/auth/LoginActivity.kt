package com.example.iat359_final_project.feature.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.iat359_final_project.R
import com.example.iat359_final_project.feature.auth.data.AuthRepositoryImpl
import com.example.iat359_final_project.feature.auth.presentation.AuthNavigation
import com.example.iat359_final_project.feature.auth.presentation.AuthUiEvent
import com.example.iat359_final_project.feature.auth.presentation.AuthUiState
import com.example.iat359_final_project.feature.auth.presentation.AuthViewModel
import com.example.iat359_final_project.feature.auth.presentation.AuthViewModelFactory
import com.example.iat359_final_project.feature.home.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpText: TextView
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (!checkAudioPermission()) {
            requestAudioPermission()
        }

        val repository = AuthRepositoryImpl(applicationContext)
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository)
        )[AuthViewModel::class.java]

        usernameEditText = findViewById(R.id.editTextUserLogin)
        passwordEditText = findViewById(R.id.editTextPassLogin)

        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            viewModel.onEvent(
                AuthUiEvent.SubmitLogin(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            )
        }

        signUpText = findViewById(R.id.signUpText)
        signUpText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

        observeUiState()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: AuthUiState) {
        state.message?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            viewModel.onEvent(AuthUiEvent.ConsumeMessage)
        }
        when (state.navigation) {
            AuthNavigation.OpenMain -> {
                startActivity(Intent(this, MainActivity::class.java))
                viewModel.onEvent(AuthUiEvent.ConsumeNavigation)
            }
            AuthNavigation.OpenSignup -> {
                startActivity(Intent(this, SignupActivity::class.java))
                viewModel.onEvent(AuthUiEvent.ConsumeNavigation)
            }
            AuthNavigation.OpenLogin,
            AuthNavigation.None -> Unit
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

    companion object {
        private const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2
    }
}
