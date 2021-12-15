package com.example.expensesmanager2

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.content.contentValuesOf
import java.lang.Exception

class SQLiteHelper(context:Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{

        private const val DATABASE_VERSION = 5
        private const val DATABASE_NAME = "expensesManager.db"
        private const val TBL_OPERATIONS = "operations"
        private const val ID = "_id"
        private const val TITLE = "title"
        private const val COST = "cost"
        private const val CATEGORY = "category"
        private const val TYPE = "type"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblOperations = ("CREATE TABLE " + TBL_OPERATIONS + "("
                + ID + " INTEGER PRIMARY KEY, " + TITLE + " TEXT,"
                + COST + " NUMERIC," + CATEGORY + " TEXT," + TYPE + " INTEGER)")
        db?.execSQL(createTblOperations)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_OPERATIONS")
        onCreate(db)
    }

    fun insertOperation(opr: OperationModel): Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TITLE, opr.title)
        contentValues.put(COST, opr.cost)
        contentValues.put(CATEGORY, opr.category)
        contentValues.put(TYPE, opr.type)

        val success = db.insert(TBL_OPERATIONS, null, contentValues)
        db.close()
        return success
    }


    @SuppressLint("Range")
    fun getAllOperations(): ArrayList<OperationModel>{
        val oprList: ArrayList<OperationModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_OPERATIONS ORDER BY _id DESC"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var title: String
        var cost: Double
        var category: String
        var type: Int

        if (cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndex("_id"))
                title = cursor.getString(cursor.getColumnIndex("title"))
                cost = cursor.getDouble(cursor.getColumnIndex("cost"))
                category = cursor.getString(cursor.getColumnIndex("category"))
                type = cursor.getInt(cursor.getColumnIndex("type"))

                val opr = OperationModel(id, title, cost, category, type)
                oprList.add(opr)
            }while (cursor.moveToNext())
        }

        cursor.close()
        return oprList
    }

    fun updateOperation(opr: OperationModel): Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, opr.id)
        contentValues.put(TITLE, opr.title)
        contentValues.put(COST, opr.cost)
        contentValues.put(CATEGORY, opr.category)
        contentValues.put(TYPE, opr.type)

        val success = db.update(TBL_OPERATIONS, contentValues, "_id=" + opr.id, null)
        db.close()

        return success
    }

    fun deleteOperation(id: Int): Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, id)

        val success = db.delete(TBL_OPERATIONS, "_id=" + id, null)
        db.close()

        return success
    }
}