package com.example.expensesmanager2

data class OperationModel(
    val id: Int,
    val title: String,
    val cost: Double,
    val category: String,
    val type: Int,
    val list: Int?
)