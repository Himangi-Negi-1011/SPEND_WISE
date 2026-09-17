package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class RiskSeverity {
    LOW,
    MEDIUM,
    HIGH
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val merchant: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isFlagged: Boolean = false,
    val riskReason: String? = null,
    val isDemo: Boolean = false
) {
    val transactionType: TransactionType
        get() = if (type.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String, // e.g. "Food", "Shopping", "TOTAL"
    val monthlyLimit: Double,
    val monthYear: String = "", // e.g. "2026-09"
    val isDemo: Boolean = false
)

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: String = "Dec 2026",
    val monthlyContribution: Double = 150.0,
    val category: String = "Savings",
    val icon: String = "🎯"
) {
    val progressPercent: Int
        get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100) else 0

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}

@Entity(tableName = "recurring_subscriptions")
data class RecurringEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val merchant: String,
    val amount: Double,
    val frequency: String = "Monthly", // "Monthly", "Annual", "Weekly"
    val category: String = "Subscriptions",
    val nextPaymentDate: String = "Oct 1, 2026",
    val status: String = "ACTIVE", // "ACTIVE", "REVIEW", "ESSENTIAL"
    val lastChargeDate: Long = System.currentTimeMillis()
) {
    val annualizedCost: Double
        get() = when (frequency.lowercase()) {
            "monthly" -> amount * 12
            "weekly" -> amount * 52
            "annual" -> amount
            else -> amount * 12
        }
}

data class SavingOpportunity(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val observation: String,
    val supportingNumbers: String,
    val whyItMatters: String,
    val suggestedAction: String,
    val potentialMonthlySavings: Double,
    val category: String,
    val severity: String = "MEDIUM"
)

data class CopilotMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedPrompts: List<String> = emptyList()
)

data class UserProfile(
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@spendwise.app",
    val monthlyIncome: Double = 4200.0,
    val targetBudget: Double = 2740.0,
    val currencySymbol: String = "$",
    val financialGoal: String = "Build 3-month safety buffer & curb food delivery surge",
    val mainCategories: List<String> = listOf("Food", "Shopping", "Transport", "Bills", "Subscriptions", "Entertainment"),
    val isOnboarded: Boolean = true,
    val isAuthenticated: Boolean = true,
    val isDemoMode: Boolean = true,
    val clerkUserId: String = "user_3JRjNj5U6cWAoxhfQ3VOeocOG1b",
    val authProvider: String = "Clerk SSO",
    val clerkSessionStatus: String = "Active (Dev)",
    val avatarInitials: String = "AR",
    val tier: String = "SpendWise Pro",
    val biometricsEnabled: Boolean = true,
    val memberSince: String = "September 2025"
)

data class RiskAlert(
    val id: String = UUID.randomUUID().toString(),
    val transactionId: String? = null,
    val merchant: String,
    val amount: Double,
    val category: String,
    val reason: String,
    val baselineComparison: String,
    val severity: RiskSeverity,
    val date: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

data class AiInsight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val supportingData: String,
    val explanation: String,
    val suggestedAction: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = true,
    val isThinkingMode: Boolean = false,
    val searchGroundingSource: String? = null
)

object SpendWiseCategories {
    val ALL = listOf(
        "Food",
        "Shopping",
        "Transport",
        "Bills",
        "Entertainment",
        "Education",
        "Subscriptions",
        "Healthcare",
        "Travel",
        "Rent",
        "Utilities",
        "Other"
    )

    fun getIcon(category: String): String {
        return when (category.lowercase()) {
            "food" -> "🍔"
            "groceries" -> "🛒"
            "transportation", "transport" -> "🚗"
            "shopping" -> "🛍️"
            "entertainment" -> "🎬"
            "education" -> "🎓"
            "bills" -> "📄"
            "healthcare" -> "💊"
            "travel" -> "✈️"
            "subscriptions", "subscription" -> "🔁"
            "rent" -> "🏠"
            "utilities" -> "⚡"
            else -> "💳"
        }
    }
}

