package com.example.iat359_final_project.feature.logs.data

import com.example.iat359_final_project.core.constants.Constants
import com.example.iat359_final_project.data.local.Database
import com.example.iat359_final_project.domain.model.LogEntry
import com.example.iat359_final_project.feature.logs.domain.LogsRepository

class LogsRepositoryImpl(
    private val database: Database
) : LogsRepository {

    override fun getAllLogs(): List<LogEntry> {
        val cursor = database.getData()
        val titleIndex = cursor.getColumnIndex(Constants.SESSION_TITLE)
        val locationIndex = cursor.getColumnIndex(Constants.LOCATION)
        val stepsIndex = cursor.getColumnIndex(Constants.STEPS_AMOUNT)

        val logs = arrayListOf<LogEntry>()
        cursor.use {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    logs.add(
                        LogEntry(
                            sessionTitle = it.getString(titleIndex),
                            location = it.getString(locationIndex),
                            steps = it.getString(stepsIndex)
                        )
                    )
                    it.moveToNext()
                }
            }
        }
        return logs
    }

    override fun searchLogs(query: String): List<LogEntry> {
        return database.queryLogs(query)
    }

    override fun deleteLog(sessionTitle: String) {
        database.deleteData(sessionTitle)
    }

    override fun deleteAllLogs() {
        database.deleteAllRecords()
    }
}
