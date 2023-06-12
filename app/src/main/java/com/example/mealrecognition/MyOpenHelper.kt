package com.example.mealrecognition

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyOpenHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(COMMENTS_TABLE_CREATE_ACTIVITY)
        db.execSQL(COMMENTS_TABLE_CREATE_PREVIOUS_ACTIVITY)
        db.execSQL(COMMENTS_TABLE_CREATE_HR)
        db.execSQL(COMMENTS_TABLE_CREATE_STEPS)
        db.execSQL(COMMENTS_TABLE_CREATE_CAL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val COMMENTS_TABLE_CREATE_ACTIVITY =
            "CREATE TABLE activityData(_id INTEGER PRIMARY KEY AUTOINCREMENT, DATETIME DEFAULT (datetime('now','localtime')), lpmAvg TEXT, devHR TEXT, maxHR TEXT, minHR TEXT, steps TEXT, calories TEXT, isSent TEXT DEFAULT '0')"
        private const val COMMENTS_TABLE_CREATE_PREVIOUS_ACTIVITY =
            "CREATE TABLE previousData(_id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT, DATETIME DEFAULT (datetime('now','localtime')), activityType TEXT, intensity TEXT, steps TEXT, heartRate TEXT, unknow1 TEXT, unknow2 TEXT, unknow3 TEXT, unknow4 TEXT, isSent TEXT DEFAULT '0')"
        private const val COMMENTS_TABLE_CREATE_HR =
            "CREATE TABLE heartRates(_id INTEGER PRIMARY KEY AUTOINCREMENT, DATETIME DEFAULT (datetime('now','localtime')), lpm TEXT, isSent TEXT DEFAULT '0')"
        private const val COMMENTS_TABLE_CREATE_STEPS =
            "CREATE TABLE steps_tb(_id INTEGER PRIMARY KEY AUTOINCREMENT, steps TEXT, DATETIME DEFAULT (datetime('now','localtime')))"
        private const val COMMENTS_TABLE_CREATE_CAL =
            "CREATE TABLE calories_tb(_id INTEGER PRIMARY KEY AUTOINCREMENT, calories TEXT, DATETIME DEFAULT (datetime('now','localtime')))"
        private const val DB_NAME = "aps.sqlite"
        private const val DB_VERSION = 1
    }
}


