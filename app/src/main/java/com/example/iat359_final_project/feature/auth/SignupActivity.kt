package com.example.iat359_final_project.feature.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginText: TextView
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val repository = AuthRepositoryImpl(applicationContext)
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository)
        )[AuthViewModel::class.java]

        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)

        val signupButton: Button = findViewById(R.id.signupButton)
        signupButton.setOnClickListener {
            viewModel.onEvent(
                AuthUiEvent.SubmitSignup(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            )
        }

        loginText = findViewById(R.id.loginText)
        loginText.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        }

        observeUiState()
        viewModel.onEvent(AuthUiEvent.CheckSignupGate)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: AuthUiState) {
        when (state.navigation) {
            AuthNavigation.OpenLogin -> {
                startActivity(Intent(this, LoginActivity::class.java))
                viewModel.onEvent(AuthUiEvent.ConsumeNavigation)
            }
            AuthNavigation.OpenMain,
            AuthNavigation.OpenSignup,
            AuthNavigation.None -> Unit
        }
    }
}
