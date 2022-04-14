package com.example.expensesmanager2.models

import android.content.Context
import com.example.expensesmanager2.interfaces.ModelInterface
import com.example.expensesmanager2.utils.SQLiteHelper

data class ProductModel(
    val context: Context,
    val id: Int,
    val name: String,
    val unit: String,
    val isBoughtRegularly: Int
) : ModelInterface {
    constructor(context: Context) : this(context, 0, "", "", 0)

    private val sql = SQLiteHelper(context)
    private val tableName = "products"
    private val fieldsMap = mutableMapOf(
        "_id" to this.id.toString(),
        "name" to this.name,
        "unit" to this.unit,
        "regular" to this.isBoughtRegularly.toString()
    )

    override fun insert(): Long {
        return sql.insert(tableName, fieldsMap)
    }

    override fun delete(whereClause: String): Int {
        return sql.delete(tableName, whereClause)
    }

    override fun update(): Int {
        return sql.update(tableName, fieldsMap, "_id = ${this.id}")
    }

    fun get(whereClause: String = ""): ArrayList<ProductModel> {
        val prodArray = arrayListOf<ProductModel>()
        for (opr in sql.get(tableName, whereClause)) {
            prodArray.add(
                ProductModel(
                    context,
                    opr["_id"]!!.toInt(),
                    opr["name"]!!,
                    opr["unit"]!!,
                    opr["regular"]!!.toInt(),
                )
            )
        }
        return prodArray
    }

    fun getAverageValues(): Map<String, Long> {
        val productsList = sql.get("list_prod", "prod_id = ${this.id}")
        val executedLists = sql.get("lists", "executed = 1")
        val executedListsIds = arrayListOf<Int>()
        for (list in executedLists) {
            executedListsIds.add(list["_id"]!!.toInt())
        }
        val listsContainProduct = arrayListOf<Int>()
        val amountOfProduct = arrayListOf<Long>()
        for (product in productsList) {
            if (executedListsIds.contains(product["list_id"]!!.toInt())) {
                listsContainProduct.add(product["list_id"]!!.toInt())
                amountOfProduct.add(product["amount"]!!.toLong())
            }
        }
        val listArrayString = listsContainProduct.toString().replace('[', '(').replace(']', ')')
        val operations = sql.get("operations", "list_id IN$listArrayString", "date", "date DESC")
        val operationsDates = arrayListOf<Long>()
        for (operation in operations) {
            operationsDates.add(operation["date"]!!.toLong())
        }
        val dateDiff = arrayListOf<Long>()
        operationsDates.forEachIndexed { index, l ->
            if (index < operationsDates.size - 1) {
                dateDiff.add(l - operationsDates[index + 1])
            }
        }
        return mapOf(
            Pair("time", dateDiff.average().toLong()),
            Pair("amount", amountOfProduct.average().toLong())
        )
    }

    fun getLastBuyDate(): Long {
        val executedLists = sql.get("lists", "executed = 1")
        val executedListsIds = arrayListOf<Int>()
        for (list in executedLists) {
            executedListsIds.add(list["id"]!!.toInt())
        }
        val products = sql.get("list_prod", "prod_id = ${this.id}")
        val listsWithProduct = arrayListOf<Int>()
        for (product in products) {
            if (executedListsIds.contains(product["list_id"]!!.toInt())) {
                listsWithProduct.add(product["list_id"]!!.toInt())
            }
        }
        val listsString = listsWithProduct.toString().replace('[', '(').replace(']', ')')

        return sql.get(
            "operations",
            "list_id IN $listsString",
            "date",
            "date DESC",
            "1"
        )[0]["date"]!!.toLong()
    }
}