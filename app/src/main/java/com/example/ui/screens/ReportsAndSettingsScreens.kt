package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.SpendWiseCategories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.domain.analysis.SpendingAnalytics
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ReportsScreen(
    analytics: SpendingAnalytics,
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    profile: UserProfile
) {
    val context = LocalContext.current

    val reportText = buildString {
        appendLine("===== SPENDWISE MONTHLY STATEMENT =====")
        appendLine("User: ${profile.name} (${profile.email})")
        appendLine("Period: Current Billing Cycle")
        appendLine("Total Income: ${profile.currencySymbol}${String.format(Locale.US, "%.2f", analytics.totalIncome)}")
        appendLine("Total Expenses: ${profile.currencySymbol}${String.format(Locale.US, "%.2f", analytics.totalExpense)}")
        appendLine("Net Balance: ${profile.currencySymbol}${String.format(Locale.US, "%.2f", analytics.totalBalance)}")
        appendLine("Spending Health Index: ${analytics.healthScore}/100 (${analytics.healthStatus})")
        appendLine("\nTop Expense Categories:")
        analytics.topCategories.forEach { (cat, amt) ->
            appendLine("  - $cat: ${profile.currencySymbol}${String.format(Locale.US, "%.2f", amt)}")
        }
        appendLine("\nGenerated locally by SpendWise Personal Expense & Risk Detector.")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Monthly Financial Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Current Cycle • ${transactions.size} ledger entries", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SpendWise Report", reportText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("copy_report_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Financial Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Income", color = TextSecondary)
                        Text("${profile.currencySymbol}${String.format(Locale.US, "%.2f", analytics.totalIncome)}", fontWeight = FontWeight.Bold, color = EmeraldLight)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Outflows", color = TextSecondary)
                        Text("${profile.currencySymbol}${String.format(Locale.US, "%.2f", analytics.totalExpense)}", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    HorizontalDivider(color = DarkBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Savings Added", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${profile.currencySymbol}${String.format(Locale.US, "%.2f", analytics.totalBalance)}", fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Category Outflows Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    analytics.topCategories.forEach { (cat, amount) ->
                        val pct = if (analytics.totalExpense > 0) ((amount / analytics.totalExpense) * 100).toInt() else 0
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${SpendWiseCategories.getIcon(cat)}  $cat", color = TextSecondary)
                            Text("${profile.currencySymbol}${String.format(Locale.US, "%.2f", amount)} ($pct%)", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun SettingsScreen(
    profile: UserProfile,
    onUpdateProfile: (UserProfile) -> Unit,
    onSeedDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(profile.name) }
    var incomeText by remember { mutableStateOf(profile.monthlyIncome.toString()) }
    var budgetText by remember { mutableStateOf(profile.targetBudget.toString()) }
    var currencySymbol by remember { mutableStateOf(profile.currencySymbol) }
    var alertSensitivity by remember { mutableStateOf(true) }

    val currencies = listOf("$", "€", "£", "₹", "¥", "A$")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // User Profile Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer)
                            .border(1.dp, EmeraldLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.avatarInitials, fontWeight = FontWeight.ExtraBold, color = EmeraldLight, fontSize = 20.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Surface(shape = RoundedCornerShape(4.dp), color = EmeraldContainer.copy(alpha = 0.5f)) {
                                Text(if (profile.isDemoMode) "Demo Profile" else "Verified User", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = EmeraldLight, fontSize = 10.sp)
                            }
                        }
                        Text(profile.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        // Clerk Authentication & Session Security Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                            Text("Clerk Identity & Security", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldContainer.copy(alpha = 0.6f)) {
                            Text(
                                text = profile.clerkSessionStatus.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Auth Provider", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(profile.authProvider, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Clerk User ID", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(profile.clerkUserId.take(16) + "...", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Instance Domain", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("tolerant-anemone-6963", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onSignOut()
                            Toast.makeText(context, "Signed out of Clerk session", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedRisk.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedRisk)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = RedRisk, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out of Clerk Session", fontWeight = FontWeight.SemiBold, color = RedRisk)
                    }
                }
            }
        }

        // Financial Baseline Settings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Financial Profile & Currency", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = incomeText,
                        onValueChange = { incomeText = it },
                        label = { Text("Monthly Income ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_income_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Target Spending Budget ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_budget_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Active Currency Symbol", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        currencies.forEach { sym ->
                            val isSelected = currencySymbol == sym
                            FilterChip(
                                selected = isSelected,
                                onClick = { currencySymbol = sym },
                                label = { Text(sym) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldContainer,
                                    selectedLabelColor = EmeraldLight
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val inc = incomeText.toDoubleOrNull() ?: profile.monthlyIncome
                            val bud = budgetText.toDoubleOrNull() ?: profile.targetBudget
                            onUpdateProfile(profile.copy(monthlyIncome = inc, targetBudget = bud, currencySymbol = currencySymbol))
                            Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("settings_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Risk & Alerts Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Risk Preferences", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Real-time Outlier Alerts", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text("Notify when individual expenses exceed 25% of monthly budget", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = alertSensitivity,
                            onCheckedChange = { alertSensitivity = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldLight, checkedTrackColor = EmeraldContainer)
                        )
                    }
                }
            }
        }

        // Demo Data & Storage Reset
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Data Persistence & Demo Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("SpendWise stores transactions locally on your device via Room SQLite database.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                onSeedDemoData()
                                Toast.makeText(context, "Loaded realistic sample dataset!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("settings_reset_demo_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealSecondary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealSecondary)
                        ) {
                            Text("Reset Demo Data", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                onClearAllData()
                                Toast.makeText(context, "Local transactions cleared.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("settings_clear_data_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedRisk),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedRisk)
                        ) {
                            Text("Clear All Data", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("SpendWise v1.0.0", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("Personal Expense & Financial Risk Detector. Powered by Android Jetpack Compose, Room SQLite, and Google Gemini AI.", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
