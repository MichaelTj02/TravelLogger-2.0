package com.example.iat359_final_project.feature.tracking.domain

interface SessionRepository {
    fun insertSession(location: String, steps: String, sessionTitle: String): Long
}
