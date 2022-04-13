package com.example.expensesmanager2.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.expensesmanager2.models.*
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

class SQLiteHelper(val context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 41
        private const val DATABASE_NAME = "expensesManager.db"
        private const val TBL_OPERATIONS = "operations"
        private const val TBL_CONFIG = "configuration"
        private const val TBL_CATEGORIES = "categories"
        private const val TBL_PRODUCTS = "products"
        private const val TBL_LISTS = "lists"
        private const val TBL_LIST_PROD = "list_prod"
        private const val TBL_REMINDERS = "reminders"
        private const val ID = "_id"
        private const val NAME = "name"
        private const val VALUE = "value"
        private const val TITLE = "title"
        private const val COST = "cost"
        private const val CATEGORY = "category"
        private const val TYPE = "type"
        private const val UNIT = "unit"
        private const val EXECUTED = "executed"
        private const val LIST_ID = "list_id"
        private const val PROD_ID = "prod_id"
        private const val AMOUNT = "amount"
        private const val DATE = "date"
        private const val REGULAR = "regular"
        private const val TIME = "time"
        private const val DESCRIPTION = "description"
        private const val PERIOD = "period_id"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        //OPERATIONS TABLE
        val createTblOperations = ("CREATE TABLE $TBL_OPERATIONS (" +
                "$ID INTEGER PRIMARY KEY, " +
                "$TITLE TEXT," +
                "$COST NUMERIC," +
                "$CATEGORY INTEGER," +
                "$TYPE INTEGER," +
                "$LIST_ID INTEGER," +
                "$DATE INTEGER," +
                "FOREIGN KEY(" + CATEGORY + ") REFERENCES " + TBL_CATEGORIES + "(" + ID + ")," +
                "FOREIGN KEY(" + LIST_ID + ") REFERENCES " + TBL_LISTS + "(" + ID + "));")
        db?.execSQL(createTblOperations)

        //CONFIG TABLE
        val createTblConfig = ("CREATE TABLE $TBL_CONFIG (" +
                "$NAME TEXT PRIMARY KEY," +
                "$VALUE TEXT);" +
                "INSERT INTO $TBL_CONFIG ($NAME, $VALUE) VALUES (\"current_list\", 0);")
        db?.execSQL(createTblConfig)

        //CATEGORIES TABLE
        val createTblCategories = ("CREATE TABLE $TBL_CATEGORIES (" +
                "$ID INTEGER PRIMARY KEY, " +
                "$NAME TEXT);" +
                "INSERT INTO $TBL_CATEGORIES ($ID, $NAME) VALUES (0, \"Income\");" +
                "INSERT INTO $TBL_CATEGORIES ($NAME) VALUES " +
                "(\"Food\"),(\"Entertaiment\"),(\"Transport\"),(\"Clothes\"),(\"Health\"),(\"Pets\"),(\"House\"),(\"Bills\"),(\"Toiletry\");")
        db?.execSQL(createTblCategories)

        //PRODUCTS TABLE
        val createTblProducts = ("CREATE TABLE $TBL_PRODUCTS(" +
                "$ID INTEGER PRIMARY KEY," +
                "$NAME TEXT," +
                "$UNIT TEXT," +
                "$REGULAR INTEGER);")
        db?.execSQL(createTblProducts)

        //SHOPPING LISTS TABLE
        val createTblLists = ("CREATE TABLE $TBL_LISTS(" +
                "$ID INTEGER PRIMARY KEY," +
                "$EXECUTED INTEGER);" +
                "INSERT INTO $TBL_LISTS ($EXECUTED) VALUES (0);")
        db?.execSQL(createTblLists)

        //LISTS_PROD TABLE
        val createTblListProd = ("CREATE TABLE $TBL_LIST_PROD(" +
                "$ID INTEGER PRIMARY KEY," +
                "$LIST_ID INTEGER," +
                "$PROD_ID INTEGER," +
                "$AMOUNT NUMERIC," +
                "FOREIGN KEY(" + LIST_ID + ") REFERENCES " + TBL_LISTS + "(" + ID + ")," +
                "FOREIGN KEY(" + LIST_ID + ") REFERENCES " + TBL_LISTS + "(" + ID + "));")
        db?.execSQL(createTblListProd)

        //REMINDERS TABLE
        val createTblReminders = ("CREATE TABLE $TBL_REMINDERS(" +
                "$ID INTEGER PRIMARY KEY," +
                "$TITLE TEXT," +
                "$DESCRIPTION TEXT," +
                "$TYPE INTEGER," +
                "$PERIOD INTEGER," +
                "$TIME NUMERIC);")
        db?.execSQL(createTblReminders)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_OPERATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CONFIG")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TBL_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TBL_LISTS")
        db.execSQL("DROP TABLE IF EXISTS $TBL_LIST_PROD")
        db.execSQL("DROP TABLE IF EXISTS $TBL_REMINDERS")
        onCreate(db)
    }

    @SuppressLint("Range")
    fun get(
        table: String,
        whereClause: String = "",
        fields: String = "*",
        order: String = "",
        limit: String = ""
    ): Array<Map<String, String>> {
        var selectQuery = "SELECT $fields FROM $table"
        if (whereClause.isNotEmpty()) selectQuery += " WHERE $whereClause"
        if (order.isNotEmpty()) selectQuery += " ORDER BY $order"
        if (limit.isNotEmpty()) selectQuery += " LIMIT $limit"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return arrayOf()
        }
        val result: MutableList<Map<String, String>> = mutableListOf()
        if (cursor.moveToFirst()) {
            do {
                val element = mutableMapOf<String, String>()
                for (field in cursor.columnNames) {
                    element[field] = cursor.getString(cursor.getColumnIndex(field))
                }
                result.add(element)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return result.toTypedArray()
    }

    fun insert(table: String, fields: MutableMap<String, String>): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        for (field in fields) if (field.key != ID) contentValues.put(field.key, field.value)

        return db.insert(table, null, contentValues)
    }

    fun update(table: String, fields: MutableMap<String, String>, whereClause: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        for (field in fields)  if (field.key != ID) contentValues.put(field.key, field.value)

        return db.update(table, contentValues, whereClause, null)
    }

    fun delete(table: String, whereClause: String): Int {
        val db = this.writableDatabase

        val success = db.delete(table, "$whereClause", null)
        db.close()

        return success
    }
}