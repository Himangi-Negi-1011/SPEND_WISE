package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetEntity
import com.example.data.model.SpendWiseCategories
import com.example.data.model.TransactionEntity
import com.example.domain.analysis.CategorizationEngine
import com.example.domain.data.CsvImportResult
import com.example.domain.data.CsvParser
import com.example.domain.data.DemoData
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTx: TransactionEntity? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (merchant: String, amount: Double, type: String, category: String, notes: String, isFlagged: Boolean, riskReason: String?) -> Unit
) {
    var merchant by remember { mutableStateOf(initialTx?.merchant ?: "") }
    var amountText by remember { mutableStateOf(if (initialTx != null) initialTx.amount.toString() else "") }
    var type by remember { mutableStateOf(initialTx?.type ?: "EXPENSE") }
    var category by remember { mutableStateOf(initialTx?.category ?: "Food") }
    var notes by remember { mutableStateOf(initialTx?.notes ?: "") }
    var isFlagged by remember { mutableStateOf(initialTx?.isFlagged ?: false) }
    var riskReason by remember { mutableStateOf(initialTx?.riskReason ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-categorize suggestions on merchant change
    LaunchedEffect(merchant) {
        if (initialTx == null && merchant.length >= 3) {
            val suggested = CategorizationEngine.categorize(merchant).category
            if (suggested != "Other") {
                category = suggested
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (initialTx == null) "Add Transaction" else "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(4.dp)
                ) {
                    val isExpense = type == "EXPENSE"
                    Button(
                        onClick = { type = "EXPENSE" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("type_expense_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) RedRisk else Color.Transparent,
                            contentColor = if (isExpense) Color.White else TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { type = "INCOME" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("type_income_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpense) EmeraldPrimary else Color.Transparent,
                            contentColor = if (!isExpense) Color.White else TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Description") },
                    placeholder = { Text("e.g. Trader Joe's, Campus Rent, Uber") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("merchant_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Category selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_dropdown"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Text("${SpendWiseCategories.getIcon(category)}  $category", modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            SpendWiseCategories.ALL.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${SpendWiseCategories.getIcon(cat)}  $cat", color = TextPrimary) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Split with room, study session") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Risk flag checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isFlagged = !isFlagged }
                ) {
                    Checkbox(
                        checked = isFlagged,
                        onCheckedChange = { isFlagged = it },
                        colors = CheckboxDefaults.colors(checkedColor = AmberWarning)
                    )
                    Text(
                        text = "Flag as unusual / potential impulse spending",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFlagged) AmberWarning else TextSecondary
                    )
                }

                if (isFlagged) {
                    OutlinedTextField(
                        value = riskReason,
                        onValueChange = { riskReason = it },
                        label = { Text("Reason for signal") },
                        placeholder = { Text("e.g. Higher than normal dining spike") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberWarning,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = RedRisk,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountText.toDoubleOrNull()
                            if (merchant.isBlank()) {
                                errorMessage = "Merchant cannot be empty"
                                return@Button
                            }
                            if (parsedAmount == null || parsedAmount <= 0) {
                                errorMessage = "Please enter a valid positive amount"
                                return@Button
                            }
                            onSave(merchant, parsedAmount, type, category, notes, isFlagged, if (isFlagged) riskReason else null)
                        },
                        modifier = Modifier.testTag("save_transaction_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Transaction", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CsvImportDialog(
    existingTransactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onConfirmImport: (String) -> Unit
) {
    var csvText by remember { mutableStateOf(DemoData.getSampleCsvString()) }
    var previewResult by remember { mutableStateOf<CsvImportResult?>(null) }
    var step by remember { mutableStateOf(1) } // 1: Input, 2: Preview & Validate

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Professional CSV Import",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (step == 1) {
                    Text(
                        text = "Paste your bank CSV statement or use the structured demo template. Format: Date, Merchant, Amount, Category, Type",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = csvText,
                        onValueChange = { csvText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("csv_input_text"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        placeholder = { Text("Date,Merchant,Amount,Category,Type") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { csvText = DemoData.getSampleCsvString() }) {
                            Text("Reset Sample CSV", color = TealSecondary)
                        }
                        Button(
                            onClick = {
                                val result = CsvParser.parse(csvText, existingTransactions)
                                previewResult = result
                                step = 2
                            },
                            modifier = Modifier.testTag("parse_csv_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Parse & Validate", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (step == 2 && previewResult != null) {
                    val res = previewResult!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = DarkSurfaceElevated, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Total", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${res.totalRows}", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = EmeraldContainer.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Valid", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                                Text("${res.validRows}", fontWeight = FontWeight.Bold, color = EmeraldLight)
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = RedContainer.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Invalid", style = MaterialTheme.typography.labelSmall, color = RedRisk)
                                Text("${res.invalidRows}", fontWeight = FontWeight.Bold, color = RedRisk)
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = AmberContainer.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Duplicates", style = MaterialTheme.typography.labelSmall, color = AmberWarning)
                                Text("${res.duplicateRows}", fontWeight = FontWeight.Bold, color = AmberWarning)
                            }
                        }
                    }

                    Text("Transaction Preview", style = MaterialTheme.typography.titleSmall, color = TextPrimary)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(res.rows) { row ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (row.isValid) DarkSurfaceElevated else RedContainer.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (row.isValid) DarkBorder else RedRisk)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(row.merchant.ifBlank { "Row ${row.rowNumber} (Error)" }, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        Text("${row.category} • ${row.type}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        if (row.errorMessage != null) {
                                            Text(row.errorMessage, color = RedRisk, style = MaterialTheme.typography.labelSmall)
                                        }
                                        if (row.isDuplicate) {
                                            Text("Potential duplicate detected", color = AmberWarning, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Text("$${row.amount}", fontWeight = FontWeight.Bold, color = if (row.type == "INCOME") EmeraldLight else TextPrimary)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { step = 1 }) {
                            Text("Back to Edit", color = TextSecondary)
                        }
                        Button(
                            onClick = { onConfirmImport(csvText) },
                            enabled = res.validRows > 0,
                            modifier = Modifier.testTag("confirm_import_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Confirm & Import (${res.validRows})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetDialog(
    initialBudget: BudgetEntity? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var category by remember { mutableStateOf(initialBudget?.category ?: "Food") }
    var limitText by remember { mutableStateOf(if (initialBudget != null) initialBudget.monthlyLimit.toString() else "") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (initialBudget == null) "Set Category Budget" else "Edit Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Category selector
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text("${SpendWiseCategories.getIcon(category)}  $category", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        SpendWiseCategories.ALL.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${SpendWiseCategories.getIcon(cat)}  $cat", color = TextPrimary) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly Limit ($currencySymbol)") },
                    placeholder = { Text("e.g. 250.00") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (error != null) {
                    Text(error!!, color = RedRisk, style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Budget", tint = RedRisk)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = limitText.toDoubleOrNull()
                                if (amount == null || amount <= 0) {
                                    error = "Please enter a valid monthly limit"
                                    return@Button
                                }
                                onSave(category, amount)
                            },
                            modifier = Modifier.testTag("save_budget_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Budget", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (title: String, target: Double, current: Double, deadline: String, contribution: Double, category: String, icon: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var currentText by remember { mutableStateOf("0") }
    var deadline by remember { mutableStateOf("Dec 2026") }
    var contributionText by remember { mutableStateOf("150") }
    var category by remember { mutableStateOf("Savings") }
    var icon by remember { mutableStateOf("🎯") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "New Financial Target",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. Emergency Fund") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Target ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { currentText = it },
                        label = { Text("Current ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = contributionText,
                        onValueChange = { contributionText = it },
                        label = { Text("Monthly ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Pick Emoji Icon:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("🎯", "🛡️", "💻", "✈️", "🏠", "🚗").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (icon == emoji) EmeraldContainer else DarkSurfaceElevated)
                                .border(1.dp, if (icon == emoji) EmeraldPrimary else DarkBorder, CircleShape)
                                .clickable { icon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = RedRisk, style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val target = targetText.toDoubleOrNull()
                            val current = currentText.toDoubleOrNull() ?: 0.0
                            val contrib = contributionText.toDoubleOrNull() ?: 50.0
                            if (title.isBlank()) {
                                error = "Please provide a goal title"
                                return@Button
                            }
                            if (target == null || target <= 0) {
                                error = "Target amount must be greater than 0"
                                return@Button
                            }
                            onSave(title, target, current, deadline, contrib, category, icon)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Create Milestone", fontWeight = FontWeight.Bold, color = DarkBackground)
                    }
                }
            }
        }
    }
}

@Composable
fun AddRecurringDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (merchant: String, amount: Double, frequency: String, category: String, nextDate: String, status: String) -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Monthly") }
    var category by remember { mutableStateOf("Subscriptions") }
    var nextDate by remember { mutableStateOf("Oct 1, 2026") }
    var status by remember { mutableStateOf("ACTIVE") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Recurring Commitment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Service / Merchant") },
                    placeholder = { Text("e.g. Netflix, Spotify, Gym") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frequency") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = nextDate,
                    onValueChange = { nextDate = it },
                    label = { Text("Next Renewal Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Initial Status:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ACTIVE", "REVIEW", "ESSENTIAL").forEach { st ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (status == st) EmeraldContainer else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (status == st) EmeraldPrimary else DarkBorder),
                                modifier = Modifier.clickable { status = st }
                            ) {
                                Text(
                                    text = st,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (status == st) EmeraldLight else TextSecondary
                                )
                            }
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = RedRisk, style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (merchant.isBlank()) {
                                error = "Merchant cannot be empty"
                                return@Button
                            }
                            if (amt == null || amt <= 0) {
                                error = "Amount must be greater than 0"
                                return@Button
                            }
                            onSave(merchant, amt, frequency, category, nextDate, status)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Track Recurring", fontWeight = FontWeight.Bold, color = DarkBackground)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthDiagnosticsDialog(
    overview: com.example.domain.analysis.DetailedHealthOverview,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HEALTH INDEX DIAGNOSTICS",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "5-Pillar Financial Audit",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer)
                            .border(1.dp, EmeraldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${overview.overallScore}", fontWeight = FontWeight.ExtraBold, color = EmeraldLight, fontSize = 16.sp)
                    }
                }

                Text(
                    text = "Status: ${overview.status}. SpendWise measures your cashflow resilience across 5 weighted dimensions with 0% black-box guesswork.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                HorizontalDivider(color = DarkBorder)

                overview.metrics.forEach { metric ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(metric.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (metric.rating) {
                                        "Optimal" -> EmeraldContainer
                                        "Good" -> BlueInfo.copy(alpha = 0.2f)
                                        else -> AmberContainer
                                    }
                                ) {
                                    Text(
                                        text = "${metric.score}/100 • ${metric.rating}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (metric.rating) {
                                            "Optimal" -> EmeraldLight
                                            "Good" -> BlueInfo
                                            else -> AmberWarning
                                        }
                                    )
                                }
                            }
                            Text(metric.actualValueDescription, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                "Formula: ${metric.formulaExplanation}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Close Diagnostics", fontWeight = FontWeight.Bold, color = DarkBackground)
                }
            }
        }
    }
}

