package com.example.iat359_final_project.feature.logs

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iat359_final_project.R
import com.example.iat359_final_project.core.constants.Constants
import com.example.iat359_final_project.data.local.Database
import com.example.iat359_final_project.data.local.DatabaseHelper
import com.example.iat359_final_project.domain.model.LogEntry

class ViewLogsActivity : Activity() {
    private lateinit var myRecycler: RecyclerView
    private lateinit var db: Database
    private lateinit var customAdapter: CustomAdapter
    private lateinit var helper: DatabaseHelper
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_logs)

        myRecycler = findViewById(R.id.recyclerViewLogs)
        db = Database(this)
        helper = DatabaseHelper(this)

        val btnDeleteAll: Button = findViewById(R.id.btnDeleteAll)
        btnDeleteAll.setOnClickListener {
            deleteAllLogs()
            customAdapter.notifyDataSetChanged()
        }

        val cursor = db.getData()
        val index1 = cursor.getColumnIndex(Constants.SESSION_TITLE)
        val index2 = cursor.getColumnIndex(Constants.LOCATION)
        val index3 = cursor.getColumnIndex(Constants.STEPS_AMOUNT)

        val list = ArrayList<LogEntry>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val logTitle = cursor.getString(index1)
            val logLocation = cursor.getString(index2)
            val logSteps = cursor.getString(index3)
            list.add(
                LogEntry(
                    sessionTitle = logTitle,
                    location = logLocation,
                    steps = logSteps
                )
            )
            cursor.moveToNext()
        }
        cursor.close()

        customAdapter = CustomAdapter(list, db)

        myRecycler.adapter = customAdapter
        layoutManager = LinearLayoutManager(this)
        myRecycler.layoutManager = layoutManager

        customAdapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onDeleteItemClick(position: Int) {
                customAdapter.deleteItem(position)
            }
        })

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val locationQuery = searchEditText.text.toString()
            val filteredList = db.queryLogs(locationQuery)
            updateRecyclerView(filteredList)
        }
    }

    private fun deleteAllLogs() {
        db.deleteAllRecords()
        customAdapter.updateDataSet(arrayListOf())
        customAdapter.notifyDataSetChanged()
        Toast.makeText(this, "All logs deleted", Toast.LENGTH_SHORT).show()
    }

    private fun updateRecyclerView(newList: List<LogEntry>) {
        if (newList.isEmpty()) {
            Toast.makeText(this, "No logs found for this location", Toast.LENGTH_SHORT).show()
        } else {
            customAdapter.updateDataSet(newList)
            customAdapter.notifyDataSetChanged()
        }
    }
}
