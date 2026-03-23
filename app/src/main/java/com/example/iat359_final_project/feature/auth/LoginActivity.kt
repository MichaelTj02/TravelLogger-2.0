package com.example.iat359_final_project.feature.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

}
