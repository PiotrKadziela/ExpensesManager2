package com.example.expensesmanager2.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.expensesmanager2.models.*
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

class SQLiteHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 40
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
        val createTblOperations = ("CREATE TABLE " + TBL_OPERATIONS + "("
                + ID + " INTEGER PRIMARY KEY, "
                + TITLE + " TEXT,"
                + COST + " NUMERIC,"
                + CATEGORY + " INTEGER,"
                + TYPE + " INTEGER,"
                + LIST_ID + " INTEGER,"
                + DATE + " INTEGER,"
                + " FOREIGN KEY(" + CATEGORY + ") REFERENCES " + TBL_CATEGORIES + "(" + ID + "),"
                + " FOREIGN KEY(" + LIST_ID + ") REFERENCES " + TBL_LISTS + "(" + ID + "))")
        db?.execSQL(createTblOperations)

        //CONFIG TABLE
        val createTblConfig = ("CREATE TABLE " + TBL_CONFIG + "("
                + NAME + " TEXT PRIMARY KEY, " + VALUE + " TEXT)")
        db?.execSQL(createTblConfig)
        val insertCurrentList =
            ("INSERT INTO $TBL_CONFIG ($NAME, $VALUE) VALUES (\"current_list\", 0)")
        db?.execSQL(insertCurrentList)

        //CATEGORIES TABLE
        val createTblCategories = ("CREATE TABLE " + TBL_CATEGORIES + "("
                + ID + " INTEGER PRIMARY KEY, " + NAME + " TEXT)")
        db?.execSQL(createTblCategories)
        val insertIncome =
            ("INSERT INTO " + TBL_CATEGORIES + " ($ID, $NAME) VALUES (0, \"Income\")")
        db?.execSQL(insertIncome)
        val insertCategories =
            ("INSERT INTO " + TBL_CATEGORIES + " (" + NAME + ") VALUES (\"Food\"),(\"Entertaiment\"),(\"Transport\"),(\"Clothes\"),(\"Health\"),(\"Pets\"),(\"House\"),(\"Bills\"),(\"Toiletry\")")
        db?.execSQL(insertCategories)

        //PRODUCTS TABLE
        val createTblProducts = ("CREATE TABLE $TBL_PRODUCTS(" +
                "$ID INTEGER PRIMARY KEY, " +
                "$NAME TEXT, " +
                "$UNIT TEXT, " +
                "$REGULAR INTEGER)")
        db?.execSQL(createTblProducts)

        //SHOPPING LISTS TABLE
        val createTblLists = ("CREATE TABLE $TBL_LISTS(" +
                "$ID INTEGER PRIMARY KEY, " +
                "$EXECUTED INTEGER)")
        db?.execSQL(createTblLists)
        val insertList = ("INSERT INTO $TBL_LISTS ($EXECUTED) VALUES (0)")
        db?.execSQL(insertList)

        //LISTS_PROD TABLE
        val createTblListProd = ("CREATE TABLE $TBL_LIST_PROD(" +
                "$ID INTEGER PRIMARY KEY, " +
                "$LIST_ID INTEGER, " +
                "$PROD_ID INTEGER, " +
                "$AMOUNT NUMERIC," +
                " FOREIGN KEY(" + LIST_ID + ") REFERENCES " + TBL_LISTS + "(" + ID + ")," +
                " FOREIGN KEY(" + LIST_ID + ") REFERENCES " + TBL_LISTS + "(" + ID + "))")
        db?.execSQL(createTblListProd)

        //REMINDERS TABLE
        val createTblReminders = ("CREATE TABLE $TBL_REMINDERS(" +
                "id INTEGER PRIMARY KEY, " +
                "$TITLE TEXT, " +
                "$DESCRIPTION TEXT, " +
                "$TYPE INTEGER, " +
                "$PERIOD INTEGER, " +
                "$TIME NUMERIC)")
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

    fun insertConfig(balance: String, currency: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(VALUE, balance)
        contentValues.put(NAME, "balance")

        val success = db.insert(TBL_CONFIG, null, contentValues)

        contentValues.clear()
        contentValues.put(VALUE, currency)
        contentValues.put(NAME, "currency")

        val success1 = db.insert(TBL_CONFIG, null, contentValues)

        db.close()
        return success * success1
    }

    private fun append(catList: Array<String>, element: String): Array<String> {
        val list: MutableList<String> = catList.toMutableList()
        list.add(element)
        return list.toTypedArray()
    }

    @SuppressLint("Range")
    fun getOne(table: String, whereClause: String): MutableMap<String, String> {
        val valuesArray = mutableMapOf<String, String>()

        val selectQuery = "SELECT * FROM $table WHERE $whereClause"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return mutableMapOf()
        }

        cursor.moveToFirst()

        for (column in cursor.columnNames) {
            valuesArray[column] = cursor.getString(cursor.getColumnIndex(column))
        }

        cursor.close()

        return valuesArray
    }

    @SuppressLint("Range")
    fun getAll(table: String, whereClause: String): ArrayList<MutableMap<String, String>> {
        val valuesArray = arrayListOf(mutableMapOf<String, String>())

        val selectQuery = "SELECT * FROM $table WHERE $whereClause"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return arrayListOf(mutableMapOf())
        }

        if (cursor.moveToFirst()) {
            do {
                val array = mutableMapOf<String, String>()
                for (column in cursor.columnNames) {
                    array[column] = cursor.getString(cursor.getColumnIndex(column))
                }
                valuesArray.add(array)
            } while (cursor.moveToNext())
        }

        cursor.close()
        valuesArray.removeAt(0)
        return valuesArray
    }


    @SuppressLint("Range")
    fun getBalance(): Double {
        val selectQuery = "SELECT $VALUE FROM $TBL_CONFIG WHERE $NAME = \"balance\""
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return 0.00
        }

        var balance: Double

        if (cursor.moveToFirst()) {
            balance = cursor.getString(cursor.getColumnIndex(VALUE)).toDouble()
        } else {
            balance = 0.00
        }
        cursor.close()

        val selectOperationsQuery = "SELECT $COST FROM $TBL_OPERATIONS"

        val cursor1: Cursor?

        try {
            cursor1 = db.rawQuery(selectOperationsQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectOperationsQuery)
            return 0.00
        }

        if (cursor1.moveToFirst()) {
            do {
                balance += cursor1.getDouble(cursor1.getColumnIndex(COST))
            } while (cursor1.moveToNext())
        }

        cursor1.close()
        return balance
    }

    //CATEGORIES
    @SuppressLint("Range")
    fun getAllCategories(): ArrayList<CategoryModel> {
        var catList: ArrayList<CategoryModel> = arrayListOf()
        val selectQuery = "SELECT * FROM $TBL_CATEGORIES WHERE $ID != 0"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return arrayListOf()
        }

        var id: Int
        var name: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                name = cursor.getString(cursor.getColumnIndex(NAME))
                catList.add(CategoryModel(id, name))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return catList
    }

    @SuppressLint("Range")
    fun getCategorySummary(id: Int, since: Long): Float {
        val selectQuery =
            "SELECT $COST FROM $TBL_OPERATIONS WHERE $CATEGORY = $id AND $DATE > $since"
        val db = this.writableDatabase
        var sum = 0F

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return sum
        }


        if (cursor.moveToFirst()) {
            do {
                sum += cursor.getFloat(cursor.getColumnIndex(COST))
            } while (cursor.moveToNext())
        }

        return -sum
    }

    //OPERATIONS
    @SuppressLint("Range")
    fun getAllOperations(): ArrayList<OperationModel> {
        val oprList: ArrayList<OperationModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_OPERATIONS ORDER BY $ID DESC"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var title: String
        var cost: Double
        var category: Int
        var type: Int
        var list: Int
        var date: Long

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                title = cursor.getString(cursor.getColumnIndex(TITLE))
                cost = cursor.getDouble(cursor.getColumnIndex(COST))
                category = cursor.getColumnIndex(CATEGORY)
                type = cursor.getInt(cursor.getColumnIndex(TYPE))
                list = cursor.getInt(cursor.getColumnIndex(LIST_ID))
                date = cursor.getLong(cursor.getColumnIndex(DATE))


            } while (cursor.moveToNext())
        }

        cursor.close()
        return oprList
    }
    @SuppressLint("Range")
    fun getAll(table: String): Array<Map<String,String>>{
        val selectQuery = "SELECT * FROM $table"
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
                for (field in cursor.columnNames){
                    result.add(mapOf(field to cursor.getString(cursor.getColumnIndex(field))))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return result.toTypedArray()
    }

    fun insert(table: String, fields: MutableMap<String, String>): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        for (field in fields) contentValues.put(field.key, field.value)

        return db.insert(table, null, contentValues)
    }

    fun updateOperation(opr: OperationModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, opr.id)
        contentValues.put(TITLE, opr.title)
        contentValues.put(COST, BigDecimal(opr.cost).setScale(2, RoundingMode.HALF_EVEN).toDouble())
        contentValues.put(CATEGORY, getOne(TBL_CATEGORIES, "$NAME = \"${opr.category}\"")[ID])
        contentValues.put(TYPE, opr.type)

        val success = db.update(TBL_OPERATIONS, contentValues, "_id=" + opr.id, null)
        db.close()

        return success
    }

    fun deleteOperation(id: Int): Int {
        val db = this.writableDatabase

        val success = db.delete(TBL_OPERATIONS, "_id=" + id, null)
        db.close()

        return success
    }

    @SuppressLint("Range")
    fun getExpensesSumSince(since: Long): Double {
        val selectQuery =
            "SELECT $COST FROM $TBL_OPERATIONS WHERE $CATEGORY != 0 AND $DATE > $since"
        val db = this.writableDatabase
        var sum = 0.00

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return sum
        }


        if (cursor.moveToFirst()) {
            do {
                sum += cursor.getDouble(cursor.getColumnIndex(COST))
            } while (cursor.moveToNext())
        }

        return -sum
    }

    //LIST_PROD
    fun updateListProd(ids: ArrayList<Int>): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        val idsString = ids.toString().replace('[', '(').replace(']', ')')
        contentValues.put(LIST_ID, getNewestListId())

        val success = db.update(TBL_LIST_PROD, contentValues, "_id IN$idsString", null)
        db.close()

        return success
    }

    fun insertListProd(prod: ListProdModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LIST_ID, prod.listId)
        contentValues.put(PROD_ID, getOne("products", "$NAME = \"" + prod.name + "\"")[ID])
        contentValues.put(AMOUNT, prod.amount)

        val success = db.insert(TBL_LIST_PROD, null, contentValues)

        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAllListProd(listId: Int): ArrayList<ListProdModel> {

        val prodList: ArrayList<ListProdModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_LIST_PROD WHERE $LIST_ID = $listId"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var listId: Int
        var prodId: Int
        var amount: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                listId = cursor.getInt(cursor.getColumnIndex(LIST_ID))
                prodId = cursor.getInt(cursor.getColumnIndex(PROD_ID))
                amount = cursor.getString(cursor.getColumnIndex(AMOUNT))

                val prod = ListProdModel(
                    id,
                    listId,
                    getOne("products", "$ID = $prodId")[NAME]!!,
                    amount + " " + getOne("products", "$ID = $prodId")[UNIT]!!
                )
                prodList.add(prod)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return prodList
    }

    fun deleteListProd(id: Int): Int {
        val db = this.writableDatabase

        val success = db.delete(TBL_LIST_PROD, "_id=" + id, null)
        db.close()

        return success
    }

    //LISTS
    @SuppressLint("Range")
    fun getNewestListId(): Int {
        val selectQuery = "SELECT $ID FROM $TBL_LISTS WHERE $EXECUTED = 0"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return -1
        }

        val id: Int

        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(ID))
        } else {
            return -1
        }

        cursor.close()
        return id
    }

    fun startNewList(): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        val newestList = getNewestListId()
        contentValues.put(EXECUTED, 1)

        db.update(TBL_LISTS, contentValues, "_id=$newestList", null)

        contentValues.clear()
        contentValues.put(ID, newestList + 1)
        contentValues.put(EXECUTED, 0)

        val success = db.insert(TBL_LISTS, null, contentValues)

        db.close()
        return success
    }

    //PRODUCTS
    fun insertProduct(prod: ProductModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(NAME, prod.name)
        contentValues.put(UNIT, prod.unit)
        contentValues.put(REGULAR, prod.isBoughtRegularly)

        val success = db.insert(TBL_PRODUCTS, null, contentValues)

        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAllProductsNames(): Array<String> {
        var valuesList: Array<String> = emptyArray()
        val selectQuery = "SELECT $NAME FROM $TBL_PRODUCTS"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return arrayOf("")
        }

        var fieldValue: String

        if (cursor.moveToFirst()) {
            do {
                fieldValue = cursor.getString(cursor.getColumnIndex(NAME))
                valuesList = append(valuesList, fieldValue)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return valuesList
    }

    @SuppressLint("Range")
    fun getProducts(): ArrayList<ProductModel> {
        val prodList: ArrayList<ProductModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_PRODUCTS"
        val db = this.writableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var name: String
        var unit: String
        var regular: Int

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                name = cursor.getString(cursor.getColumnIndex(NAME))
                unit = cursor.getString(cursor.getColumnIndex(UNIT))
                regular = cursor.getInt(cursor.getColumnIndex(REGULAR))

                val prod = ProductModel(id, name, unit, regular)
                prodList.add(prod)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return prodList
    }

    fun getAverageProdBuy(id: Int): MutableMap<String, Long> {
        val listProdList = getAll(TBL_LIST_PROD, "$PROD_ID = $id")
        val listArray = arrayListOf<Int>()
        val amounts = mutableListOf<Long>()
        for (prod in listProdList) {
            if (!prod[LIST_ID].isNullOrEmpty()) {
                listArray.add(prod[LIST_ID]!!.toInt())
                amounts.add(prod[AMOUNT]!!.toLong())
            }
        }
        val listArrayString = listArray.toString().replace('[', '(').replace(']', ')')
        val oprArray = getAll(TBL_OPERATIONS, "$LIST_ID IN$listArrayString ORDER BY $DATE DESC")
        val dates = mutableListOf<Long>()
        for (opr in oprArray) {
            dates.add(opr[DATE]!!.toLong())
        }
        val dateDiff = arrayListOf<Long>()
        dates.forEachIndexed { index, l ->
            if (index < dates.size - 1) {
                dateDiff.add(l - dates[index + 1])
            }
        }
        var sum: Long = 0
        for (value in dateDiff) {
            sum += value
        }
        val avgTime = when (dateDiff.size) {
            0 -> 0
            else -> (round((sum / dateDiff.size).toDouble() / 1000 / 3600 / 24)).toLong()
        }

        sum = 0
        for (value in amounts) {
            sum += value
        }
        val avgAmount = when (amounts.size) {
            0 -> 0
            else -> sum / amounts.size
        }

        return mutableMapOf(Pair("time", avgTime), Pair("amount", avgAmount))
    }

    fun getLastBuyDate(id: Int): Long {
        val listProdList = getAll(TBL_LIST_PROD, "$PROD_ID = $id")
        val listArray = arrayListOf<Int>()
        for (prod in listProdList) {
            if (!prod[LIST_ID].isNullOrEmpty()) {
                listArray.add(prod[LIST_ID]!!.toInt())
            }
        }
        val listArrayString = listArray.toString().replace('[', '(').replace(']', ')')
        val oprArray =
            getAll(TBL_OPERATIONS, "$LIST_ID IN$listArrayString ORDER BY $DATE DESC LIMIT 1")
        return when (oprArray.size) {
            0 -> 0
            else -> oprArray[0][DATE]!!.toLong()
        }
    }

    fun updateProduct(prod: ProductModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(NAME, prod.name)
        contentValues.put(UNIT, prod.unit)
        contentValues.put(REGULAR, prod.isBoughtRegularly)

        val success = db.update(TBL_PRODUCTS, contentValues, "$ID=" + prod.id, null)
        db.close()

        return success
    }

    fun deleteProduct(id: Int): Int {
        val db = this.writableDatabase

        val success = db.delete(TBL_PRODUCTS, "$ID=" + id, null) * db.delete(
            TBL_LIST_PROD,
            "$PROD_ID=" + id,
            null
        )
        db.close()

        return success
    }

    //REMINDERS
    fun insertReminder(rmd: ReminderModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("id", rmd.id)
        contentValues.put(TITLE, rmd.title)
        contentValues.put(DESCRIPTION, rmd.description)
        contentValues.put(TYPE, rmd.type)
        contentValues.put(PERIOD, rmd.periodId)
        contentValues.put(TIME, rmd.time)

        val success = db.insert(TBL_REMINDERS, null, contentValues)

        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAllReminders(): ArrayList<ReminderModel> {
        val rmdList: ArrayList<ReminderModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_REMINDERS ORDER BY id DESC"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var title: String
        var description: String
        var type: Int
        var period: Int
        var time: Long

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                title = cursor.getString(cursor.getColumnIndex(TITLE))
                description = cursor.getString(cursor.getColumnIndex(DESCRIPTION))
                type = cursor.getInt(cursor.getColumnIndex(TYPE))
                time = cursor.getLong(cursor.getColumnIndex(TIME))
                period = cursor.getInt(cursor.getColumnIndex(PERIOD))

                val rmd = ReminderModel(id, title, description, type, time, period)
                rmdList.add(rmd)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return rmdList
    }

    fun deleteReminder(id: Int): Int {
        val db = this.writableDatabase

        val success = db.delete(TBL_REMINDERS, "id=" + id, null)
        db.close()

        return success
    }

    fun deleteOutdatedReminders(currentTimeMillis: Long): Int {
        val db = this.writableDatabase

        val success = db.delete(TBL_REMINDERS, "$TYPE = 1 AND $TIME < $currentTimeMillis", null)
        db.close()

        return success
    }

    fun updateReminderTime(id: Int, timeInMillis: Long): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TIME, timeInMillis)

        val success = db.update(TBL_REMINDERS, contentValues, "id=$id", null)
        db.close()

        return success
    }

    @SuppressLint("Range")
    fun getConfig(): MutableMap<String, String> {
        val selectQuery = "SELECT * FROM $TBL_CONFIG"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return mutableMapOf()
        }

        var key: String
        var value: String
        var config: MutableMap<String, String> = mutableMapOf()
        if (cursor.moveToFirst()) {
            do {
                key = cursor.getString(cursor.getColumnIndex(NAME))
                value = cursor.getString(cursor.getColumnIndex(VALUE))

                config[key] = value
            } while (cursor.moveToNext())
        }

        cursor.close()
        return config

    }

    @SuppressLint("Range")
    fun setConfig(balanceStr: String, currency: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(VALUE, currency)
        val success = db.update(TBL_CONFIG, contentValues, "$NAME = \"currency\"", null)

        var balance = balanceStr.toDouble()

        val selectOperationsQuery = "SELECT $COST FROM $TBL_OPERATIONS"

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectOperationsQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectOperationsQuery)
            return -1
        }

        if (cursor.moveToFirst()) {
            do {
                balance -= cursor.getDouble(cursor.getColumnIndex(COST))
            } while (cursor.moveToNext())
        }
        cursor.close()

        contentValues.clear()
        contentValues.put(VALUE, balance.toString())
        val success1 = db.update(TBL_CONFIG, contentValues, "$NAME = \"balance\"", null)
        db.close()

        return success * success1
    }
}