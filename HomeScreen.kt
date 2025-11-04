package com.example.moneymanager

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    incomeList: SnapshotStateList<Transaction>,
    expenseList: SnapshotStateList<Transaction>,
    viewModel: MoneyViewModel
) {
    var selectedMonth by viewModel.selectedMonth
    var selectedYear by viewModel.selectedYear
    var showMonthDialog by viewModel.showMonthDialog

    val filteredIncome by remember(selectedMonth, selectedYear, incomeList) {
        derivedStateOf {
            if (selectedMonth == 0) incomeList
            else incomeList.filter { it.month == selectedMonth && it.year == selectedYear }
        }
    }
    val filteredExpense by remember(selectedMonth, selectedYear, expenseList) {
        derivedStateOf {
            if (selectedMonth == 0) expenseList
            else expenseList.filter { it.month == selectedMonth && it.year == selectedYear }
        }
    }

    val totalIncome by remember { derivedStateOf { filteredIncome.sumOf { it.amount } } }
    val totalExpense by remember { derivedStateOf { filteredExpense.sumOf { it.amount } } }
    val balance by remember { derivedStateOf { totalIncome - totalExpense } }

    val heading =
        if (selectedMonth == 0) "Overall Summary"
        else {
            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            "${months[selectedMonth - 1]} $selectedYear Summary"
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // ✨ Sparkling title
            SparklingTitle()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                heading,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Income: ₹${"%.2f".format(totalIncome)}", color = Color(0xFF2E7D32), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Expense: ₹${"%.2f".format(totalExpense)}", color = Color(0xFFC62828), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Balance: ₹${"%.2f".format(balance)}", color = Color(0xFF6A1B9A), fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showMonthDialog = true },
                colors = ButtonDefaults.buttonColors(Color(0xFF8E24AA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (selectedMonth == 0) "Select Month & Year" else "Show Overall / Change Month",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { onNavigate("Income") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF43A047)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💰 Income Tracker", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onNavigate("Expense") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFE53935)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💸 Expense Tracker", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showMonthDialog) {
        ModernMonthYearDialog(
            onDismiss = { showMonthDialog = false },
            onConfirm = { m, y ->
                selectedMonth = m
                selectedYear = y
                showMonthDialog = false
            },
            onOverall = {
                selectedMonth = 0
                showMonthDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernMonthYearDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    onOverall: () -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val years = (2020..2035).map { it.toString() }

    // ✅ Compatible with API 24 (uses java.util.Calendar)
    val calendar = remember { java.util.Calendar.getInstance() }
    val currentMonth = remember { calendar.get(java.util.Calendar.MONTH) + 1 } // 1-12
    val currentYear = remember { calendar.get(java.util.Calendar.YEAR).toString() }

    // ✅ Default selections
    var selectedMonth by remember { mutableStateOf(months[currentMonth - 1]) }
    var selectedYear by remember { mutableStateOf(currentYear) }

    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onOverall) { Text("Overall", color = Color(0xFF8E24AA)) }
                TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                TextButton(onClick = {
                    val monthIndex = months.indexOf(selectedMonth) + 1
                    val yearValue = selectedYear.toIntOrNull() ?: currentYear.toInt()
                    onConfirm(monthIndex, yearValue)
                }) {
                    Text("OK", color = Color(0xFF8E24AA))
                }
            }
        },
        title = {
            Text(
                "Select Month & Year",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 🔹 Month Dropdown
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = !monthExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = {},
                        label = { Text("Month") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(0.8f)
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    selectedMonth = month
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Year Dropdown
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = !yearExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedYear,
                        onValueChange = { newValue ->
                            if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                selectedYear = newValue
                            }
                        },
                        label = { Text("Year") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(0.8f)
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year) },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}



@Composable
fun SparklingTitle() {
    // 🔹 Infinite shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val shimmer = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        ),
        label = "sparkleShift"
    )

    // 🔹 Gradient shimmer brush
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8E24AA),
            Color(0xFFFF80AB),
            Color(0xFF8E24AA)
        ),
        start = Offset(shimmer.value - 500f, 0f),
        end = Offset(shimmer.value, 200f)
    )

    BasicText(
        text = "💼 Money Manager",
        style = TextStyle(
            brush = shimmerBrush,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    )
}
