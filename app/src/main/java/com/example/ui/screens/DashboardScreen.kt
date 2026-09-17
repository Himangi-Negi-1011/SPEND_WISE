package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.runtime.*
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    analytics: SpendingAnalytics,
    riskAlerts: List<RiskAlert>,
    aiInsights: List<AiInsight>,
    budgets: List<BudgetEntity>,
    goals: List<GoalEntity> = emptyList(),
    recentTransactions: List<TransactionEntity>,
    savingOpportunities: List<SavingOpportunity>,
    detailedHealthOverview: DetailedHealthOverview,
    userProfile: UserProfile = UserProfile(),
    currencySymbol: String,
    timeHorizon: String = "THIS_MONTH",
    onTimeHorizonChange: (String) -> Unit = {},
    selectedDayIndex: Int? = null,
    onSelectDayIndex: (Int?) -> Unit = {},
    onNavigate: (SpendWiseScreen) -> Unit,
    onAddTransaction: () -> Unit,
    onQuickAddExpense: (merchant: String, amount: Double, category: String) -> Unit = { _, _, _ -> },
    onResolveAlert: (String) -> Unit = {},
    onApplyOpportunity: (SavingOpportunity) -> Unit = {},
    onContributeGoal: (goalId: String, amount: Double) -> Unit = { _, _ -> },
    onOpenCsvImport: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d", Locale.US)
    var appliedToastMessage by remember { mutableStateOf<String?>(null) }

    // 7-day daily spending data
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

    // Days remaining in the month and daily velocity
    val cal = Calendar.getInstance()
    val currentDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysRemaining = (maxDaysInMonth - currentDayOfMonth).coerceAtLeast(1)
    val dailyBurnRate = if (currentDayOfMonth > 0) analytics.totalExpense / currentDayOfMonth else 0.0
    val projectedMonthEndSpend = analytics.totalExpense + (dailyBurnRate * daysRemaining)
    val projectedSavings = (analytics.totalIncome - projectedMonthEndSpend).coerceAtLeast(0.0)

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

            // 1. Clerk Identity & Live Security Header
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer)
                                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.avatarInitials,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    fontSize = 13.sp
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = userProfile.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = EmeraldContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = if (userProfile.isDemoMode) "DEMO ACTIVE" else "CLERK VERIFIED",
                                            color = EmeraldLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${userProfile.authProvider} • ${userProfile.email}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { onNavigate(SpendWiseScreen.SETTINGS) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Interactive Time Horizon Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val horizons = listOf(
                        "THIS_MONTH" to "This Month",
                        "LAST_30_DAYS" to "Last 30 Days",
                        "THIS_WEEK" to "This Week"
                    )
                    horizons.forEach { (key, label) ->
                        val isSelected = timeHorizon == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) EmeraldContainer else DarkSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else DarkBorder
                            ),
                            modifier = Modifier.clickable { onTimeHorizonChange(key) }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) EmeraldLight else TextSecondary
                            )
                        }
                    }
                }
            }

            // Applied feedback banner
            if (appliedToastMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldContainer.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                Text(appliedToastMessage!!, color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { appliedToastMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // 3. Spending Health Indicator Card (Clickable to view transparent diagnostics)
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

            // 4. Actionable Risk Alert Banner (With 1-Tap Inline Resolution!)
            if (riskAlerts.isNotEmpty()) {
                item {
                    val topAlert = riskAlerts.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, RedRisk.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = RedContainer.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = RedRisk)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Risk Signal: ${topAlert.merchant}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFECACA),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = topAlert.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        onResolveAlert(topAlert.id)
                                        appliedToastMessage = "Alert for ${topAlert.merchant} resolved and marked safe."
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resolve & Mark Safe", color = EmeraldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(
                                    onClick = { onNavigate(SpendWiseScreen.RISK) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Risk Center →", color = RedRisk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Metric Cards Grid (2x2)
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

            // 6. Quick-Log Expense Bar (Instant 1-Tap Entry for Fast Testing)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quick Log Expense",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "1-Tap Entry",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                QuickLogChip(
                                    label = "+$currencySymbol 15 Lunch",
                                    icon = "☕",
                                    onClick = {
                                        onQuickAddExpense("Artisan Cafe Lunch", 15.0, "Food")
                                        appliedToastMessage = "Logged: $currencySymbol 15 Lunch (Food)"
                                    }
                                )
                            }
                            item {
                                QuickLogChip(
                                    label = "+$currencySymbol 45 Groceries",
                                    icon = "🛒",
                                    onClick = {
                                        onQuickAddExpense("Trader Joe's Market", 45.0, "Food")
                                        appliedToastMessage = "Logged: $currencySymbol 45 Groceries (Food)"
                                    }
                                )
                            }
                            item {
                                QuickLogChip(
                                    label = "+$currencySymbol 25 Uber",
                                    icon = "🚗",
                                    onClick = {
                                        onQuickAddExpense("Uber Mobility Ride", 25.0, "Transport")
                                        appliedToastMessage = "Logged: $currencySymbol 25 Uber (Transport)"
                                    }
                                )
                            }
                            item {
                                QuickLogChip(
                                    label = "+$currencySymbol 12 Streaming",
                                    icon = "📺",
                                    onClick = {
                                        onQuickAddExpense("Digital Stream Pro", 12.0, "Subscriptions")
                                        appliedToastMessage = "Logged: $currencySymbol 12 Subscriptions"
                                    }
                                )
                            }
                            item {
                                QuickLogChip(
                                    label = "+ Custom Entry",
                                    icon = "✏️",
                                    onClick = onAddTransaction
                                )
                            }
                        }
                    }
                }
            }

            // 7. Executive Navigation Action Chips
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
                            label = "Goals (${goals.size})",
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
                            icon = Icons.Default.Analytics,
                            label = "Spending Analysis",
                            color = BlueInfo,
                            onClick = { onNavigate(SpendWiseScreen.ANALYSIS) }
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
                }
            }

            // 8. Interactive 7-Day Velocity Bar Chart
            item {
                SimpleSpendingBarChart(
                    dailyExpenses = dailyExpenses,
                    currencySymbol = currencySymbol,
                    selectedDayIndex = selectedDayIndex,
                    onSelectDay = { onSelectDayIndex(it) }
                )
            }

            // 9. Cashflow Velocity & Burn Rate Card
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
                                text = "Burn Rate & Cashflow Runway",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(shape = RoundedCornerShape(6.dp), color = DarkSurfaceElevated) {
                                Text(
                                    text = "$daysRemaining days remaining",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Daily Burn Rate", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%.1f", dailyBurnRate)}/day",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Projected Month-End", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%.0f", projectedMonthEndSpend)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (projectedMonthEndSpend > userProfile.targetBudget) AmberWarning else EmeraldLight
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (analytics.totalExpense / (userProfile.targetBudget.coerceAtLeast(100.0))).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (analytics.totalExpense > userProfile.targetBudget) RedRisk else EmeraldPrimary,
                            trackColor = DarkSurfaceElevated
                        )
                    }
                }
            }

            // 10. Active Goals Snapshot (With 1-Tap Contribution!)
            if (goals.isNotEmpty()) {
                item {
                    val primaryGoal = goals.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = primaryGoal.icon, fontSize = 18.sp)
                                    Column {
                                        Text(primaryGoal.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Text("${primaryGoal.progressPercent}% of $currencySymbol${primaryGoal.targetAmount.toInt()}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            onContributeGoal(primaryGoal.id, 50.0)
                                            appliedToastMessage = "+$currencySymbol 50 deposited to ${primaryGoal.title}!"
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
                                    ) {
                                        Text("+$currencySymbol 50", color = EmeraldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { onNavigate(SpendWiseScreen.GOALS) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "View Goals", tint = EmeraldLight)
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { (primaryGoal.progressPercent / 100f).coerceIn(0f, 1f) },
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

            // 11. Saving Opportunities (Actionable!)
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
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "$currencySymbol${String.format(Locale.US, "%.0f", opp.potentialMonthlySavings)}",
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldLight,
                                                fontSize = 14.sp
                                            )
                                            TextButton(
                                                onClick = {
                                                    onApplyOpportunity(opp)
                                                    appliedToastMessage = "Optimized budget for ${opp.category}."
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                            ) {
                                                Text("Apply", color = EmeraldLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 12. Top Spending Categories with Visual Progress
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

            // 13. Category Budgets Snapshot
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

            // 14. AI Intelligence Card Highlight
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

            // 15. Recent Ledger Activity
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
fun QuickLogChip(
    label: String,
    icon: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
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
