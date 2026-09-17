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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiInsight
import com.example.data.model.RiskAlert
import com.example.data.model.RiskSeverity
import com.example.ui.components.AiInsightCard
import com.example.ui.components.RiskAlertCard
import com.example.ui.theme.*

@Composable
fun RiskDetectionScreen(
    riskAlerts: List<RiskAlert>,
    currencySymbol: String,
    onResolveAlert: (String) -> Unit = {}
) {
    var selectedSeverity by remember { mutableStateOf<RiskSeverity?>(null) }

    val filteredAlerts = if (selectedSeverity == null) {
        riskAlerts
    } else {
        riskAlerts.filter { it.severity == selectedSeverity }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header overview card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                        Text(
                            text = "SpendWise Risk Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Continuous automated telemetry checks for unusually large purchases, category spikes, repeated daily charges, and sneaky subscription fee creeping.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Severity filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSeverity == null,
                    onClick = { selectedSeverity = null },
                    label = { Text("All (${riskAlerts.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldContainer,
                        selectedLabelColor = EmeraldLight
                    )
                )
                listOf(RiskSeverity.HIGH, RiskSeverity.MEDIUM, RiskSeverity.LOW).forEach { sev ->
                    val count = riskAlerts.count { it.severity == sev }
                    val isSelected = selectedSeverity == sev
                    val color = when (sev) {
                        RiskSeverity.HIGH -> RedRisk
                        RiskSeverity.MEDIUM -> AmberWarning
                        RiskSeverity.LOW -> BlueInfo
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSeverity = if (isSelected) null else sev },
                        label = { Text("${sev.name} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = color
                        )
                    )
                }
            }
        }

        if (filteredAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(42.dp))
                        Text("No Risk Signals Detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Your recent spending patterns align with historical category baselines.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        } else {
            items(filteredAlerts, key = { it.id }) { alert ->
                RiskAlertCard(
                    alert = alert,
                    currencySymbol = currencySymbol,
                    onResolve = { onResolveAlert(alert.id) }
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Risk Detection Ethics Note", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("Signals flag spending outliers and budget drift for your awareness. SpendWise never reports to credit bureaus or categorizes transactions as fraudulent.", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun AiInsightsScreen(
    aiInsights: List<AiInsight>,
    isGenerating: Boolean,
    currencySymbol: String,
    onRefreshAi: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredInsights = if (selectedCategory == null) {
        aiInsights
    } else {
        aiInsights.filter { it.category.equals(selectedCategory, true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Gemini AI Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Gemini AI Intelligence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("High Thinking & Search Grounding", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                            }
                        }

                        Button(
                            onClick = onRefreshAi,
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("ai_reanalyze_button")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Re-Analyze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text(
                        text = "SpendWise harnesses Google Gemini 3.1 Pro Preview with High Thinking Level for deep financial pattern analysis, coupled with Gemini 3.5 Flash Search Grounding for current student living benchmarks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            val categories = listOf("Category increases", "Unusual activity", "Budget observations", "Recurring spending", "Savings opportunities")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${aiInsights.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldContainer,
                            selectedLabelColor = EmeraldLight
                        )
                    )
                }
                items(categories) { cat ->
                    val count = aiInsights.count { it.category.equals(cat, true) }
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = if (isSelected) null else cat },
                        label = { Text("$cat ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldContainer,
                            selectedLabelColor = EmeraldLight
                        )
                    )
                }
            }
        }

        items(filteredInsights, key = { it.id }) { insight ->
            AiInsightCard(insight = insight)
        }

        // Informational disclaimer
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Educational Purpose", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("AI Insights are generated to assist your personal spending awareness. They do not constitute certified financial planning, tax guidance, or investment advice.", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
