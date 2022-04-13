package com.example.expensesmanager2.interfaces

interface ModelInterface {
    fun insert(): Long
    fun update(): Int
    fun delete(whereClause: String): Int
}