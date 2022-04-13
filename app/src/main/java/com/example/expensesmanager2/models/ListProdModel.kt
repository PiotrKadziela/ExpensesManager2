package com.example.expensesmanager2.models

import android.content.Context
import com.example.expensesmanager2.interfaces.ModelInterface
import com.example.expensesmanager2.utils.SQLiteHelper

data class ListProdModel(
    val context: Context,
    val id: Int,
    val list_id: Int,
    val prod_id: Int,
    val amount: Double
) : ModelInterface {
    constructor(context: Context) : this(context, 0, 0, 0, 0.0)

    private val sql = SQLiteHelper(context)
    private val tableName = "list_prod"
    private val fieldsMap = mutableMapOf(
        "_id" to this.id.toString(),
        "list_id" to this.list_id.toString(),
        "prod_id" to this.prod_id.toString(),
        "amount" to this.amount.toString()
    )

    fun get(whereClause: String = ""): ArrayList<ListProdModel> {
        val listProdArray = arrayListOf<ListProdModel>()
        for (listProd in sql.get(tableName, whereClause)) {
            listProdArray.add(
                ListProdModel(
                    context,
                    listProd["_id"]!!.toInt(),
                    listProd["list_id"]!!.toInt(),
                    listProd["prod_id"]!!.toInt(),
                    listProd["amount"]!!.toDouble()
                )
            )
        }
        return listProdArray
    }

    fun passUnbuyedProducts(productsIds: ArrayList<Int>) {
        val idsString = productsIds.toString().replace('[', '(').replace(']', ')')
        sql.update(
            tableName,
            mutableMapOf("list_id" to ListModel(context).getNewestListId().toString()),
            "_id IN $idsString"
        )
    }

    override fun insert(): Long {
        return sql.insert(tableName, fieldsMap)
    }

    override fun delete(whereClause: String): Int {
        return sql.delete(tableName, whereClause)
    }

    override fun update(): Int {
        return sql.update(tableName, fieldsMap, "_id = ${this.id}")
    }
}