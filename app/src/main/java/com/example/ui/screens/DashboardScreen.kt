package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.*
import com.example.domain.analysis.DetailedHealthOverview
import com.example.domain.analysis.SpendingAnalytics
import com.example.ui.SpendWiseScreen
import com.example.ui.components.AiInsightCard
import com.example.ui.components.MetricCard
import com.example.ui.components.SimpleSpendingBarChart
import com.example.ui.components.SpendingHealthCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    analytics: SpendingAnalytics,
    riskAlerts: List<RiskAlert>,
    aiInsights: List<AiInsight>,
    budgets: List<BudgetEntity>,
    recentTransactions: List<TransactionEntity>,
    savingOpportunities: List<SavingOpportunity>,
    detailedHealthOverview: DetailedHealthOverview,
    currencySymbol: String,
    onNavigate: (SpendWiseScreen) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenCsvImport: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d", Locale.US)

    // Calculate last 7 days daily spending
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60L * 60L * 1000L
    val dailyExpenses = (6 downTo 0).map { daysAgo ->
        val targetDay = now - (daysAgo * dayMillis)
        val dayLabel = dateFormat.format(Date(targetDay))
        val amount = recentTransactions
            .filter { it.transactionType == TransactionType.EXPENSE && Math.abs(it.date - targetDay) < (dayMillis / 2) }
            .sumOf { it.amount }
        Pair(dayLabel, amount)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = EmeraldPrimary,
                contentColor = DarkBackground,
                shape = CircleShape,
                modifier = Modifier.testTag("dashboard_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
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
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. Executive User Profile & Command Center Bar
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer)
                                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("AR", fontWeight = FontWeight.Bold, color = EmeraldLight, fontSize = 12.sp)
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Alex Rivera", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Surface(shape = RoundedCornerShape(4.dp), color = EmeraldContainer.copy(alpha = 0.5f)) {
                                        Text("CLERK VERIFIED", color = EmeraldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                Text("Session active • Enterprise Shield", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        IconButton(onClick = { onNavigate(SpendWiseScreen.SETTINGS) }) {
                            Icon(Icons.Default.Tune, contentDescription = "Preferences", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // 2. Spending Health Indicator Card (Clickable to view transparent diagnostics)
            item {
                Box(modifier = Modifier.clickable { onOpenDiagnostics() }) {
                    SpendingHealthCard(
                        healthScore = analytics.healthScore,
                        status = analytics.healthStatus,
                        utilizationPercent = analytics.budgetUtilizationPercent,
                        currencySymbol = currencySymbol,
                        remainingBudget = analytics.remainingBudget
                    )
                }
            }

            // 3. High Priority Risk Alert Banner (if any)
            if (riskAlerts.isNotEmpty()) {
                item {
                    val topAlert = riskAlerts.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, RedRisk.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable { onNavigate(SpendWiseScreen.RISK) },
                        colors = CardDefaults.cardColors(containerColor = RedContainer.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = RedRisk)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Risk Signal Detected",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFECACA),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${topAlert.merchant}: ${topAlert.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RedRisk)
                        }
                    }
                }
            }

            // 4. Metric Cards Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Net Balance",
                            amount = analytics.totalBalance,
                            currencySymbol = currencySymbol,
                            subtitle = "Available reserve",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Monthly Income",
                            amount = analytics.totalIncome,
                            currencySymbol = currencySymbol,
                            changePercent = analytics.momIncomeChangePercent,
                            isPositiveGood = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Monthly Spending",
                            amount = analytics.totalExpense,
                            currencySymbol = currencySymbol,
                            changePercent = analytics.momExpenseChangePercent,
                            isPositiveGood = false,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Remaining Budget",
                            amount = analytics.remainingBudget,
                            currencySymbol = currencySymbol,
                            subtitle = "${analytics.budgetUtilizationPercent}% utilized",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Executive Quick Action Command Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ActionChip(
                            icon = Icons.Default.AutoAwesome,
                            label = "Ask Copilot",
                            color = EmeraldLight,
                            onClick = { onNavigate(SpendWiseScreen.COPILOT) }
                        )
                    }
                    item {
                        ActionChip(
                            icon = Icons.Default.TrackChanges,
                            label = "Goals",
                            color = EmeraldLight,
                            onClick = { onNavigate(SpendWiseScreen.GOALS) }
                        )
                    }
                    item {
                        ActionChip(
                            icon = Icons.Default.Repeat,
                            label = "Subscriptions",
                            color = AmberWarning,
                            onClick = { onNavigate(SpendWiseScreen.RECURRING) }
                        )
                    }
                    item {
                        ActionChip(
                            icon = Icons.Default.FileUpload,
                            label = "Import CSV",
                            color = TealSecondary,
                            onClick = onOpenCsvImport
                        )
                    }
                    item {
                        ActionChip(
                            icon = Icons.Default.Analytics,
                            label = "Analysis",
                            color = BlueInfo,
                            onClick = { onNavigate(SpendWiseScreen.ANALYSIS) }
                        )
                    }
                }
            }

            // 6. Saving Opportunities & Quick Wins
            if (savingOpportunities.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Saving Opportunities",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                val totalPot = savingOpportunities.sumOf { it.potentialMonthlySavings }
                                Surface(shape = RoundedCornerShape(6.dp), color = EmeraldContainer.copy(alpha = 0.5f)) {
                                    Text(
                                        text = "+$currencySymbol${String.format(Locale.US, "%.0f", totalPot)}/mo",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                            }

                            savingOpportunities.take(2).forEach { opp ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(opp.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                            Text(opp.observation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$currencySymbol${String.format(Locale.US, "%.0f", opp.potentialMonthlySavings)}",
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldLight,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Daily Spending Bar Chart
            item {
                SimpleSpendingBarChart(
                    dailyExpenses = dailyExpenses,
                    currencySymbol = currencySymbol
                )
            }

            // 8. Top Spending Categories with Visual Progress
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Top Expense Categories",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                modifier = Modifier.clickable { onNavigate(SpendWiseScreen.ANALYSIS) }
                            )
                        }

                        if (analytics.topCategories.isEmpty()) {
                            Text("No expenses recorded for this period.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        } else {
                            analytics.topCategories.take(4).forEach { (cat, amount) ->
                                val pct = if (analytics.totalExpense > 0) ((amount / analytics.totalExpense) * 100).toInt() else 0
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${SpendWiseCategories.getIcon(cat)}  $cat",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "$currencySymbol${String.format(Locale.US, "%.2f", amount)} ($pct%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
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
            }

            // 9. Budget Status Snapshot
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
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
                            Text(
                                text = "Category Budgets",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Manage",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                modifier = Modifier.clickable { onNavigate(SpendWiseScreen.BUDGETS) }
                            )
                        }

                        budgets.take(3).forEach { budget ->
                            val spent = recentTransactions
                                .filter { it.transactionType == TransactionType.EXPENSE && it.category.equals(budget.category, true) }
                                .sumOf { it.amount }
                            val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
                            val statusColor = when {
                                ratio > 1f -> RedRisk
                                ratio >= 0.8f -> AmberWarning
                                else -> EmeraldPrimary
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(budget.category, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    Text(
                                        text = "$currencySymbol${String.format(Locale.US, "%.0f", spent)} / $currencySymbol${String.format(Locale.US, "%.0f", budget.monthlyLimit)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = statusColor,
                                    trackColor = DarkSurfaceElevated
                                )
                            }
                        }
                    }
                }
            }

            // 10. AI Intelligence Card Highlight
            if (aiInsights.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI Spending Intelligence",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "View All Insights",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                modifier = Modifier.clickable { onNavigate(SpendWiseScreen.INSIGHTS) }
                            )
                        }
                        AiInsightCard(insight = aiInsights.first())
                    }
                }
            }

            // 11. Recent Transactions Quick List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Ledger Activity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "All Transactions (${recentTransactions.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight,
                        modifier = Modifier.clickable { onNavigate(SpendWiseScreen.TRANSACTIONS) }
                    )
                }
            }

            items(recentTransactions.take(5)) { tx ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (tx.isFlagged) AmberWarning.copy(alpha = 0.5f) else DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = SpendWiseCategories.getIcon(tx.category), fontSize = 18.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tx.merchant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (tx.isFlagged) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Flagged",
                                        tint = AmberWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${tx.category} • ${dateFormat.format(Date(tx.date))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        Text(
                            text = "${if (tx.transactionType == TransactionType.INCOME) "+" else "-"}$currencySymbol${String.format(Locale.US, "%.2f", tx.amount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.transactionType == TransactionType.INCOME) EmeraldLight else TextPrimary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
