@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moneymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val moneyViewModel: MoneyViewModel by viewModels {
        MoneyViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyManagerApp(viewModel = moneyViewModel)
        }
    }
}

@Composable
fun MoneyManagerApp(viewModel: MoneyViewModel) {
    when (viewModel.currentScreen.value) {
        "Home" -> HomeScreen(
            onNavigate = { viewModel.currentScreen.value = it },
            incomeList = viewModel.incomeList,
            expenseList = viewModel.expenseList,
            viewModel = viewModel
        )

        "Income" -> TrackerScreen(
            title = "Income Tracker 💰",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFF43C6AC), Color(0xFF191654))
            ),
            transactions = viewModel.incomeList,
            onBack = { viewModel.currentScreen.value = "Home" },
            isIncome = true,
            viewModel = viewModel
        )

        "Expense" -> TrackerScreen(
            title = "Expense Tracker 💸",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFF8E24AA), Color(0xFFEC407A), Color(0xFFFF7043))
            ),
            transactions = viewModel.expenseList,
            onBack = { viewModel.currentScreen.value = "Home" },
            isIncome = false,
            viewModel = viewModel
        )
    }
}
