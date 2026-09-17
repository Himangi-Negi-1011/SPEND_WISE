package com.example.domain.data

import com.example.data.model.BudgetEntity
import com.example.data.model.SpendWiseCategories
import com.example.data.model.TransactionEntity
import com.example.domain.analysis.CategorizationEngine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ParsedCsvRow(
    val rowNumber: Int,
    val rawLine: String,
    val merchant: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val isValid: Boolean,
    val errorMessage: String? = null,
    val isDuplicate: Boolean = false
)

data class CsvImportResult(
    val totalRows: Int,
    val validRows: Int,
    val invalidRows: Int,
    val duplicateRows: Int,
    val rows: List<ParsedCsvRow>
)

object CsvParser {
    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("MM/dd/yyyy", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US),
        SimpleDateFormat("yyyy/MM/dd", Locale.US)
    )

    fun parse(csvContent: String, existingTransactions: List<TransactionEntity>): CsvImportResult {
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return CsvImportResult(0, 0, 0, 0, emptyList())
        }

        // Determine if first row is header
        val hasHeader = lines[0].lowercase().contains("date") ||
                lines[0].lowercase().contains("amount") ||
                lines[0].lowercase().contains("merchant")

        val dataLines = if (hasHeader) lines.drop(1) else lines
        val parsedRows = mutableListOf<ParsedCsvRow>()

        dataLines.forEachIndexed { index, line ->
            val rowNum = index + (if (hasHeader) 2 else 1)
            val parts = splitCsvLine(line)

            if (parts.size < 2) {
                parsedRows.add(
                    ParsedCsvRow(
                        rowNumber = rowNum,
                        rawLine = line,
                        merchant = "",
                        amount = 0.0,
                        type = "EXPENSE",
                        category = "Other",
                        date = System.currentTimeMillis(),
                        isValid = false,
                        errorMessage = "Insufficient columns. Need at least Date, Merchant, Amount."
                    )
                )
                return@forEachIndexed
            }

            var parsedDate = System.currentTimeMillis()
            var dateFound = false
            for (format in dateFormats) {
                try {
                    val d = format.parse(parts[0].trim())
                    if (d != null) {
                        parsedDate = d.time
                        dateFound = true
                        break
                    }
                } catch (_: Exception) {}
            }

            val merchant = if (parts.size > 1) parts[1].trim() else "Unknown"
            val amountStr = if (parts.size > 2) parts[2].trim().replace("$", "").replace("€", "").replace("£", "").replace(",", "") else "0.0"
            val amount = amountStr.toDoubleOrNull() ?: -1.0

            val rawCategory = if (parts.size > 3) parts[3].trim() else ""
            val rawType = if (parts.size > 4) parts[4].trim().uppercase() else "EXPENSE"

            if (merchant.isBlank()) {
                parsedRows.add(
                    ParsedCsvRow(rowNum, line, merchant, amount, rawType, "Other", parsedDate, false, "Merchant cannot be empty")
                )
                return@forEachIndexed
            }

            if (amount <= 0.0) {
                parsedRows.add(
                    ParsedCsvRow(rowNum, line, merchant, amount, rawType, "Other", parsedDate, false, "Amount must be a positive number")
                )
                return@forEachIndexed
            }

            // Categorization
            val autoCat = if (rawCategory.isNotBlank() && SpendWiseCategories.ALL.any { it.equals(rawCategory, true) }) {
                rawCategory
            } else {
                CategorizationEngine.categorize(merchant).category
            }

            // Check duplicate against existing transactions
            val isDuplicate = existingTransactions.any {
                it.merchant.equals(merchant, ignoreCase = true) &&
                it.amount == amount &&
                Math.abs(it.date - parsedDate) < 24 * 60 * 60 * 1000
            }

            parsedRows.add(
                ParsedCsvRow(
                    rowNumber = rowNum,
                    rawLine = line,
                    merchant = merchant,
                    amount = amount,
                    type = if (rawType == "INCOME") "INCOME" else "EXPENSE",
                    category = autoCat,
                    date = if (dateFound) parsedDate else System.currentTimeMillis(),
                    isValid = true,
                    isDuplicate = isDuplicate
                )
            )
        }

        val total = parsedRows.size
        val valid = parsedRows.count { it.isValid }
        val invalid = parsedRows.count { !it.isValid }
        val duplicates = parsedRows.count { it.isDuplicate }

        return CsvImportResult(total, valid, invalid, duplicates, parsedRows)
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()
        for (c in line) {
            when {
                c == '\"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}

object DemoData {
    fun createSampleTransactions(): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val day = 24L * 60L * 60L * 1000L

        return listOf(
            // Income
            TransactionEntity(
                id = "tx-demo-inc-1",
                merchant = "TechCorp University Stipend",
                amount = 2600.0,
                type = "INCOME",
                category = "Income",
                date = now - (14 * day),
                notes = "Bi-weekly research & graduate stipend",
                isDemo = true
            ),
            TransactionEntity(
                id = "tx-demo-inc-2",
                merchant = "Campus Tutoring Center",
                amount = 600.0,
                type = "INCOME",
                category = "Income",
                date = now - (2 * day),
                notes = "Peer tutoring payment",
                isDemo = true
            ),

            // Rent & Utilities
            TransactionEntity(
                id = "tx-demo-rent-1",
                merchant = "Parkside Apartments",
                amount = 950.0,
                type = "EXPENSE",
                category = "Rent",
                date = now - (12 * day),
                notes = "Monthly studio room lease",
                isDemo = true
            ),
            TransactionEntity(
                id = "tx-demo-util-1",
                merchant = "ConEdison Power & Gas",
                amount = 74.50,
                type = "EXPENSE",
                category = "Utilities",
                date = now - (10 * day),
                notes = "Shared electric & heating",
                isDemo = true
            ),

            // Groceries & Food
            TransactionEntity(
                id = "tx-demo-groc-1",
                merchant = "Trader Joe's",
                amount = 68.20,
                type = "EXPENSE",
                category = "Groceries",
                date = now - (1 * day),
                notes = "Weekly fresh produce and pantry",
                isDemo = true
            ),
            TransactionEntity(
                id = "tx-demo-groc-2",
                merchant = "Whole Foods Market",
                amount = 84.10,
                type = "EXPENSE",
                category = "Groceries",
                date = now - (8 * day),
                notes = "Snacks, grains, and olive oil",
                isDemo = true
            ),
            TransactionEntity(
                id = "tx-demo-food-1",
                merchant = "Starbucks",
                amount = 6.45,
                type = "EXPENSE",
                category = "Food",
                date = now - (4 * day),
                notes = "Iced oat matcha latte",
                isDemo = true
            ),
            TransactionEntity(
                id = "tx-demo-food-2",
                merchant = "Chipotle Mexican Grill",
                amount = 14.80,
                type = "EXPENSE",
                category = "Food",
                date = now - (3 * day),
                notes = "Burrito bowl with guac",
                isDemo = true
            ),

            // Risk: Sudden food delivery spike
            TransactionEntity(
                id = "tx-demo-risk-1",
                merchant = "DoorDash Late Night",
                amount = 78.50,
                type = "EXPENSE",
                category = "Food",
                date = now - (2 * day),
                notes = "Surge priced late night sushi delivery with service fees",
                isFlagged = true,
                riskReason = "Unusually high dining cost (3.5x normal meal baseline)",
                isDemo = true
            ),

            // Transportation
            TransactionEntity(
                id = "tx-demo-trans-1",
                merchant = "MTA Subway Pass",
                amount = 34.00,
                type = "EXPENSE",
                category = "Transportation",
                date = now - (9 * day),
                notes = "7-day unlimited campus transit",
                isDemo = true
            ),
            TransactionEntity(
                id = "tx-demo-trans-2",
                merchant = "Uber Trip",
                amount = 22.40,
                type = "EXPENSE",
                category = "Transportation",
                date = now - (5 * day),
                notes = "Ride home during rainstorm",
                isDemo = true
            ),

            // Subscriptions
            TransactionEntity(
                id = "tx-demo-sub-1",
                merchant = "Spotify Student",
                amount = 5.99,
                type = "EXPENSE",
                category = "Subscriptions",
                date = now - (15 * day),
                notes = "Monthly student streaming",
                isDemo = true
            ),
            // Risk: Subscription fee creeping up
            TransactionEntity(
                id = "tx-demo-sub-2",
                merchant = "Gym & Wellness Club",
                amount = 55.00,
                type = "EXPENSE",
                category = "Subscriptions",
                date = now - (6 * day),
                notes = "Automated charge increased from $35 baseline without prompt warning",
                isFlagged = true,
                riskReason = "Subscription rate jump (+57% over prior month)",
                isDemo = true
            ),

            // Risk: Unusually large shopping impulse
            TransactionEntity(
                id = "tx-demo-shop-1",
                merchant = "Best Buy Electronics",
                amount = 289.99,
                type = "EXPENSE",
                category = "Shopping",
                date = now - (7 * day),
                notes = "Noise-cancelling headphones purchased off-budget",
                isFlagged = true,
                riskReason = "Single purchase accounts for 22% of total discretionary budget",
                isDemo = true
            ),

            // Education
            TransactionEntity(
                id = "tx-demo-edu-1",
                merchant = "University Campus Bookstore",
                amount = 92.50,
                type = "EXPENSE",
                category = "Education",
                date = now - (13 * day),
                notes = "Algorithms course pack & lab notebook",
                isDemo = true
            )
        )
    }

    fun createSampleBudgets(): List<BudgetEntity> {
        return listOf(
            BudgetEntity(id = "b-1", category = "Food", monthlyLimit = 350.0),
            BudgetEntity(id = "b-2", category = "Shopping", monthlyLimit = 250.0),
            BudgetEntity(id = "b-3", category = "Transport", monthlyLimit = 160.0),
            BudgetEntity(id = "b-4", category = "Bills", monthlyLimit = 180.0),
            BudgetEntity(id = "b-5", category = "Subscriptions", monthlyLimit = 80.0),
            BudgetEntity(id = "b-6", category = "Entertainment", monthlyLimit = 120.0),
            BudgetEntity(id = "b-7", category = "Rent", monthlyLimit = 950.0)
        )
    }

    fun createSampleGoals(): List<com.example.data.model.GoalEntity> {
        return listOf(
            com.example.data.model.GoalEntity(
                id = "goal-1",
                title = "Emergency Safety Buffer",
                targetAmount = 3000.0,
                currentAmount = 1850.0,
                deadline = "Dec 2026",
                monthlyContribution = 250.0,
                category = "Savings",
                icon = "🛡️"
            ),
            com.example.data.model.GoalEntity(
                id = "goal-2",
                title = "MacBook Pro M4 Tech Upgrade",
                targetAmount = 1800.0,
                currentAmount = 1150.0,
                deadline = "Nov 2026",
                monthlyContribution = 200.0,
                category = "Tech",
                icon = "💻"
            ),
            com.example.data.model.GoalEntity(
                id = "goal-3",
                title = "Kyoto Autumn Expedition",
                targetAmount = 2400.0,
                currentAmount = 820.0,
                deadline = "Oct 2027",
                monthlyContribution = 140.0,
                category = "Travel",
                icon = "✈️"
            )
        )
    }

    fun createSampleRecurring(): List<com.example.data.model.RecurringEntity> {
        return listOf(
            com.example.data.model.RecurringEntity(
                id = "rec-1",
                merchant = "Spotify Premium",
                amount = 10.99,
                frequency = "Monthly",
                category = "Subscriptions",
                nextPaymentDate = "Oct 4, 2026",
                status = "ESSENTIAL"
            ),
            com.example.data.model.RecurringEntity(
                id = "rec-2",
                merchant = "Netflix 4K Ultra",
                amount = 22.99,
                frequency = "Monthly",
                category = "Entertainment",
                nextPaymentDate = "Oct 12, 2026",
                status = "REVIEW"
            ),
            com.example.data.model.RecurringEntity(
                id = "rec-3",
                merchant = "Gym & Wellness Club",
                amount = 55.00,
                frequency = "Monthly",
                category = "Subscriptions",
                nextPaymentDate = "Oct 1, 2026",
                status = "REVIEW"
            ),
            com.example.data.model.RecurringEntity(
                id = "rec-4",
                merchant = "GitHub Copilot Pro",
                amount = 20.00,
                frequency = "Monthly",
                category = "Education",
                nextPaymentDate = "Oct 18, 2026",
                status = "ESSENTIAL"
            ),
            com.example.data.model.RecurringEntity(
                id = "rec-5",
                merchant = "iCloud 2TB Storage",
                amount = 9.99,
                frequency = "Monthly",
                category = "Subscriptions",
                nextPaymentDate = "Oct 9, 2026",
                status = "ESSENTIAL"
            )
        )
    }

    fun getSampleCsvString(): String {
        return """
Date,Merchant,Amount,Category,Type
2026-09-12,Trader Joe's,68.20,Groceries,EXPENSE
2026-09-11,Starbucks,6.45,Food,EXPENSE
2026-09-10,Uber Trip,22.40,Transportation,EXPENSE
2026-09-09,Best Buy,289.99,Shopping,EXPENSE
2026-09-08,Spotify,5.99,Subscriptions,EXPENSE
2026-09-07,Campus Bookstore,92.50,Education,EXPENSE
2026-09-06,Chipotle,14.80,Food,EXPENSE
2026-09-05,ConEdison,74.50,Utilities,EXPENSE
2026-09-01,TechCorp Stipend,2600.00,Income,INCOME
        """.trimIndent()
    }
}
