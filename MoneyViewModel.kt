package com.example.moneymanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf

class MoneyViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: TransactionRepository

    var incomeList = mutableStateListOf<Transaction>()
    var expenseList = mutableStateListOf<Transaction>()

    var currentScreen = mutableStateOf("Home")
    var selectedMonth = mutableStateOf(0)
    var selectedYear = mutableStateOf(2025)
    var showMonthDialog = mutableStateOf(false)

    // ✅ Add this refresh trigger
    var refreshTrigger = mutableStateOf(0)

    init {
        val dao = AppDatabase.getDatabase(application).transactionDao()
        repo = TransactionRepository(dao)
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val incomeData = repo.getIncomeTransactions()
            val expenseData = repo.getExpenseTransactions()
            incomeList.clear()
            incomeList.addAll(incomeData)
            expenseList.clear()
            expenseList.addAll(expenseData)
        }
    }

    // ✅ Add this helper function
    fun refreshData() {
        refreshTrigger.value++
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repo.addTransaction(transaction)
            loadTransactions()
            refreshData() // ✅ Trigger UI refresh
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repo.deleteTransaction(transaction)
            loadTransactions()
            refreshData() // ✅ Trigger UI refresh
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repo.updateTransaction(transaction)
            loadTransactions()
            refreshData() // ✅ Trigger UI refresh
        }
    }
}
