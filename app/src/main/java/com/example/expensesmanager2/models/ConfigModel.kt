package com.example.expensesmanager2.models

import android.content.Context
import android.util.Log
import com.example.expensesmanager2.interfaces.ModelInterface
import com.example.expensesmanager2.utils.SQLiteHelper

data class ConfigModel(
    val context: Context,
    val name: String,
    val value: String
): ModelInterface {
    constructor(context: Context) : this(context, "", "")

    val sql = SQLiteHelper(context)
    private val tableName = "configuration"
    private val fieldsMap = mutableMapOf(
        "name" to this.name,
        "value" to this.value
    )

    fun get(name: String = ""): String {
        val configArray = arrayListOf<ConfigModel>()
        for (row in sql.get(tableName, "name = \"$name\"")) {
            if(row["name"] == "balance"){
                var balance = row["value"]!!.toDouble()
                for(operation in OperationModel(context).get()){
                    balance += operation.cost
                }
                return balance.toString()
            } else {
                configArray.add(
                    ConfigModel(
                        context,
                        row["name"]!!,
                        row["value"]!!
                    )
                )
            }
        }
        return configArray[0].value
    }

    override fun insert(): Long {
        return sql.insert(tableName, fieldsMap)
    }

    override fun delete(whereClause: String): Int {
        return sql.delete(tableName, whereClause)
    }

    override fun update(): Int {
        Log.e("EEEE", fieldsMap.toString())
        return sql.update(tableName, fieldsMap, "name = \"${this.name}\"")
    }
}