package com.example.data.repository

import android.content.Context
import com.example.data.local.SpendWiseDatabase
import com.example.data.model.AiInsight
import com.example.data.model.BudgetEntity
import com.example.data.model.GoalEntity
import com.example.data.model.RecurringEntity
import com.example.data.model.RiskAlert
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.domain.ai.GeminiAiService
import com.example.domain.analysis.RiskDetectionEngine
import com.example.domain.analysis.SpendingAnalytics
import com.example.domain.analysis.SpendingAnalyticsEngine
import com.example.domain.data.DemoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpendWiseRepository(private val context: Context) {
    private val database = SpendWiseDatabase.getInstance(context)
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val goalDao = database.goalDao()
    private val recurringDao = database.recurringDao()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _aiInsights = MutableStateFlow<List<AiInsight>>(emptyList())
    val aiInsights: StateFlow<List<AiInsight>> = _aiInsights.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val goals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val recurring: Flow<List<RecurringEntity>> = recurringDao.getAllRecurring()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkAndSeedDemoDataIfEmpty()
        }
    }

    suspend fun checkAndSeedDemoDataIfEmpty() {
        val existing = transactionDao.getAllTransactions().first()
        if (existing.isEmpty()) {
            seedDemoData()
        } else {
            refreshAiInsights()
        }
    }

    suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        budgetDao.clearAll()
        goalDao.clearAll()
        recurringDao.clearAll()

        val sampleTx = DemoData.createSampleTransactions()
        val sampleBudgets = DemoData.createSampleBudgets()
        val sampleGoals = DemoData.createSampleGoals()
        val sampleRecurring = DemoData.createSampleRecurring()

        transactionDao.insertTransactions(sampleTx)
        budgetDao.insertBudgets(sampleBudgets)
        goalDao.insertGoals(sampleGoals)
        recurringDao.insertAll(sampleRecurring)

        _userProfile.value = UserProfile(isDemoMode = true, isOnboarded = true)
        refreshAiInsights()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        budgetDao.clearAll()
        goalDao.clearAll()
        recurringDao.clearAll()
        _aiInsights.value = emptyList()
    }

    suspend fun addTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
        refreshAiInsights()
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
        refreshAiInsights()
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
        refreshAiInsights()
    }

    suspend fun addTransactions(transactionsList: List<TransactionEntity>) = withContext(Dispatchers.IO) {
        transactionDao.insertTransactions(transactionsList)
        refreshAiInsights()
    }

    suspend fun saveBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun saveGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        goalDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        goalDao.deleteGoal(goal)
    }

    suspend fun contributeToGoal(goalId: String, amount: Double) = withContext(Dispatchers.IO) {
        val all = goalDao.getAllGoals().first()
        val match = all.find { it.id == goalId }
        if (match != null) {
            val updated = match.copy(currentAmount = match.currentAmount + amount)
            goalDao.updateGoal(updated)
        }
    }

    suspend fun saveRecurring(item: RecurringEntity) = withContext(Dispatchers.IO) {
        recurringDao.insertRecurring(item)
    }

    suspend fun updateRecurringStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        val all = recurringDao.getAllRecurring().first()
        val match = all.find { it.id == id }
        if (match != null) {
            recurringDao.updateRecurring(match.copy(status = status))
        }
    }

    suspend fun deleteRecurring(item: RecurringEntity) = withContext(Dispatchers.IO) {
        recurringDao.deleteRecurring(item)
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    suspend fun refreshAiInsights() = withContext(Dispatchers.IO) {
        _isGeneratingAi.value = true
        try {
            val txList = transactionDao.getAllTransactions().first()
            val bgList = budgetDao.getAllBudgets().first()
            val profile = _userProfile.value

            val analytics = SpendingAnalyticsEngine.calculate(
                transactions = txList,
                budgets = bgList,
                monthlyIncome = profile.monthlyIncome,
                targetBudget = profile.targetBudget
            )
            val riskAlerts = RiskDetectionEngine.analyze(
                transactions = txList,
                budgets = bgList,
                monthlyIncome = profile.monthlyIncome,
                targetBudget = profile.targetBudget
            )

            val baseInsights = GeminiAiService.getDeterministicInsights(
                analytics = analytics,
                riskAlerts = riskAlerts,
                currencySymbol = profile.currencySymbol
            ).toMutableList()

            // Try running high-thinking & search grounded live if available
            val deepAuditResult = GeminiAiService.generateDeepRiskAudit(analytics, riskAlerts, profile.currencySymbol)
            deepAuditResult.getOrNull()?.let { highThinking ->
                val idx = baseInsights.indexOfFirst { it.isThinkingMode }
                if (idx != -1) baseInsights[idx] = highThinking else baseInsights.add(0, highThinking)
            }

            val groundedResult = GeminiAiService.generateGroundedBenchmarkInsight(analytics, profile.currencySymbol)
            groundedResult.getOrNull()?.let { grounded ->
                val idx = baseInsights.indexOfFirst { it.searchGroundingSource != null }
                if (idx != -1) baseInsights[idx] = grounded else baseInsights.add(grounded)
            }

            _aiInsights.value = baseInsights
        } catch (_: Exception) {
            // Keep existing
        } finally {
            _isGeneratingAi.value = false
        }
    }
}
