package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpendWiseCategories
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueGuest: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(EmeraldContainer)
                .border(2.dp, EmeraldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SW",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = EmeraldLight
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSignUp) "Create your Account" else "Welcome to SpendWise",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldContainer.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "SECURED BY CLERK",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                )
            }
            Text(
                text = "• Zero-knowledge ledger encryption",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Social Auth Buttons (Clerk style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onAuthSuccess,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onAuthSuccess,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("Apple", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onAuthSuccess,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("GitHub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DarkBorder)
                    Text(" or with email ", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DarkBorder)
                }

                // Tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { isSignUp = false; error = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSignUp) EmeraldPrimary else Color.Transparent,
                            contentColor = if (!isSignUp) DarkBackground else TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isSignUp = true; error = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSignUp) EmeraldPrimary else Color.Transparent,
                            contentColor = if (isSignUp) DarkBackground else TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sign Up", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    label = { Text("Email address") },
                    placeholder = { Text("alex.rivera@example.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = TextSecondary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (error != null) {
                    Text(text = error!!, color = RedRisk, style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = {
                        if (email.isBlank() || !email.contains("@")) {
                            error = "Please enter a valid email address"
                            return@Button
                        }
                        if (password.length < 6) {
                            error = "Password must be at least 6 characters"
                            return@Button
                        }
                        onAuthSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isSignUp) "Create Clerk Account" else "Sign In with Clerk",
                        fontWeight = FontWeight.Bold,
                        color = DarkBackground,
                        fontSize = 15.sp
                    )
                }

                HorizontalDivider(color = DarkBorder)

                OutlinedButton(
                    onClick = onContinueGuest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_guest_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue as Verified Demo (Alex Rivera)", fontWeight = FontWeight.SemiBold, color = EmeraldLight)
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    initialIncome: Double = 3200.0,
    initialBudget: Double = 2400.0,
    initialCurrency: String = "$",
    onFinishOnboarding: (income: Double, budget: Double, currency: String, categories: List<String>, goal: String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 to 6
    var incomeText by remember { mutableStateOf(initialIncome.toString()) }
    var budgetText by remember { mutableStateOf(initialBudget.toString()) }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var selectedCategories by remember { mutableStateOf(setOf("Food", "Groceries", "Transportation", "Shopping", "Subscriptions")) }
    var financialGoal by remember { mutableStateOf("Build 3-month living emergency fund & cut impulse delivery") }

    val currencies = listOf("$" to "USD / CAD", "€" to "EUR", "£" to "GBP", "₹" to "INR", "¥" to "JPY", "A$" to "AUD")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Progress header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SpendWise Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                )
                Text(
                    text = "Step $step of 6",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            LinearProgressIndicator(
                progress = { step / 6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = EmeraldPrimary,
                trackColor = DarkSurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step Contents
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                when (step) {
                    1 -> {
                        Text("Welcome to SpendWise", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            "Let's personalize your financial telemetry in under 60 seconds. You'll establish your income baseline, budget caps, and focus categories.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    2 -> {
                        Text("What is your monthly income?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Include job wages, campus research stipends, or regular allowances.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        OutlinedTextField(
                            value = incomeText,
                            onValueChange = { incomeText = it },
                            label = { Text("Monthly Income ($selectedCurrency)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_income_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                    3 -> {
                        Text("Set your monthly spending budget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("We recommend allocating 70-80% of income to cover rent, food, transport, and discretionary items.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        OutlinedTextField(
                            value = budgetText,
                            onValueChange = { budgetText = it },
                            label = { Text("Monthly Target Spending ($selectedCurrency)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_budget_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                    4 -> {
                        Text("Choose your currency", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currencies.forEach { (symbol, name) ->
                                val isSelected = selectedCurrency == symbol
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedCurrency = symbol }
                                        .border(1.dp, if (isSelected) EmeraldPrimary else DarkBorder, RoundedCornerShape(10.dp)),
                                    color = if (isSelected) EmeraldContainer.copy(alpha = 0.4f) else DarkSurfaceElevated
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("$symbol  •  $name", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldLight)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    5 -> {
                        Text("Select your primary spending categories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Choose categories you interact with most frequently.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpendWiseCategories.ALL.take(8).chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { cat ->
                                        val isChecked = selectedCategories.contains(cat)
                                        FilterChip(
                                            selected = isChecked,
                                            onClick = {
                                                selectedCategories = if (isChecked) selectedCategories - cat else selectedCategories + cat
                                            },
                                            label = { Text("${SpendWiseCategories.getIcon(cat)} $cat") },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldContainer,
                                                selectedLabelColor = EmeraldLight
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    6 -> {
                        Text("What is your primary financial goal?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = financialGoal,
                            onValueChange = { financialGoal = it },
                            label = { Text("Primary Goal") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_goal_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Suggestions:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            listOf(
                                "Save 20% of monthly stipend for emergencies",
                                "Reduce DoorDash & takeout dining by half",
                                "Stop subscription creep & cancel unused streaming"
                            ).forEach { suggestion ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { financialGoal = suggestion }
                                ) {
                                    Text(
                                        text = "• $suggestion",
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (step < 6) {
                        step++
                    } else {
                        val inc = incomeText.toDoubleOrNull() ?: 3200.0
                        val bud = budgetText.toDoubleOrNull() ?: 2400.0
                        onFinishOnboarding(inc, bud, selectedCurrency, selectedCategories.toList(), financialGoal)
                    }
                },
                modifier = Modifier.testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (step == 6) "Launch Dashboard" else "Continue",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
