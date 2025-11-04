package com.example.moneymanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val date: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val type: String // "income" or "expense"
)
