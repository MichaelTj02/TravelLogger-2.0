package com.example.iat359_final_project.feature.logs

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iat359_final_project.R
import com.example.iat359_final_project.data.local.Database
import com.example.iat359_final_project.domain.model.LogEntry
import com.example.iat359_final_project.feature.logs.data.LogsRepositoryImpl
import com.example.iat359_final_project.feature.logs.presentation.LogsUiEvent
import com.example.iat359_final_project.feature.logs.presentation.LogsUiState
import com.example.iat359_final_project.feature.logs.presentation.LogsViewModel
import com.example.iat359_final_project.feature.logs.presentation.LogsViewModelFactory
import kotlinx.coroutines.launch

class ViewLogsActivity : AppCompatActivity() {
    private lateinit var myRecycler: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: LogsViewModel

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_logs)

        val repository = LogsRepositoryImpl(Database(this))
        viewModel = ViewModelProvider(
            this,
            LogsViewModelFactory(repository)
        )[LogsViewModel::class.java]

        myRecycler = findViewById(R.id.recyclerViewLogs)
        customAdapter = CustomAdapter(mutableListOf())

        val btnDeleteAll: Button = findViewById(R.id.btnDeleteAll)
        btnDeleteAll.setOnClickListener {
            viewModel.onEvent(LogsUiEvent.DeleteAllLogs)
        }

        myRecycler.adapter = customAdapter
        layoutManager = LinearLayoutManager(this)
        myRecycler.layoutManager = layoutManager

        customAdapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onDeleteItemClick(item: LogEntry) {
                viewModel.onEvent(LogsUiEvent.DeleteLog(item.sessionTitle))
            }
        })

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val locationQuery = searchEditText.text.toString()
            viewModel.onEvent(LogsUiEvent.SearchLogs(locationQuery))
        }

        observeUiState()
        viewModel.onEvent(LogsUiEvent.LoadLogs)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: LogsUiState) {
        customAdapter.updateDataSet(state.logs)
        state.message?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(LogsUiEvent.ConsumeMessage)
        }
    }
}
