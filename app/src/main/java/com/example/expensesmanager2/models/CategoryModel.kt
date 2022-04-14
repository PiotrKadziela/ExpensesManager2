package com.example.expensesmanager2.models

import android.content.Context
import com.example.expensesmanager2.interfaces.ModelInterface
import com.example.expensesmanager2.utils.SQLiteHelper
import java.util.*

data class CategoryModel(
    val context: Context,
    val id: Int,
    var name: String
) : ModelInterface {
    constructor(context: Context) : this(context, 0, "")
    constructor(context: Context, id: Int) : this(context, id, "") {
        this.name = this.get("_id=${this.id}")[0].name
    }

    private val sql = SQLiteHelper(context)
    private val tableName = "categories"
    private val fieldsMap = mutableMapOf(
        "_id" to this.id.toString(),
        "name" to this.name
    )

    fun get(whereClause: String = ""): ArrayList<CategoryModel> {
        val catArray = arrayListOf<CategoryModel>()
        for (opr in sql.get(tableName, whereClause)) {
            catArray.add(
                CategoryModel(
                    context,
                    opr["_id"]!!.toInt(),
                    opr["name"]!!,
                )
            )
        }
        return catArray
    }

    fun getSum(since: Long): Float {
        val operations = sql.get(
            "operations",
            "category = ${this.id} AND date > $since"
        )
        var sum = 0F
        for (opr in operations) {
            sum += opr["cost"]!!.toFloat()
        }
        return -sum
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