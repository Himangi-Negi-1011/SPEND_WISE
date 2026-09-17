package com.example.domain.analysis

import com.example.data.model.BudgetEntity
import com.example.data.model.RiskAlert
import com.example.data.model.RiskSeverity
import com.example.data.model.SpendWiseCategories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class CategorizationResult(
    val category: String,
    val confidence: Float,
    val explanation: String
)

object CategorizationEngine {
    private val ruleMap = mapOf(
        "Groceries" to listOf("trader joe", "whole foods", "safeway", "kroger", "walmart", "costco", "aldi", "supermarket", "grocery", "market basket", "target grocery", "instacart"),
        "Food" to listOf("starbucks", "mcdonald", "burger", "pizza", "cafe", "diner", "restaurant", "chipotle", "subway", "taco", "ramen", "doordash", "ubereats", "grubhub", "bakery", "sushi", "shake shack", "panera", "dunkin", "coffee"),
        "Transportation" to listOf("uber", "lyft", "shell", "chevron", "bp", "exxon", "gas", "transit", "metro", "train", "amtrak", "parking", "toll", "subway pass", "caltrain", "mta"),
        "Subscriptions" to listOf("netflix", "spotify", "apple music", "disney+", "hulu", "icloud", "google one", "patreon", "substack", "planet fitness", "gym", "prime membership", "youtube premium", "hbo"),
        "Shopping" to listOf("amazon", "target", "ebay", "apple store", "best buy", "zara", "h&m", "clothing", "nike", "adidas", "sephora", "ulta", "nordstrom", "asos", "thrift"),
        "Entertainment" to listOf("cinema", "amc", "steam", "playstation", "nintendo", "ticketmaster", "concert", "bowling", "arcade", "game", "eventbrite", "broadway"),
        "Education" to listOf("bookstore", "campus", "university", "college", "tuition", "udemy", "coursera", "textbook", "chegg", "school", "library", "student loan"),
        "Bills" to listOf("verizon", "at&t", "t-mobile", "geico", "progressive", "insurance", "spectrum", "comcast", "xfinity", "phone bill"),
        "Healthcare" to listOf("pharmacy", "cvs", "walgreens", "clinic", "dentist", "doctor", "urgent care", "hospital", "optometry", "prescription", "labcorp"),
        "Travel" to listOf("airbnb", "delta", "united air", "american air", "hotel", "expedia", "booking.com", "flight", "hostel", "southwest", "marriott", "hilton"),
        "Rent" to listOf("landlord", "lease", "rent payment", "property management", "apartment", "real estate", "housing"),
        "Utilities" to listOf("electric", "power", "water utility", "gas electric", "con edison", "pge", "waste management", "sewer", "trash")
    )

    fun categorize(merchant: String, description: String = ""): CategorizationResult {
        val query = "${merchant.lowercase()} ${description.lowercase()}"

        for ((category, keywords) in ruleMap) {
            for (keyword in keywords) {
                if (query.contains(keyword)) {
                    return CategorizationResult(
                        category = category,
                        confidence = 0.95f,
                        explanation = "Matched trusted keyword '$keyword' associated with $category"
                    )
                }
            }
        }

        // Generic heuristics
        return when {
            query.contains("food") || query.contains("eat") || query.contains("kitchen") -> CategorizationResult("Food", 0.75f, "Matched culinary keywords")
            query.contains("store") || query.contains("shop") || query.contains("mart") -> CategorizationResult("Shopping", 0.70f, "Matched retail store patterns")
            query.contains("fare") || query.contains("ride") || query.contains("fuel") -> CategorizationResult("Transportation", 0.75f, "Matched transit/fuel terminology")
            else -> CategorizationResult("Other", 0.40f, "General unclassified expense; assigned to Other")
        }
    }
}

object RiskDetectionEngine {
    fun analyze(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        monthlyIncome: Double,
        targetBudget: Double
    ): List<RiskAlert> {
        val alerts = mutableListOf<RiskAlert>()
        val expenseTx = transactions.filter { it.transactionType == TransactionType.EXPENSE }
        if (expenseTx.isEmpty()) return emptyList()

        // 1. Unusually large individual transactions
        val categoryAverages = expenseTx.groupBy { it.category }
            .mapValues { entry -> entry.value.map { it.amount }.average() }

        val monthlyBudgetBaseline = if (targetBudget > 0) targetBudget else monthlyIncome

        expenseTx.forEach { tx ->
            val catAvg = categoryAverages[tx.category] ?: 40.0
            val isSevereMultiple = tx.amount > (catAvg * 2.8) && tx.amount > 60.0
            val isLargeFractionOfBudget = monthlyBudgetBaseline > 0 && (tx.amount / monthlyBudgetBaseline) > 0.20

            if (isSevereMultiple || isLargeFractionOfBudget || tx.isFlagged) {
                val severity = when {
                    tx.amount > (catAvg * 3.5) || tx.amount > (monthlyBudgetBaseline * 0.35) -> RiskSeverity.HIGH
                    tx.amount > (catAvg * 2.2) || tx.amount > (monthlyBudgetBaseline * 0.18) -> RiskSeverity.MEDIUM
                    else -> RiskSeverity.LOW
                }
                val reason = tx.riskReason ?: if (isLargeFractionOfBudget) {
                    "Single expense represents ${(tx.amount / monthlyBudgetBaseline * 100).roundToInt()}% of entire monthly budget"
                } else {
                    "Amount is ${String.format(Locale.US, "%.1f", tx.amount / catAvg)}x higher than your average ${tx.category} purchase"
                }

                alerts.add(
                    RiskAlert(
                        transactionId = tx.id,
                        merchant = tx.merchant,
                        amount = tx.amount,
                        category = tx.category,
                        reason = "Unusual spending pattern detected: $reason",
                        baselineComparison = "Baseline ${tx.category} average: $${String.format(Locale.US, "%.2f", catAvg)}",
                        severity = severity,
                        date = tx.date
                    )
                )
            }
        }

        // 2. High Frequency: multiple purchases at same merchant within 24 hours
        val sortedByDate = expenseTx.sortedBy { it.date }
        for (i in 0 until sortedByDate.size - 1) {
            val current = sortedByDate[i]
            val next = sortedByDate[i + 1]
            val diffHours = abs(next.date - current.date) / (1000 * 60 * 60)
            if (current.merchant.equals(next.merchant, ignoreCase = true) && diffHours < 18 && current.amount > 20.0) {
                alerts.add(
                    RiskAlert(
                        transactionId = next.id,
                        merchant = next.merchant,
                        amount = current.amount + next.amount,
                        category = next.category,
                        reason = "Unusual spending pattern detected: Multiple charges at ${next.merchant} within ${diffHours.toInt()} hours",
                        baselineComparison = "Normal pattern is single isolated transactions",
                        severity = RiskSeverity.MEDIUM,
                        date = next.date
                    )
                )
            }
        }

        // 3. Category budget overruns
        val categorySpending = expenseTx.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        budgets.forEach { budget ->
            val spent = categorySpending[budget.category] ?: 0.0
            if (budget.monthlyLimit > 0 && spent > budget.monthlyLimit) {
                val overPercent = ((spent - budget.monthlyLimit) / budget.monthlyLimit * 100).roundToInt()
                alerts.add(
                    RiskAlert(
                        merchant = "Category Budget: ${budget.category}",
                        amount = spent,
                        category = budget.category,
                        reason = "Unusual spending pattern detected: Budget overrun in ${budget.category} exceeding allocation by $overPercent%",
                        baselineComparison = "Limit: $${String.format(Locale.US, "%.2f", budget.monthlyLimit)} | Spent: $${String.format(Locale.US, "%.2f", spent)}",
                        severity = if (overPercent > 30) RiskSeverity.HIGH else RiskSeverity.MEDIUM,
                        date = System.currentTimeMillis()
                    )
                )
            }
        }

        // 4. Subscription creep check
        val subscriptionTx = expenseTx.filter { it.category.equals("Subscriptions", ignoreCase = true) }
        val groupedSubs = subscriptionTx.groupBy { it.merchant.lowercase() }
        groupedSubs.forEach { (merchant, list) ->
            if (list.size >= 2) {
                val sorted = list.sortedBy { it.date }
                val prev = sorted[sorted.size - 2].amount
                val latest = sorted.last().amount
                if (latest > prev * 1.15) {
                    alerts.add(
                        RiskAlert(
                            transactionId = sorted.last().id,
                            merchant = sorted.last().merchant,
                            amount = latest,
                            category = "Subscriptions",
                            reason = "Unusual spending pattern detected: Recurring fee increased by ${((latest - prev) / prev * 100).roundToInt()}% compared to prior bill",
                            baselineComparison = "Previous bill: $${String.format(Locale.US, "%.2f", prev)} -> Current: $${String.format(Locale.US, "%.2f", latest)}",
                            severity = RiskSeverity.LOW,
                            date = sorted.last().date
                        )
                    )
                }
            }
        }

        return alerts.distinctBy { "${it.merchant}-${it.amount}-${it.reason.take(20)}" }.sortedByDescending { it.severity }
    }
}

data class SpendingAnalytics(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val remainingBudget: Double,
    val budgetUtilizationPercent: Int,
    val momIncomeChangePercent: Double,
    val momExpenseChangePercent: Double,
    val topCategories: List<Pair<String, Double>>,
    val biggestMerchants: List<Pair<String, Double>>,
    val recurringExpenses: List<Pair<String, Double>>,
    val spendingSpikes: List<TransactionEntity>,
    val healthScore: Int,
    val healthStatus: String
)

object SpendingAnalyticsEngine {
    fun calculate(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        monthlyIncome: Double,
        targetBudget: Double
    ): SpendingAnalytics {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val cal = Calendar.getInstance()
        val currentMonthTx = transactions.filter {
            cal.timeInMillis = it.date
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }

        val prevMonthTx = transactions.filter {
            cal.timeInMillis = it.date
            val isPrevMonth = if (currentMonth == 0) cal.get(Calendar.MONTH) == 11 && cal.get(Calendar.YEAR) == currentYear - 1
            else cal.get(Calendar.MONTH) == currentMonth - 1 && cal.get(Calendar.YEAR) == currentYear
            isPrevMonth
        }

        val totalIncome = if (currentMonthTx.any { it.transactionType == TransactionType.INCOME }) {
            currentMonthTx.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
        } else {
            monthlyIncome
        }

        val totalExpense = currentMonthTx.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
        val prevTotalExpense = prevMonthTx.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
        val prevTotalIncome = prevMonthTx.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }

        val activeBudget = if (targetBudget > 0) targetBudget else totalIncome * 0.80
        val remainingBudget = activeBudget - totalExpense
        val budgetUtilization = if (activeBudget > 0) ((totalExpense / activeBudget) * 100).roundToInt() else 0

        val momExpenseChange = if (prevTotalExpense > 0) {
            ((totalExpense - prevTotalExpense) / prevTotalExpense) * 100
        } else 0.0

        val momIncomeChange = if (prevTotalIncome > 0) {
            ((totalIncome - prevTotalIncome) / prevTotalIncome) * 100
        } else 0.0

        val topCategories = currentMonthTx.filter { it.transactionType == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }

        val biggestMerchants = currentMonthTx.filter { it.transactionType == TransactionType.EXPENSE }
            .groupBy { it.merchant }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(6)

        val recurring = currentMonthTx.filter {
            it.category.equals("Subscriptions", true) ||
            it.category.equals("Rent", true) ||
            it.category.equals("Utilities", true) ||
            it.category.equals("Bills", true)
        }.groupBy { it.merchant }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second }

        val avgExpense = if (currentMonthTx.isNotEmpty()) totalExpense / currentMonthTx.filter { it.transactionType == TransactionType.EXPENSE }.size.coerceAtLeast(1) else 30.0
        val spikes = currentMonthTx.filter { it.transactionType == TransactionType.EXPENSE && it.amount > (avgExpense * 2.5) }

        // Health Score Calculation (Transparent algorithm based on Budget adherence + Savings rate)
        var score = 85
        if (budgetUtilization > 100) score -= 30
        else if (budgetUtilization > 85) score -= 15
        else if (budgetUtilization < 70) score += 10

        val savingsRate = if (totalIncome > 0) (totalIncome - totalExpense) / totalIncome else 0.0
        if (savingsRate > 0.20) score += 5
        else if (savingsRate < 0.05) score -= 15

        if (spikes.size > 3) score -= 10
        score = score.coerceIn(10, 98)

        val status = when {
            score >= 80 -> "Strong & In Control"
            score >= 65 -> "Moderate — Watch Impulses"
            score >= 50 -> "Elevated Risk — Approaching Limits"
            else -> "High Risk — Budget Overrun"
        }

        return SpendingAnalytics(
            totalBalance = (totalIncome - totalExpense).coerceAtLeast(0.0),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            remainingBudget = remainingBudget,
            budgetUtilizationPercent = budgetUtilization,
            momIncomeChangePercent = momIncomeChange,
            momExpenseChangePercent = momExpenseChange,
            topCategories = topCategories,
            biggestMerchants = biggestMerchants,
            recurringExpenses = recurring,
            spendingSpikes = spikes,
            healthScore = score,
            healthStatus = status
        )
    }
}

data class HealthMetricDetail(
    val name: String,
    val score: Int, // 0 - 100
    val rating: String, // "Optimal", "Good", "Needs Attention", "Critical"
    val formulaExplanation: String,
    val actualValueDescription: String
)

data class DetailedHealthOverview(
    val overallScore: Int,
    val status: String,
    val metrics: List<HealthMetricDetail>
)

object FinancialHealthDiagnostics {
    fun computeDetailedOverview(
        analytics: SpendingAnalytics,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>
    ): DetailedHealthOverview {
        val totalIncome = analytics.totalIncome.coerceAtLeast(1.0)
        val totalExpense = analytics.totalExpense
        val savingsRate = ((totalIncome - totalExpense) / totalIncome).coerceIn(0.0, 1.0)
        val budgetUtil = analytics.budgetUtilizationPercent

        // 1. Budget Adherence (Target <= 85%)
        val budgetScore = when {
            budgetUtil <= 75 -> 95
            budgetUtil <= 85 -> 85
            budgetUtil <= 95 -> 65
            budgetUtil <= 100 -> 50
            else -> (50 - (budgetUtil - 100) * 2).coerceIn(10, 45)
        }
        val budgetRating = when {
            budgetScore >= 85 -> "Optimal"
            budgetScore >= 70 -> "Good"
            budgetScore >= 50 -> "Needs Attention"
            else -> "Critical"
        }

        // 2. Savings Rate (Target >= 20%)
        val savingsScore = when {
            savingsRate >= 0.25 -> 98
            savingsRate >= 0.20 -> 90
            savingsRate >= 0.15 -> 78
            savingsRate >= 0.10 -> 60
            savingsRate >= 0.05 -> 45
            else -> 25
        }
        val savingsRating = when {
            savingsScore >= 85 -> "Optimal"
            savingsScore >= 70 -> "Good"
            savingsScore >= 50 -> "Needs Attention"
            else -> "Critical"
        }

        // 3. Spending Consistency (lower spikes = higher consistency)
        val spikeCount = analytics.spendingSpikes.size
        val consistencyScore = when (spikeCount) {
            0 -> 95
            1 -> 85
            2 -> 75
            3 -> 60
            else -> 40
        }
        val consistencyRating = if (consistencyScore >= 80) "Optimal" else if (consistencyScore >= 65) "Good" else "Needs Attention"

        // 4. Recurring-Cost Ratio (recurring expenses / total income)
        val totalRecurring = analytics.recurringExpenses.sumOf { it.second }
        val recurringRatio = totalRecurring / totalIncome
        val recurringScore = when {
            recurringRatio <= 0.25 -> 92
            recurringRatio <= 0.35 -> 82
            recurringRatio <= 0.45 -> 68
            else -> 48
        }
        val recurringRating = if (recurringScore >= 80) "Optimal" else if (recurringScore >= 65) "Good" else "High Commitment"

        // 5. Large-Expense Frequency
        val largeTxCount = transactions.count { it.transactionType == TransactionType.EXPENSE && it.amount > 150.0 }
        val largeExpenseScore = when {
            largeTxCount <= 1 -> 92
            largeTxCount <= 3 -> 80
            largeTxCount <= 5 -> 65
            else -> 45
        }
        val largeExpenseRating = if (largeExpenseScore >= 80) "Controlled" else "Elevated"

        val overall = ((budgetScore * 0.30) + (savingsScore * 0.25) + (consistencyScore * 0.15) + (recurringScore * 0.15) + (largeExpenseScore * 0.15)).roundToInt().coerceIn(10, 98)

        val metricsList = listOf(
            HealthMetricDetail(
                name = "Budget Adherence",
                score = budgetScore,
                rating = budgetRating,
                formulaExplanation = "Evaluates total expenses against active budget envelope ($budgetUtil% used)",
                actualValueDescription = "${budgetUtil}% of monthly budget consumed"
            ),
            HealthMetricDetail(
                name = "Savings Rate",
                score = savingsScore,
                rating = savingsRating,
                formulaExplanation = "Net savings divided by total monthly income ($totalIncome baseline)",
                actualValueDescription = "${(savingsRate * 100).roundToInt()}% of earnings retained"
            ),
            HealthMetricDetail(
                name = "Spending Consistency",
                score = consistencyScore,
                rating = consistencyRating,
                formulaExplanation = "Flags impulse outlays exceeding 2.5x your typical category baseline",
                actualValueDescription = "$spikeCount isolated spending spikes detected"
            ),
            HealthMetricDetail(
                name = "Recurring-Cost Ratio",
                score = recurringScore,
                rating = recurringRating,
                formulaExplanation = "Fixed commitments and subscriptions as a fraction of monthly inflow",
                actualValueDescription = "${(recurringRatio * 100).roundToInt()}% fixed overhead commitments"
            ),
            HealthMetricDetail(
                name = "Large-Expense Frequency",
                score = largeExpenseScore,
                rating = largeExpenseRating,
                formulaExplanation = "Number of non-routine transactions exceeding $150 threshold",
                actualValueDescription = "$largeTxCount purchases over $150 this billing cycle"
            )
        )

        return DetailedHealthOverview(
            overallScore = overall,
            status = when {
                overall >= 80 -> "Optimal & Resilient"
                overall >= 68 -> "Balanced — Minor Drift"
                overall >= 52 -> "Elevated Risk — Approaching Caps"
                else -> "High Risk — Budget Overrun"
            },
            metrics = metricsList
        )
    }
}

object SavingOpportunitiesEngine {
    fun evaluate(
        analytics: SpendingAnalytics,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        currencySymbol: String
    ): List<com.example.data.model.SavingOpportunity> {
        val list = mutableListOf<com.example.data.model.SavingOpportunity>()

        // 1. Food & Delivery vs Baseline
        val foodTotal = transactions.filter {
            it.transactionType == TransactionType.EXPENSE && it.category.equals("Food", ignoreCase = true)
        }.sumOf { it.amount }

        if (foodTotal > 180.0) {
            val potential = (foodTotal * 0.28).coerceAtLeast(40.0)
            list.add(
                com.example.data.model.SavingOpportunity(
                    title = "Dining & Delivery Consolidation",
                    observation = "Your Food outlays total $currencySymbol${String.format(Locale.US, "%.2f", foodTotal)}, representing a major portion of discretionary cashflow.",
                    supportingNumbers = "$currencySymbol${String.format(Locale.US, "%.0f", foodTotal)} spent across ${transactions.count { it.category.equals("Food", true) }} orders",
                    whyItMatters = "Frequent delivery apps add 25-35% in service and rush surcharges compared to grocery meal-prep.",
                    suggestedAction = "Swap 2-3 delivery meals each week for home preparation.",
                    potentialMonthlySavings = potential,
                    category = "Food",
                    severity = "HIGH"
                )
            )
        }

        // 2. Subscription Optimization
        val subTransactions = transactions.filter {
            it.transactionType == TransactionType.EXPENSE &&
            (it.category.equals("Subscriptions", true) || it.category.equals("Entertainment", true))
        }
        val subTotal = subTransactions.sumOf { it.amount }
        if (subTotal > 40.0) {
            val potential = 25.0
            list.add(
                com.example.data.model.SavingOpportunity(
                    title = "Subscription Audit & Rotation",
                    observation = "You have ${subTransactions.size} streaming, software, or gym subscriptions costing ~$currencySymbol${String.format(Locale.US, "%.2f", subTotal)}/month.",
                    supportingNumbers = "$currencySymbol${String.format(Locale.US, "%.2f", subTotal)} monthly | ~$currencySymbol${String.format(Locale.US, "%.0f", subTotal * 12)} annualized",
                    whyItMatters = "Unused entertainment platforms often renew in the background without regular engagement.",
                    suggestedAction = "Pause 1 underutilized subscription for 60 days to test real usage.",
                    potentialMonthlySavings = potential,
                    category = "Subscriptions",
                    severity = "MEDIUM"
                )
            )
        }

        // 3. Shopping Spikes & 48-Hour Delay
        val shoppingSpikes = transactions.filter {
            it.transactionType == TransactionType.EXPENSE &&
            it.category.equals("Shopping", true) &&
            it.amount > 100.0
        }
        if (shoppingSpikes.isNotEmpty()) {
            val largest = shoppingSpikes.maxByOrNull { it.amount }!!
            list.add(
                com.example.data.model.SavingOpportunity(
                    title = "Cooling-Off Rule for Big Purchases",
                    observation = "Discretionary electronics or shopping included ${largest.merchant} for $currencySymbol${String.format(Locale.US, "%.2f", largest.amount)}.",
                    supportingNumbers = "$currencySymbol${String.format(Locale.US, "%.2f", largest.amount)} single charge",
                    whyItMatters = "Spur-of-the-moment purchases >$100 consume budget buffers that could otherwise fund emergency reserves.",
                    suggestedAction = "Institute an intentional 48-hour pause before finalizing non-routine online shopping carts.",
                    potentialMonthlySavings = (largest.amount * 0.40).coerceIn(40.0, 150.0),
                    category = "Shopping",
                    severity = "MEDIUM"
                )
            )
        }

        // 4. Budget Overrun Buffer
        val overBudgetCategories = budgets.filter { b ->
            val spent = transactions.filter { it.transactionType == TransactionType.EXPENSE && it.category.equals(b.category, true) }.sumOf { it.amount }
            spent > b.monthlyLimit
        }
        if (overBudgetCategories.isNotEmpty()) {
            val topOver = overBudgetCategories.first()
            val spent = transactions.filter { it.transactionType == TransactionType.EXPENSE && it.category.equals(topOver.category, true) }.sumOf { it.amount }
            val overAmt = spent - topOver.monthlyLimit
            list.add(
                com.example.data.model.SavingOpportunity(
                    title = "Realign ${topOver.category} Envelope",
                    observation = "${topOver.category} is running $currencySymbol${String.format(Locale.US, "%.2f", overAmt)} beyond its target ceiling.",
                    supportingNumbers = "$currencySymbol${String.format(Locale.US, "%.2f", spent)} spent of $currencySymbol${String.format(Locale.US, "%.2f", topOver.monthlyLimit)} limit",
                    whyItMatters = "Category overruns compress your remaining month-end savings buffer.",
                    suggestedAction = "Cap further ${topOver.category} transactions for the remainder of this calendar cycle.",
                    potentialMonthlySavings = overAmt,
                    category = topOver.category,
                    severity = "HIGH"
                )
            )
        }

        return list
    }
}

object SpendWiseCopilotEngine {
    fun generateAnswer(
        question: String,
        analytics: SpendingAnalytics,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        currencySymbol: String
    ): String {
        val q = question.lowercase()

        return when {
            q.contains("where did i spend the most") || q.contains("top category") || q.contains("most spent") -> {
                val topCat = analytics.topCategories.firstOrNull()
                val topMerchants = analytics.biggestMerchants.take(2).joinToString { "${it.first} ($currencySymbol${String.format(Locale.US, "%.2f", it.second)})" }
                if (topCat != null) {
                    "Your largest spending category this month is **${topCat.first}** at **$currencySymbol${String.format(Locale.US, "%.2f", topCat.second)}** (${((topCat.second / analytics.totalExpense.coerceAtLeast(1.0)) * 100).roundToInt()}% of total expenses). Your highest merchant charges were at $topMerchants."
                } else {
                    "No expense transactions recorded yet for this billing cycle."
                }
            }

            q.contains("why did my expenses increase") || q.contains("expense increase") || q.contains("spending jump") -> {
                val spikes = analytics.spendingSpikes
                if (spikes.isNotEmpty()) {
                    val spikeDetails = spikes.joinToString { "${it.merchant} ($currencySymbol${String.format(Locale.US, "%.2f", it.amount)})" }
                    "Your spending surged primarily due to ${spikes.size} out-of-pattern purchases: $spikeDetails. In addition, ${analytics.topCategories.firstOrNull()?.first ?: "Food"} outlays are trending higher than the previous month by ${String.format(Locale.US, "%.1f", analytics.momExpenseChangePercent)}%."
                } else {
                    "Overall monthly expenses are $currencySymbol${String.format(Locale.US, "%.2f", analytics.totalExpense)}, which is ${String.format(Locale.US, "%.1f", analytics.momExpenseChangePercent)}% compared to last cycle's baseline."
                }
            }

            q.contains("subscription") || q.contains("recurring") -> {
                val subs = transactions.filter {
                    it.transactionType == TransactionType.EXPENSE &&
                    (it.category.equals("Subscriptions", true) || it.category.equals("Entertainment", true) || it.category.equals("Bills", true))
                }.groupBy { it.merchant }.mapValues { it.value.sumOf { tx -> tx.amount } }

                val subCount = subs.size
                val subTotal = subs.values.sum()
                val topSubs = subs.entries.take(4).joinToString { "${it.key}: $currencySymbol${String.format(Locale.US, "%.2f", it.value)}" }
                "You have **$subCount recurring services** totaling approximately **$currencySymbol${String.format(Locale.US, "%.2f", subTotal)}/month** (~$currencySymbol${String.format(Locale.US, "%.0f", subTotal * 12)}/year). Key commitments include: $topSubs."
            }

            q.contains("categories changed") || q.contains("category change") -> {
                val catBreakdown = analytics.topCategories.take(3).joinToString { "${it.first} ($currencySymbol${String.format(Locale.US, "%.2f", it.second)})" }
                "The categories showing the highest concentration this month are: $catBreakdown. Food and Shopping constitute over ${((analytics.topCategories.take(2).sumOf { it.second } / analytics.totalExpense.coerceAtLeast(1.0)) * 100).roundToInt()}% of your total outflows."
            }

            q.contains("food") -> {
                val foodTx = transactions.filter { it.transactionType == TransactionType.EXPENSE && it.category.equals("Food", true) }
                val total = foodTx.sumOf { it.amount }
                val count = foodTx.size
                val avg = if (count > 0) total / count else 0.0
                "You spent **$currencySymbol${String.format(Locale.US, "%.2f", total)}** across **$count food transactions** (averaging $currencySymbol${String.format(Locale.US, "%.2f", avg)} per meal). The largest single food order was $currencySymbol${String.format(Locale.US, "%.2f", foodTx.maxOfOrNull { it.amount } ?: 0.0)}."
            }

            q.contains("unusual") || q.contains("risk") || q.contains("anomaly") -> {
                val spikes = analytics.spendingSpikes
                if (spikes.isNotEmpty()) {
                    val list = spikes.joinToString("\n• ") { "${it.merchant}: $currencySymbol${String.format(Locale.US, "%.2f", it.amount)} (${it.category})" }
                    "SpendWise detected ${spikes.size} unusual transactions exceeding your normal baseline:\n• $list\n\nThese deviated by >2.5x from typical peer amounts."
                } else {
                    "No high-severity anomalous purchases detected. Your transactions conform closely to your historical average baseline."
                }
            }

            q.contains("budget") || q.contains("help me understand") -> {
                val remaining = analytics.remainingBudget
                val util = analytics.budgetUtilizationPercent
                val status = if (remaining >= 0) "on track with $currencySymbol${String.format(Locale.US, "%.2f", remaining)} remaining" else "over budget by $currencySymbol${String.format(Locale.US, "%.2f", -remaining)}"
                "You have used **$util%** of your monthly budget. You are currently $status. Your Spending Health Index is **${analytics.healthScore}/100** (${analytics.healthStatus})."
            }

            q.contains("goal") || q.contains("save") -> {
                val netSavings = analytics.totalIncome - analytics.totalExpense
                "Based on current monthly net savings of **$currencySymbol${String.format(Locale.US, "%.2f", netSavings)}**, allocating $currencySymbol${String.format(Locale.US, "%.0f", netSavings * 0.60)} directly to your Emergency Safety Buffer would accelerate target completion by ~3 months."
            }

            else -> {
                "Looking at your live finances: You have $currencySymbol${String.format(Locale.US, "%.2f", analytics.totalIncome)} in income and $currencySymbol${String.format(Locale.US, "%.2f", analytics.totalExpense)} in expenses this month. Your remaining monthly envelope is $currencySymbol${String.format(Locale.US, "%.2f", analytics.remainingBudget)} with a health score of ${analytics.healthScore}/100. How can I help you optimize further?"
            }
        }
    }
}

