package com.example.proje.util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.proje.model.DatabaseEntry

class DataBaseHelper(context: Context,databaseName:String):SQLiteOpenHelper(context,databaseName,null,1) {

    private val TABLE_NAME = "APP_TABLE"
    private val COLUM_ID = "ID"
    private val COLUM_APP_PACKAGE_NAME = "APP_PACKAGE_NAME"
    private val COLUM_APP_NAME = "APP_NAME"
    private val COLUM_APP_DATE = "APP_DATE"
    private val COLUM_APP_USAGE = "APP_USAGE"


    override fun onCreate(db: SQLiteDatabase?) {
        var createDB = "CREATE TABLE $TABLE_NAME " +
                "( $COLUM_ID  INTEGER PRIMARY KEY AUTOINCREMENT," +
                 " $COLUM_APP_PACKAGE_NAME TEXT," +
                 " $COLUM_APP_NAME TEXT," +
                 " $COLUM_APP_DATE TEXT," +
                 " $COLUM_APP_USAGE  INTEGER )"
        db!!.execSQL(createDB)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }


    fun addEntry(databaseEntry: DatabaseEntry):Boolean{
        var db = this.writableDatabase
        var cv = ContentValues()
        cv.put(COLUM_APP_PACKAGE_NAME,databaseEntry.packageName)
        cv.put(COLUM_APP_NAME,databaseEntry.app_name)
        cv.put(COLUM_APP_DATE,databaseEntry.app_date)
        cv.put(COLUM_APP_USAGE,databaseEntry.usage_app)
        var rs = db.insert(TABLE_NAME,null,cv)
        return rs.toInt() != -1
    }

    fun getApp(appName:String,appDate:String):DatabaseEntry?{
        var db = this.readableDatabase
        println("gelen veriler : appName:$appName     appDate:$appDate")
        var cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE APP_NAME=? AND APP_DATE=?", arrayOf(appName,appDate))

        if(cursor.moveToFirst()){
            var entry = DatabaseEntry(
                    cursor.getString(cursor.getColumnIndex("$COLUM_APP_PACKAGE_NAME")),
                    cursor.getString(cursor.getColumnIndex("$COLUM_APP_NAME")),
                    cursor.getString(cursor.getColumnIndex("$COLUM_APP_DATE")),
                    cursor.getLong(cursor.getColumnIndex("$COLUM_APP_USAGE"))
            )
            return entry
        }else {
            println("illa buraya girecek.")
            return null
        }
    }

}