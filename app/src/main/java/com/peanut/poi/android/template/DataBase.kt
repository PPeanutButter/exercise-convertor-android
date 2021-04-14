package com.peanut.poi.android.template

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * SQL代码参见:
 * http://www.w3school.com.cn/sql/sql_update.asp
 */
class DataBase(context: Context?, name: String?, cursorFactory: SQLiteDatabase.CursorFactory?, version: Int) {
    var sqLiteDatabase: SQLiteDatabase? = null

    init {
        val dataBase = `DataBase$`(context, name, cursorFactory, version)
        sqLiteDatabase = dataBase.writableDatabase
    }

    fun version() = sqLiteDatabase!!.version

    fun execSQL(sql: String): String {
        return try {
            Log.v("SQL", sql)
            sqLiteDatabase!!.execSQL(sql)
            "Successful"
        } catch (e: SQLiteException) {
            e.printStackTrace()
            e.localizedMessage
        }
    }

    fun execSQL(sql: String, onError: String): String {
        return try {
            Log.v("SQL", sql)
            sqLiteDatabase!!.execSQL(sql)
            "Successful"
        } catch (e: SQLiteException) {
            e.printStackTrace()
            execSQL(onError)
            execSQL(sql)
        }
    }

    public fun rawQuery(sql: String): Cursor {
        Log.v("SQL",sql)
        return sqLiteDatabase!!.rawQuery(sql, null)
    }

    public fun close() {
        sqLiteDatabase!!.close()
    }
}

internal class `DataBase$`(context: Context?, name: String?, cursorFactory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, cursorFactory, version) {
    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
    }

}