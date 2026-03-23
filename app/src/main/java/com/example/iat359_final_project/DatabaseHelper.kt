package com.example.iat359_final_project

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(
    context,
    Constants.DATABASE_NAME,
    null,
    Constants.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(CREATE_TABLE)
            Toast.makeText(context, "onCreate() called", Toast.LENGTH_LONG).show()
        } catch (_: SQLException) {
            Toast.makeText(context, "exception onCreate() db", Toast.LENGTH_LONG).show()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL(DROP_TABLE)
            onCreate(db)
            Toast.makeText(context, "onUpgrade called", Toast.LENGTH_LONG).show()
        } catch (_: SQLException) {
            Toast.makeText(context, "exception onUpgrade() db", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val CREATE_TABLE =
            "CREATE TABLE " +
                Constants.TABLE_NAME + " (" +
                Constants.UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.STEPS_AMOUNT + " TEXT, " +
                Constants.LOCATION + " TEXT, " +
                Constants.SESSION_TITLE + " TEXT);"

        private const val DROP_TABLE = "DROP TABLE IF EXISTS ${Constants.TABLE_NAME}"
    }
}
