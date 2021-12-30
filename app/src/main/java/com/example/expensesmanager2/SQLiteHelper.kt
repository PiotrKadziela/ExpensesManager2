package com.example.expensesmanager2

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{

        private const val DATABASE_VERSION = 15
        private const val DATABASE_NAME = "expensesManager.db"
        private const val TBL_OPERATIONS = "operations"
        private const val TBL_CONFIG = "configuration"
        private const val TBL_CATEGORIES = "categories"
        private const val ID = "_id"
        private const val NAME = "name"
        private const val VALUE = "value"
        private const val TITLE = "title"
        private const val COST = "cost"
        private const val CATEGORY = "category"
        private const val TYPE = "type"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblOperations = ("CREATE TABLE " + TBL_OPERATIONS + "("
                + ID + " INTEGER PRIMARY KEY, "
                + TITLE + " TEXT,"
                + COST + " NUMERIC,"
                + CATEGORY + " INTEGER,"
                + TYPE + " INTEGER,"
                + "FOREIGN KEY(" + CATEGORY + ") REFERENCES " + TBL_CATEGORIES + "(" + ID + "))")
        db?.execSQL(createTblOperations)
        val createTblConfig = ("CREATE TABLE " + TBL_CONFIG + "("
                + NAME + " TEXT PRIMARY KEY, " + VALUE + " NUMERIC)")
        db?.execSQL(createTblConfig)
        val createTblCategories = ("CREATE TABLE " + TBL_CATEGORIES + "("
                + ID + " INTEGER PRIMARY KEY, " + NAME + " TEXT)")
        db?.execSQL(createTblCategories)
        val insertCategories = ("INSERT INTO " + TBL_CATEGORIES + " (" + NAME + ") VALUES (\"Food\"),(\"Entertaiment\"),(\"Transport\"),(\"Clothes\"),(\"Health\"),(\"Pets\"),(\"House\"),(\"Bills\"),(\"Toiletry\")")
        db?.execSQL(insertCategories)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_OPERATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CONFIG")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CATEGORIES")
        onCreate(db)
    }

    fun insertOperation(opr: OperationModel): Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TITLE, opr.title)
        contentValues.put(COST, BigDecimal(opr.cost).setScale(2, RoundingMode.HALF_EVEN).toDouble())
        contentValues.put(CATEGORY, getCategoryId(opr.category))
        contentValues.put(TYPE, opr.type)

        val success = db.insert(TBL_OPERATIONS, null, contentValues)

        return success
    }

    fun insertBalance(balance: Double): Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(VALUE, balance)
        contentValues.put(NAME, "balance")

        val success = db.insert(TBL_CONFIG, null, contentValues)

        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getBalance(): Double {
        val selectQuery = "SELECT $VALUE FROM $TBL_CONFIG WHERE $NAME = \"balance\""
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return 0.00
        }

        var balance: Double

        if (cursor.moveToFirst()){
            balance = cursor.getDouble(cursor.getColumnIndex(VALUE))
        }
        else {
            balance = 0.00
        }
        cursor.close()

        val selectOperationsQuery = "SELECT $COST FROM $TBL_OPERATIONS"

        val cursor1: Cursor?

        try {
            cursor1 = db.rawQuery(selectOperationsQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectOperationsQuery)
            return 0.00
        }

        if (cursor1.moveToFirst()){
            do {
                balance += cursor1.getDouble(cursor1.getColumnIndex(COST))
            }while (cursor1.moveToNext())
        }

        cursor1.close()
        return balance
    }

    @SuppressLint("Range")
    fun getAllCategories(): Array<String> {
        var catList: Array<String> = emptyArray()
        val selectQuery = "SELECT * FROM $TBL_CATEGORIES"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return arrayOf("")
        }

        var name: String

        if (cursor.moveToFirst()){
            do {
                name = cursor.getString(cursor.getColumnIndex(NAME))
                catList = append(catList, name)
            }while (cursor.moveToNext())
        }

        cursor.close()
        return catList
    }

    @SuppressLint("Range", "Recycle")
    fun getCategoryName(id: Int): String{
        val selectQuery = "SELECT $NAME FROM $TBL_CATEGORIES WHERE $ID = " + id.toString()
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return "ERROR"
        }

        var name: String

        if (cursor.moveToFirst()){
            name = cursor.getString(cursor.getColumnIndex(NAME))
        }
        else {
            name = "ERROR"
        }

        if(id == 0){
            name = "Income"
        }

        return name
    }

    @SuppressLint("Range", "Recycle")
    fun getCategoryId(name: String): Int{
        val id: Int
        Log.e("CAT_NAME", name)
        if(name == "Income"){
            id = 0
        }
        else {
            val selectQuery = "SELECT $ID FROM $TBL_CATEGORIES WHERE $NAME = \"" + name + "\""
            val db = this.writableDatabase

            Log.e("QUERY", selectQuery)
            val cursor: Cursor?

            try {
                cursor = db.rawQuery(selectQuery, null)

            } catch (e: Exception) {
                e.printStackTrace()
                db.execSQL(selectQuery)
                return -1
            }

            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex(ID))
            } else {
                id = -1
            }
        }
        return id
    }

    private fun append(catList: Array<String>, element: String): Array<String> {
        val list: MutableList<String> = catList.toMutableList()
        list.add(element)
        return list.toTypedArray()
    }

    @SuppressLint("Range")
    fun getAllOperations(): ArrayList<OperationModel>{
        val oprList: ArrayList<OperationModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_OPERATIONS ORDER BY $ID DESC"
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
                id = cursor.getInt(cursor.getColumnIndex(ID))
                title = cursor.getString(cursor.getColumnIndex(TITLE))
                cost = cursor.getDouble(cursor.getColumnIndex(COST))
                category = getCategoryName(cursor.getInt(cursor.getColumnIndex(CATEGORY)))
                type = cursor.getInt(cursor.getColumnIndex(TYPE))

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
        contentValues.put(COST, BigDecimal(opr.cost).setScale(2, RoundingMode.HALF_EVEN).toDouble())
        contentValues.put(CATEGORY, getCategoryId(opr.category))
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