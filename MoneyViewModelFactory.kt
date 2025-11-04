package com.example.moneymanager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoneyViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoneyViewModel::class.java)) {
            return MoneyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
