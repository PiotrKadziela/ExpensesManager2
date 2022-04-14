package com.example.expensesmanager2.models

import android.content.Context
import com.example.expensesmanager2.interfaces.ModelInterface
import com.example.expensesmanager2.utils.SQLiteHelper

data class ReminderModel(
    val context: Context,
    val id: Int,
    val title: String,
    val description: String,
    val type: Int,
    var time: Long,
    val period_id: Int
) : ModelInterface {
    constructor(context: Context) : this(context, 0, "", "", 0, 0, 0)

    private val sql = SQLiteHelper(context)
    private val tableName = "reminders"
    private val fieldsMap = mutableMapOf(
        "_id" to this.id.toString(),
        "title" to this.title,
        "description" to this.description,
        "type" to this.type.toString(),
        "time" to this.time.toString(),
        "period_id" to this.period_id.toString()
    )

    fun get(whereClause: String = ""): ArrayList<ReminderModel> {
        val reminders = arrayListOf<ReminderModel>()
        for (reminder in sql.get(tableName, whereClause)) {
            reminders.add(
                ReminderModel(
                    context,
                    reminder["_id"]!!.toInt(),
                    reminder["title"]!!,
                    reminder["description"]!!,
                    reminder["type"]!!.toInt(),
                    reminder["time"]!!.toLong(),
                    reminder["period_id"]!!.toInt()
                )
            )
        }
        return reminders
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