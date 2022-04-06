package com.example.expensesmanager2.interfaces

import java.util.*
import kotlin.collections.ArrayList

interface ModelInterface {
    fun get(whereClause: String)
    fun insert(): Long
    fun delete(id: Int)
    fun update()
}