package com.example.iat359_final_project.feature.tracking.data

import com.example.iat359_final_project.data.local.Database
import com.example.iat359_final_project.feature.tracking.domain.SessionRepository

class SessionRepositoryImpl(
    private val database: Database
) : SessionRepository {
    override fun insertSession(location: String, steps: String, sessionTitle: String): Long {
        return database.insertData(location, steps, sessionTitle)
    }
}
