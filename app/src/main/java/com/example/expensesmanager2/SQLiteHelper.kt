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

        private const val DATABASE_VERSION = 18
        private const val DATABASE_NAME = "expensesManager.db"
        private const val TBL_OPERATIONS = "operations"
        private const val TBL_CONFIG = "configuration"
        private const val TBL_CATEGORIES = "categories"
        private const val TBL_PRODUCTS = "products"
        private const val TBL_LISTS = "lists"
        private const val TBL_LIST_PROD = "list_prod"
        private const val ID = "_id"
        private const val NAME = "name"
        private const val VALUE = "value"
        private const val TITLE = "title"
        private const val COST = "cost"
        private const val CATEGORY = "category"
        private const val TYPE = "type"
        private const val UNIT = "unit"
        private const val DONE = "done"
        private const val LIST_ID = "list_id"
        private const val PROD_ID = "prod_id"
        private const val AMOUNT = "amount"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        //OPERATIONS TABLE
        val createTblOperations = ("CREATE TABLE " + TBL_OPERATIONS + "("
                + ID + " INTEGER PRIMARY KEY, "
                + TITLE + " TEXT,"
                + COST + " NUMERIC,"
                + CATEGORY + " INTEGER,"
                + TYPE + " INTEGER,"
                + "FOREIGN KEY(" + CATEGORY + ") REFERENCES " + TBL_CATEGORIES + "(" + ID + "))")
        db?.execSQL(createTblOperations)

        //CONFIG TABLE
        val createTblConfig = ("CREATE TABLE " + TBL_CONFIG + "("
                + NAME + " TEXT PRIMARY KEY, " + VALUE + " NUMERIC)")
        db?.execSQL(createTblConfig)
        val insertCurrentList = ("INSERT INTO $TBL_CONFIG ($NAME, $VALUE) VALUES (\"current_list\", 0)")
        db?.execSQL(insertCurrentList)

        //CATEGORIES TABLE
        val createTblCategories = ("CREATE TABLE " + TBL_CATEGORIES + "("
                + ID + " INTEGER PRIMARY KEY, " + NAME + " TEXT)")
        db?.execSQL(createTblCategories)
        val insertCategories = ("INSERT INTO " + TBL_CATEGORIES + " (" + NAME + ") VALUES (\"Food\"),(\"Entertaiment\"),(\"Transport\"),(\"Clothes\"),(\"Health\"),(\"Pets\"),(\"House\"),(\"Bills\"),(\"Toiletry\")")
        db?.execSQL(insertCategories)

        //PRODUCTS TABLE
        val createTblProducts = ("CREATE TABLE $TBL_PRODUCTS(" +
                "$ID INTEGER PRIMARY KEY, " +
                "$NAME TEXT, " +
                "$UNIT TEXT)")
        db?.execSQL(createTblProducts)

        //SHOPPING LISTS TABLE
        val createTblLists = ("CREATE TABLE $TBL_LISTS(" +
                "$ID INTEGER PRIMARY KEY, " +
                "$DONE INTEGER)")
        db?.execSQL(createTblLists)
        val insertList = ("INSERT INTO $TBL_LISTS ($DONE) VALUES (0)")
        db?.execSQL(insertList)

        //LISTS_PROD TABLE
        val createTblListProd = ("CREATE TABLE $TBL_LIST_PROD(" +
                "$ID INTEGER PRIMARY KEY, " +
                "$LIST_ID INTEGER, " +
                "$PROD_ID INTEGER, " +
                "$AMOUNT NUMERIC)")
        db?.execSQL(createTblListProd)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_OPERATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CONFIG")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TBL_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TBL_LISTS")
        db.execSQL("DROP TABLE IF EXISTS $TBL_LIST_PROD")
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
        if(name == "Income"){
            id = 0
        }
        else {
            val selectQuery = "SELECT $ID FROM $TBL_CATEGORIES WHERE $NAME = \"" + name + "\""
            val db = this.writableDatabase

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

    fun insertListProd(prod: ListProdModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LIST_ID, prod.listId)
        contentValues.put(PROD_ID, getProductId(prod.name))
        contentValues.put(AMOUNT, prod.amount)

        val success = db.insert(TBL_LIST_PROD, null, contentValues)

        db.close()
        return success
    }

    fun insertProd(prod: ProductModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(NAME, prod.name)
        contentValues.put(UNIT, prod.unit)

        val success = db.insert(TBL_PRODUCTS, null, contentValues)

        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAllProducts(): Array<String> {
        var prodList: Array<String> = emptyArray()
        val selectQuery = "SELECT * FROM $TBL_PRODUCTS"
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
                prodList = append(prodList, name)
            }while (cursor.moveToNext())
        }

        cursor.close()
        return prodList
    }

    @SuppressLint("Range")
    fun getProductId(name: String): Int{
        val id: Int

        val selectQuery = "SELECT $ID FROM $TBL_PRODUCTS WHERE $NAME = \"" + name + "\""
        val db = this.writableDatabase

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

        cursor.close()
        return id
    }

    @SuppressLint("Range")
    fun getNewestListId(): Int{
        val selectQuery = "SELECT $ID FROM $TBL_LISTS WHERE $DONE = 0"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
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

    @SuppressLint("Range")
    fun getAllListProd(): ArrayList<ListProdModel> {

        val prodList: ArrayList<ListProdModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_LIST_PROD"
        val db = this.writableDatabase

        Log.e("QUERY", selectQuery)
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var listId: Int
        var prodId: Int
        var amount: String

        if (cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                listId = cursor.getInt(cursor.getColumnIndex(LIST_ID))
                prodId = cursor.getInt(cursor.getColumnIndex(PROD_ID))
                amount = cursor.getString(cursor.getColumnIndex(AMOUNT))

                val prod = ListProdModel(id, listId, getProductName(prodId), amount + " " + getProductUnit(prodId))
                prodList.add(prod)
            }while (cursor.moveToNext())
        }

        cursor.close()
        return prodList
    }

    @SuppressLint("Range")
    private fun getProductName(id: Int): String {
        val selectQuery = "SELECT $NAME FROM $TBL_PRODUCTS WHERE $ID = " + id.toString()
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return "ERROR"
        }

        val name: String

        if (cursor.moveToFirst()){
            name = cursor.getString(cursor.getColumnIndex(NAME))
        }
        else {
            name = "ERROR"
        }

        cursor.close()

        return name
    }

    @SuppressLint("Range")
    fun getProductUnit(id: Int): String {
        val selectQuery = "SELECT $UNIT FROM $TBL_PRODUCTS WHERE $ID = " + id.toString()
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return "ERROR"
        }

        val name: String

        if (cursor.moveToFirst()){
            name = cursor.getString(cursor.getColumnIndex(UNIT))
        }
        else {
            name = "ERROR"
        }

        cursor.close()

        return name
    }
}