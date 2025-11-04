package com.example.moneymanager

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun addTransaction(transaction: Transaction) = dao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = dao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = dao.delete(transaction)
    suspend fun getIncomeTransactions() = dao.getTransactionsByType("income")
    suspend fun getExpenseTransactions() = dao.getTransactionsByType("expense")
}
