package com.example.iat359_final_project

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class Database(context: Context) {
    private var db: SQLiteDatabase? = null
    private val helper: DatabaseHelper = DatabaseHelper(context)

    fun insertData(location: String, steps: String, sessionTitle: String): Long {
        db = helper.writableDatabase
        val contentValues = ContentValues().apply {
            put(Constants.SESSION_TITLE, sessionTitle)
            put(Constants.LOCATION, location)
            put(Constants.STEPS_AMOUNT, steps)
        }
        return db!!.insert(Constants.TABLE_NAME, null, contentValues)
    }

    fun getData(): Cursor {
        val writableDb = helper.writableDatabase
        val columns = arrayOf(
            Constants.UID,
            Constants.SESSION_TITLE,
            Constants.LOCATION,
            Constants.STEPS_AMOUNT
        )
        return writableDb.query(Constants.TABLE_NAME, columns, null, null, null, null, null)
    }

    fun queryLogs(location: String?): ArrayList<LogEntry> {
        val readableDb = helper.readableDatabase
        val columns = arrayOf(Constants.SESSION_TITLE, Constants.STEPS_AMOUNT, Constants.LOCATION)
        val selection = "${Constants.LOCATION} LIKE ?"
        val queryLocation = location?.let { "%${it.lowercase()}%" } ?: "%%"

        val cursor = readableDb.query(
            Constants.TABLE_NAME,
            columns,
            selection,
            arrayOf(queryLocation),
            null,
            null,
            null
        )

        val resultList = ArrayList<LogEntry>()
        cursor.use {
            if (it.moveToFirst()) {
                val titleIndex = it.getColumnIndex(Constants.SESSION_TITLE)
                val locationIndex = it.getColumnIndex(Constants.LOCATION)
                val stepsIndex = it.getColumnIndex(Constants.STEPS_AMOUNT)

                do {
                    val sessionTitle = it.getString(titleIndex)
                    val logLocation = it.getString(locationIndex)
                    val stepsAmount = it.getString(stepsIndex)
                    resultList.add(
                        LogEntry(
                            sessionTitle = sessionTitle,
                            location = logLocation,
                            steps = stepsAmount
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return resultList
    }

    fun deleteData(location: String) {
        db = helper.writableDatabase
        db?.delete(Constants.TABLE_NAME, "${Constants.SESSION_TITLE}=?", arrayOf(location))
        db?.close()
    }

    fun deleteAllRecords() {
        db = helper.writableDatabase
        db?.delete(Constants.TABLE_NAME, null, null)
        db?.close()
    }
}
