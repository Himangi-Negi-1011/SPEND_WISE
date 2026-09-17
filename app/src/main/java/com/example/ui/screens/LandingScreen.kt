package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    onStartTracking: () -> Unit,
    onExploreDemo: () -> Unit,
    onOpenAuth: () -> Unit = onStartTracking
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Brand Header Pill
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = EmeraldContainer.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldLight)
                )
                Text(
                    text = "SpendWise 2.0 • Luxury Financial Intelligence",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldLight,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Hero Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Understand your money.\nControl your spending.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
            Text(
                text = "Autonomous spending intelligence with transparent risk audits, recurring subscription tracking, and context-grounded AI copilot guidance.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStartTracking,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("landing_start_tracking_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Launch App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkBackground
                )
            }
            OutlinedButton(
                onClick = onExploreDemo,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("landing_explore_demo_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(
                    text = "Live Demo Data",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        // Interactive Showcase Card Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Spending Run-Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "$1,607.03",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainer
                    ) {
                        Text(
                            text = "Health Score 82 / 100",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Budget: 67% utilized", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("$792.97 remaining envelope", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                    }
                    LinearProgressIndicator(
                        progress = { 0.67f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = DarkSurfaceElevated
                    )
                }

                // Mock Risk Banner inside preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RedContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedRisk.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = RedRisk,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Risk Signal: Uber Eats order $78.50 exceeded 3.2x baseline",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFECACA),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Key Value Propositions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Architected for young professionals & students",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            FeatureItem(
                icon = Icons.Default.Shield,
                title = "Risk Detection Engine",
                description = "Deterministic behavioral rules detect sudden category spikes, unexpected price creeping, and off-budget impulses."
            )

            FeatureItem(
                icon = Icons.Default.Repeat,
                title = "Subscription & Overhead Auditor",
                description = "Continuous scanning aggregates recurring commitments and highlights under-review services before renewal."
            )

            FeatureItem(
                icon = Icons.Default.TrackChanges,
                title = "Financial Goals & Milestones",
                description = "Set visual milestones for emergency reserves, tech investments, and travel with instant micro-deposits."
            )

            FeatureItem(
                icon = Icons.Default.AutoAwesome,
                title = "Grounded Copilot AI",
                description = "Context-aware conversational assistance powered by Google Gemini and real ledger mathematics."
            )

            FeatureItem(
                icon = Icons.Default.VerifiedUser,
                title = "Clerk-Ready Security",
                description = "Enterprise session management, biometric unlock toggles, and zero unconsented third-party data tracking."
            )
        }

        // Informational Disclaimer
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkSurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Informational Disclaimer",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "SpendWise is an informational personal-finance intelligence app designed to build spending discipline. It does not provide registered financial advice or legal fraud underwriting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldContainer)
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
