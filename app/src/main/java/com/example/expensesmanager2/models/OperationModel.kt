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
    constructor(context: Context) : this(context, 0, "", 0.0,0,0,0,0)
    private val sql = SQLiteHelper(context)
    override fun get(whereClause: String) {}
    fun getAll(): ArrayList<OperationModel> {
        return arrayListOf()
    }
    override fun insert(): Long {
        val fields = mutableMapOf(
            "title" to this.title,
            "cost" to this.cost.toString(),
            "category" to this.category.toString(),
            "type" to this.type.toString(),
            "list_id" to this.listId.toString(),
            "date" to this.date.toString(),
        )

        return sql.insert("operations", fields)
    }
    override fun delete(id: Int) {}
    override fun update() {}
}