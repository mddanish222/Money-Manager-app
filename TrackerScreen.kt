@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.moneymanager

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.*


/** ----------------------- Dialogs ----------------------- **/

@Composable
fun EditDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var nameState by remember { mutableStateOf(transaction.name) }
    var amountState by remember { mutableStateOf(transaction.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountState,
                    onValueChange = { amountState = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onDelete(transaction) }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }

                TextButton(onClick = {
                    val parsed = amountState.toDoubleOrNull() ?: transaction.amount
                    val updated = transaction.copy(name = nameState, amount = parsed)
                    onSave(updated)
                }) {
                    Text("Save", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearDialog(
    onDismiss: () -> Unit,
    onConfirm: (month: String, year: String) -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearRange = (currentYear - 5..currentYear + 5).map { it.toString() }

    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    var selectedMonth by remember { mutableStateOf(monthNames[Calendar.getInstance().get(Calendar.MONTH)]) }
    var selectedYear by remember { mutableStateOf(currentYear.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month & Year") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedMonth,
                    onExpandedChange = { expandedMonth = !expandedMonth }
                ) {
                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false }
                    ) {
                        monthNames.forEach { month ->
                            DropdownMenuItem(text = { Text(month) }, onClick = {
                                selectedMonth = month
                                expandedMonth = false
                            })
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedYear,
                    onExpandedChange = { expandedYear = !expandedYear }
                ) {
                    OutlinedTextField(
                        value = selectedYear,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Year") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false }
                    ) {
                        yearRange.forEach { year ->
                            DropdownMenuItem(text = { Text(year) }, onClick = {
                                selectedYear = year
                                expandedYear = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val monthNumber = (monthNames.indexOf(selectedMonth) + 1).toString()
                onConfirm(monthNumber, selectedYear)
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearDialog(
    onDismiss: () -> Unit,
    onConfirm: (year: String) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearRange = (currentYear - 5..currentYear + 5).map { it.toString() }

    var expanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(currentYear.toString()) }
    var manualInput by remember { mutableStateOf(TextFieldValue("")) }
    var isManual by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Year") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isManual, onCheckedChange = { isManual = it })
                    Text("Enter year manually")
                }

                if (isManual) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        label = { Text("Enter Year") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Year") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            yearRange.forEach { year ->
                                DropdownMenuItem(text = { Text(year) }, onClick = {
                                    selectedYear = year
                                    expanded = false
                                })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val year = if (isManual && manualInput.text.isNotBlank()) manualInput.text else selectedYear
                onConfirm(year)
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** ----------------------- TrackerScreen ----------------------- **/

@Composable
fun TrackerScreen(
    title: String,
    gradient: Brush,
    transactions: SnapshotStateList<Transaction>,
    onBack: () -> Unit,
    isIncome: Boolean,
    viewModel: MoneyViewModel,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp)
) {
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }

    var topMessage by remember { mutableStateOf("") }
    var topMessageType by remember { mutableStateOf("success") }
    var topMessageVisible by remember { mutableStateOf(false) }

    var refreshTrigger by remember { mutableStateOf(0) }
    fun refreshList() {
        refreshTrigger++
    }

    fun showTopMessage(msg: String, type: String) {
        topMessage = msg
        topMessageType = type
        topMessageVisible = true
        coroutineScope.launch {
            delay(2500)
            topMessageVisible = false
        }
    }

    var editDialogVisible by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    // fast selection map (transaction -> selected)
    val selectedTransactions = remember { mutableStateMapOf<Transaction, Boolean>() }

    var filterType by remember { mutableStateOf("Today") }
    var filteredList by remember { mutableStateOf<List<Transaction>>(transactions.toList()) }
    var monthInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var showMonthYearDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactions.size, refreshTrigger, filterType, monthInput, yearInput) {
        val cal = Calendar.getInstance()
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH) + 1
        val y = cal.get(Calendar.YEAR)
        filteredList = when (filterType) {
            "Today" -> transactions.filter { it.day == d && it.month == m && it.year == y }
            "All" -> transactions
            "Monthly" -> {
                val mi = monthInput.toIntOrNull()
                val yi = yearInput.toIntOrNull()
                if (mi != null && yi != null) transactions.filter { it.month == mi && it.year == yi } else transactions
            }

            "Yearly" -> {
                val yi = yearInput.toIntOrNull()
                if (yi != null) transactions.filter { it.year == yi } else transactions
            }

            else -> transactions
        }
    }

    Scaffold(
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clip(shape)
                        .background(gradient)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(
                    visible = topMessageVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(400)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (topMessageType) {
                                    "success" -> Color(0xFF4CAF50)
                                    "warning" -> Color(0xFFFFA000)
                                    "delete" -> Color(0xFFD32F2F)
                                    else -> Color(0xFF1976D2)
                                }
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(topMessage, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 1.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Enter Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Enter Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.text.isNotBlank() && amount.text.isNotBlank()) {
                            val amt = amount.text.toDoubleOrNull()
                            if (amt != null) {
                                val cal = Calendar.getInstance()
                                val d = cal.get(Calendar.DAY_OF_MONTH)
                                val m = cal.get(Calendar.MONTH) + 1
                                val y = cal.get(Calendar.YEAR)
                                val formattedDate = SimpleDateFormat(
                                    "dd MMM yyyy",
                                    Locale.getDefault()
                                ).format(cal.time)
                                val t = Transaction(
                                    name = name.text,
                                    amount = amt,
                                    date = formattedDate,
                                    day = d,
                                    month = m,
                                    year = y,
                                    type = if (isIncome) "income" else "expense"
                                )
                                coroutineScope.launch {
                                    viewModel.addTransaction(t)
                                    delay(120)
                                    refreshList()
                                }
                                name = TextFieldValue("")
                                amount = TextFieldValue("")
                                showTopMessage("✅ Entry added successfully!", "success")
                            } else showTopMessage("⚠️ Invalid amount entered!", "warning")
                        } else showTopMessage("⚠️ Please fill all fields!", "warning")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color(
                            0xFF6A1B9A
                        ) else Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add Entry ➕", color = Color.White) }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    Spacer(Modifier.width(8.dp))
                    listOf("Today", "All", "Monthly", "Yearly").forEach { filter ->
                        val isSelectedFilter = filterType == filter
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelectedFilter) Color(
                                0xFF6A1B9A
                            ) else Color.LightGray, animationSpec = tween(300)
                        )
                        Button(
                            onClick = {
                                when (filter) {
                                    "Monthly" -> showMonthYearDialog = true
                                    "Yearly" -> showYearDialog = true
                                    else -> {
                                        filterType = filter
                                        monthInput = ""
                                        yearInput = ""
                                        refreshList()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(horizontal = 6.dp).height(42.dp)
                        ) { Text(filter, color = Color.White) }
                    }
                    Spacer(Modifier.width(8.dp))
                }

                // selection list computed from map
                val selectedList = selectedTransactions.filterValues { it }.keys.toList()

                AnimatedVisibility(
                    visible = selectedList.isNotEmpty(),
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(450)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(300)
                    ) + fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${selectedList.size} selected",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6A1B9A),
                            fontSize = 16.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (selectedList.size == 1) {
                                        transactionToEdit = selectedList.first()
                                        editDialogVisible = true
                                        selectedTransactions.clear()
                                    } else {
                                        showTopMessage("⚠️ Select only one to edit!", "warning")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFFA726
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(100.dp).height(40.dp)
                            ) {
                                Text("✏️Edit", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val count = selectedList.size
                                        selectedList.forEach { viewModel.deleteTransaction(it) }
                                        delay(120)
                                        refreshList()
                                        selectedTransactions.clear()
                                        showTopMessage(
                                            "🗑️ $count entr${if (count == 1) "y" else "ies"} deleted successfully!",
                                            "delete"
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(130.dp).height(40.dp)
                            ) {
                                Text("🗑️Delete", color = Color.White)
                            }

                        }
                    }
                }

                val totalAmount = filteredList.sumOf { it.amount }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isIncome) Color(0xFFE1BEE7) else Color(
                            0xFFFFCDD2
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (filterType) {
                                "Today" -> "Today's Total"
                                "All" -> "Overall Total"
                                "Monthly" -> "Monthly Total"
                                "Yearly" -> "Yearly Total"
                                else -> "Total"
                            },
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "₹${String.format("%.2f", totalAmount)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color(0xFF4A148C) else Color(0xFFD32F2F)
                        )
                    }
                }

                // Optimized LazyColumn (recent first)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                ) {
                    items(filteredList.asReversed(), key = { it.id }) { transaction ->

                    val isSelected = selectedTransactions[transaction] == true
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF6A1B9A) else Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTransactions[transaction] = !isSelected },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(13.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(transaction.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(transaction.date, color = Color.Gray, fontSize = 14.sp)
                                }
                                Text(
                                    text = "₹${transaction.amount}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (transaction.type == "income") Color(0xFF4A148C) else Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }

                if (showMonthYearDialog) {
                    MonthYearDialog(onDismiss = { showMonthYearDialog = false }) { month, year ->
                        monthInput = month
                        yearInput = year
                        filterType = "Monthly"
                        showMonthYearDialog = false
                        refreshList()
                    }
                }

                if (showYearDialog) {
                    YearDialog(onDismiss = { showYearDialog = false }) { year ->
                        yearInput = year
                        filterType = "Yearly"
                        showYearDialog = false
                        refreshList()
                    }
                }

                transactionToEdit?.let { transaction ->
                    if (editDialogVisible) {
                        EditDialog(
                            transaction = transaction,
                            onDismiss = { editDialogVisible = false },
                            onSave = { updated ->
                                coroutineScope.launch {
                                    viewModel.updateTransaction(updated)
                                    delay(120)
                                    refreshList()
                                    editDialogVisible = false
                                    showTopMessage("✅ Edited successfully!", "success")
                                }
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.deleteTransaction(it)
                                    delay(120)
                                    refreshList()
                                    editDialogVisible = false
                                    showTopMessage("🗑️ Deleted successfully!", "delete")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
