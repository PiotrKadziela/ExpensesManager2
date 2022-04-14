package com.example.expensesmanager2.models

import android.content.Context
import com.example.expensesmanager2.interfaces.ModelInterface
import com.example.expensesmanager2.utils.SQLiteHelper

data class OperationModel(
    val context: Context,
    val id: Int,
    val title: String,
    val cost: Double,
    val category: Int,
    val type: Int,
    val listId: Int?,
    val date: Long,
) : ModelInterface {
    constructor(context: Context) : this(context, 0, "", 0.0, 0, 0, 0, 0)

    private val sql = SQLiteHelper(context)
    private val tableName = "operations"
    private val fieldsMap = mutableMapOf(
        "_id" to this.id.toString(),
        "title" to this.title,
        "cost" to this.cost.toString(),
        "category" to this.category.toString(),
        "type" to this.type.toString(),
        "list_id" to this.listId.toString(),
        "date" to this.date.toString(),
    )

    fun get(whereClause: String = ""): ArrayList<OperationModel> {
        val oprArray = arrayListOf<OperationModel>()
        for (opr in sql.get(tableName, whereClause)) {
            oprArray.add(
                OperationModel(
                    context,
                    opr["_id"]!!.toInt(),
                    opr["title"]!!,
                    opr["cost"]!!.toDouble(),
                    opr["category"]!!.toInt(),
                    opr["type"]!!.toInt(),
                    opr["list_id"]!!.toInt(),
                    opr["date"]!!.toLong(),
                )
            )
        }
        return oprArray
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

    fun getExpensesSum(since: Long): Float {
        val operations = sql.get(
            tableName,
            "date > $since"
        )
        var sum = 0F
        for (opr in operations) {
            sum += opr["cost"]!!.toFloat()
        }
        return -sum
    }
}