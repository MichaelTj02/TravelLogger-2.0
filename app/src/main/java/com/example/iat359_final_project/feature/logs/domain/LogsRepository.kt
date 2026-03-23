package com.example.iat359_final_project.feature.logs.domain

import com.example.iat359_final_project.domain.model.LogEntry

interface LogsRepository {
    fun getAllLogs(): List<LogEntry>
    fun searchLogs(query: String): List<LogEntry>
    fun deleteLog(sessionTitle: String)
    fun deleteAllLogs()
}
