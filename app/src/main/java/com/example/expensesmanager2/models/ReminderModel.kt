package com.example.expensesmanager2.models

data class ReminderModel(
    val id : Int,
    val title: String,
    val description: String,
    val type: Int,
    val time: Long,
    val periodId: Int
)