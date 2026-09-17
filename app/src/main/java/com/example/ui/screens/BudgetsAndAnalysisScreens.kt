package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.SpendWiseCategories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.domain.analysis.SpendingAnalytics
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun BudgetsScreen(
    budgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>,
    targetTotalBudget: Double,
    currencySymbol: String,
    onAddEditBudget: (BudgetEntity?) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit
) {
    val totalExpense = transactions.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalAllocated = budgets.sumOf { it.monthlyLimit }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddEditBudget(null) },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("budgets_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Monthly Budget Allocation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("$currencySymbol${String.format(Locale.US, "%,.2f", totalExpense)}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Target Ceiling", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("$currencySymbol${String.format(Locale.US, "%,.2f", targetTotalBudget)}", fontWeight = FontWeight.Bold, color = EmeraldLight, fontSize = 20.sp)
                            }
                        }

                        val overallRatio = if (targetTotalBudget > 0) (totalExpense / targetTotalBudget).toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { overallRatio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (overallRatio > 1f) RedRisk else if (overallRatio > 0.8f) AmberWarning else EmeraldPrimary,
                            trackColor = DarkSurfaceElevated
                        )

                        Text(
                            text = "${(overallRatio * 100).roundToInt()}% of overall budget utilized",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Category Limits (${budgets.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (budgets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No category budgets set. Tap + to create one.", color = TextMuted)
                    }
                }
            }

            items(budgets, key = { it.id }) { budget ->
                val spent = transactions
                    .filter { it.transactionType == TransactionType.EXPENSE && it.category.equals(budget.category, true) }
                    .sumOf { it.amount }
                val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
                val remaining = budget.monthlyLimit - spent

                val (statusLabel, statusColor) = when {
                    ratio > 1.0f -> Pair("OVER BUDGET", RedRisk)
                    ratio >= 0.80f -> Pair("APPROACHING LIMIT", AmberWarning)
                    else -> Pair("NORMAL", EmeraldLight)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, if (ratio > 1f) RedRisk.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(14.dp))
                        .clickable { onAddEditBudget(budget) },
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(SpendWiseCategories.getIcon(budget.category), fontSize = 20.sp)
                                Text(budget.category, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = statusLabel,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = statusColor,
                            trackColor = DarkSurfaceElevated
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: $currencySymbol${String.format(Locale.US, "%.2f", spent)} (${(ratio * 100).roundToInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = if (remaining >= 0) "Left: $currencySymbol${String.format(Locale.US, "%.2f", remaining)}"
                                else "Over: $currencySymbol${String.format(Locale.US, "%.2f", -remaining)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (remaining >= 0) EmeraldLight else RedRisk,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }
}

@Composable
fun SpendingAnalysisScreen(
    analytics: SpendingAnalytics,
    currencySymbol: String
) {
    val dateFormat = SimpleDateFormat("MMM d", Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Diagnostic Question Header: "Where am I overspending?"
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = EmeraldLight)
                        Text("Where am I overspending?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    val topCat = analytics.topCategories.firstOrNull()
                    val topMerch = analytics.biggestMerchants.firstOrNull()
                    Text(
                        text = if (topCat != null) "Your biggest spending concentration is in ${topCat.first} ($currencySymbol${String.format(Locale.US, "%.2f", topCat.second)}), led by purchases at ${topMerch?.first ?: "various merchants"}."
                        else "No high overspending concentrations detected in this billing cycle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Top Spending Categories Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Top Spending Categories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    analytics.topCategories.forEach { (cat, amount) ->
                        val pct = if (analytics.totalExpense > 0) ((amount / analytics.totalExpense) * 100).toInt() else 0
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${SpendWiseCategories.getIcon(cat)}  $cat", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("$currencySymbol${String.format(Locale.US, "%.2f", amount)} ($pct%)", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EmeraldPrimary,
                                trackColor = DarkSurfaceElevated
                            )
                        }
                    }
                }
            }
        }

        // Biggest Merchants
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Biggest Merchants & Vendors", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    analytics.biggestMerchants.forEach { (merch, total) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(merch, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("$currencySymbol${String.format(Locale.US, "%.2f", total)}", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Recurring Commitments (Subscriptions & Rent)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Repeat, contentDescription = null, tint = TealSecondary)
                        Text("Recurring Fixed Expenses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    val totalRecurring = analytics.recurringExpenses.sumOf { it.second }
                    Text("Total Recurring Baseline: $currencySymbol${String.format(Locale.US, "%.2f", totalRecurring)}/month", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                    analytics.recurringExpenses.forEach { (name, amount) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("$currencySymbol${String.format(Locale.US, "%.2f", amount)}", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Spending Spikes Detected
        if (analytics.spendingSpikes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = AmberWarning)
                            Text("Spending Spikes & Impulse Outliers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("Transactions > 2.5x your average purchase amount:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        analytics.spendingSpikes.forEach { spike ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(spike.merchant, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("${spike.category} • ${dateFormat.format(Date(spike.date))}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Text("$currencySymbol${String.format(Locale.US, "%.2f", spike.amount)}", fontWeight = FontWeight.Bold, color = AmberWarning)
                            }
                        }
                    }
                }
            }
        }

        // Transparent Calculation Disclosures
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Transparent Mathematical Methodology", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("Health Index = 85 (Baseline) - 30 (if Budget Utilization > 100%) + 10 (if Savings Rate > 20%) - 10 (Impulse Spikes Penalty). Transparently calculated on-device without opaque black-box scoring.", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
