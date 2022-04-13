package com.example.expensesmanager2.models

import android.content.Context
import com.example.expensesmanager2.interfaces.ModelInterface
import com.example.expensesmanager2.utils.SQLiteHelper

data class ListModel(
    val context: Context,
    val id: Int,
    val executed: Int
): ModelInterface {
    constructor(context: Context) : this(context, 0, 0)

    val sql = SQLiteHelper(context)
    private val tableName = "lists"
    private val fieldsMap = mutableMapOf(
        "_id" to this.id.toString(),
        "executed" to this.executed.toString()
    )

    fun getNewestListId(): Int {
        return sql.get(tableName, "executed = 0", "_id")[0]["_id"]!!.toInt()
    }

    fun startNewList(): Long {
        val newestList = getNewestListId()
        ListModel(context, newestList, 1).update()
        return ListModel(context, newestList + 1, 0).insert()
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