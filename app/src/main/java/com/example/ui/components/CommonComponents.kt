package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiInsight
import com.example.data.model.RiskAlert
import com.example.data.model.RiskSeverity
import com.example.data.model.SpendWiseCategories
import com.example.ui.SpendWiseScreen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendWiseTopBar(
    currentScreen: SpendWiseScreen,
    currencySymbol: String,
    onOpenNav: () -> Unit,
    onNavigate: (SpendWiseScreen) -> Unit,
    isDemoMode: Boolean = true
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldContainer)
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                Column {
                    Text(
                        text = currentScreen.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isDemoMode) {
                        Text(
                            text = "SpendWise Demo Mode ($currencySymbol)",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenNav, modifier = Modifier.testTag("nav_menu_button")) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onNavigate(SpendWiseScreen.RISK) },
                modifier = Modifier.testTag("top_bar_risk_button")
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Risk Alerts",
                    tint = AmberWarning
                )
            }
            IconButton(
                onClick = { onNavigate(SpendWiseScreen.SETTINGS) },
                modifier = Modifier.testTag("top_bar_settings_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AR",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground,
            titleContentColor = TextPrimary
        )
    )
}

@Composable
fun MetricCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    changePercent: Double? = null,
    isPositiveGood: Boolean = true,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%,.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (changePercent != null) {
                val isUp = changePercent >= 0
                val color = if (isUp == isPositiveGood) EmeraldPrimary else RedRisk
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (isUp) "+" else ""}${String.format(Locale.US, "%.1f", changePercent)}% vs last month",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun SpendingHealthCard(
    healthScore: Int,
    status: String,
    utilizationPercent: Int,
    currencySymbol: String,
    remainingBudget: Double,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        healthScore >= 75 -> EmeraldPrimary
        healthScore >= 55 -> AmberWarning
        else -> RedRisk
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Health Index",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(barColor.copy(alpha = 0.15f))
                        .border(1.dp, barColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$healthScore",
                        fontWeight = FontWeight.ExtraBold,
                        color = barColor,
                        fontSize = 16.sp
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (healthScore / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = DarkSurfaceElevated
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget Used: $utilizationPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "Remaining: $currencySymbol${String.format(Locale.US, "%,.2f", remainingBudget)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remainingBudget >= 0) EmeraldLight else RedRisk
                )
            }
        }
    }
}

@Composable
fun RiskAlertCard(
    alert: RiskAlert,
    currencySymbol: String,
    onInvestigate: () -> Unit = {},
    onResolve: (() -> Unit)? = null
) {
    val (badgeColor, containerColor) = when (alert.severity) {
        RiskSeverity.HIGH -> Pair(RedRisk, RedContainer)
        RiskSeverity.MEDIUM -> Pair(AmberWarning, AmberContainer)
        RiskSeverity.LOW -> Pair(BlueInfo, Color(0xFF1E3A8A))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Risk Alert",
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = alert.merchant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = containerColor.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${alert.severity.name} RISK",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Text(
                text = alert.reason,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.baselineComparison,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", alert.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            if (onResolve != null) {
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(top = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onResolve,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Safe & Resolve", color = EmeraldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AiInsightCard(
    insight: AiInsight,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (insight.isThinkingMode) EmeraldPrimary.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (insight.isThinkingMode) Icons.Default.Psychology else Icons.Default.AutoAwesome,
                        contentDescription = "AI Intelligence",
                        tint = if (insight.isThinkingMode) EmeraldPrimary else TealSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (insight.isThinkingMode) "Gemini High Thinking" else insight.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (insight.isThinkingMode) EmeraldLight else TealSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (insight.searchGroundingSource != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BlueInfo.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Google Search Grounded",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueInfo,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = insight.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Supporting Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = insight.supportingData,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = insight.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldContainer.copy(alpha = 0.3f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Action: ${insight.suggestedAction}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldLight,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SimpleSpendingBarChart(
    dailyExpenses: List<Pair<String, Double>>,
    currencySymbol: String,
    selectedDayIndex: Int? = null,
    onSelectDay: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val maxVal = (dailyExpenses.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(10.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "7-Day Velocity & Outflows",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (selectedDayIndex != null && selectedDayIndex in dailyExpenses.indices) {
                    val sel = dailyExpenses[selectedDayIndex]
                    Text(
                        text = "Selected: ${sel.first} • $currencySymbol${String.format(Locale.US, "%.2f", sel.second)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "Peak: $currencySymbol${String.format(Locale.US, "%.0f", maxVal)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val displayDays = dailyExpenses.takeLast(7)
            displayDays.forEachIndexed { index, (day, amount) ->
                val ratio = (amount / maxVal).toFloat().coerceIn(0.06f, 1f)
                val isSelected = selectedDayIndex == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectDay?.invoke(index) }
                        .padding(horizontal = 2.dp)
                ) {
                    if (isSelected) {
                        Text(
                            text = "$currencySymbol${amount.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                when {
                                    isSelected -> EmeraldLight
                                    amount > maxVal * 0.75 -> AmberWarning
                                    else -> EmeraldPrimary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) EmeraldLight else TextMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
